package com.pothole.my

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.pothole.my.databinding.ActivityAddPotholeBinding
import android.provider.MediaStore
import android.net.Uri
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.*
import kotlin.collections.HashMap


class AddPotholeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPotholeBinding
    private lateinit var mapFragment: MapsFragment
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    var selectedGeo :GeoPoint? = null

    private var imageURL: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Toast.makeText(this, "Hold and drag the pinpoint to the specific location", Toast.LENGTH_SHORT).show()

        super.onCreate(savedInstanceState)
        binding = ActivityAddPotholeBinding.inflate(layoutInflater)
        setContentView(binding.root)


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        getCurrentLocation()

        val launcher = registerForActivityResult(
            StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK
                && result.data != null
            ) {
                val photoUri: Uri? = result.data!!.data


                val storage = Firebase.storage
                val storageRef = storage.reference
                val imageRef = storageRef.child("images/${UUID.randomUUID().toString()}")
                val uploadTask = imageRef.putFile(photoUri!!)

                Toast.makeText(this, "Uploading...", Toast.LENGTH_LONG).show()

                uploadTask.continueWithTask {task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    imageRef.downloadUrl
                }.addOnCompleteListener{ task ->
                    Toast.makeText(this, "Upload successfully!", Toast.LENGTH_SHORT).show()
                    binding.imgView.setImageURI(photoUri)
                    imageURL = task.result.toString()
                }

                    .addOnFailureListener{
                    Toast.makeText(this, "Upload failed!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnChoose.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            launcher.launch(intent)
        }

        binding.submitButton.setOnClickListener{
            if(imageURL.isNullOrEmpty()){
                Toast.makeText(this, "Please upload image!", Toast.LENGTH_SHORT).show()
            }
            else onSubmit()
        }

    }

    fun setAddressText (text:String){
        binding.addressText.text = text
    }

    fun setCurrentGeo(geo:GeoPoint){
        selectedGeo = geo
    }

    private fun onSubmit(){

                val user = Firebase.auth.currentUser
                user?.let{
                    val email = user.email
                    val pothole: MutableMap<String, Any?> = HashMap()
                    pothole["address"] = binding.addressText.text
                    pothole["email"] = email.toString()
                    pothole["geopoint"] = selectedGeo
                    pothole["image"] = imageURL

                    val db = Firebase.firestore

                    db.collection("pothole").add(pothole)
                        .addOnSuccessListener { documentReference ->
                            Toast.makeText(this, "Report Successfully!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to report!", Toast.LENGTH_SHORT).show()
                        }
                }
            }





    private fun getCurrentLocation(){
        mapFragment = MapsFragment()

        supportFragmentManager
            .beginTransaction()
            .add(R.id.mapView, mapFragment , "MapFragment")
            .commit()

        mapFragment.getCurrentLayout("add")

        if(checkPermissions()){
            if(isLocationEnabled()){
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermission()
                    return
                }
                fusedLocationProviderClient.lastLocation.addOnCompleteListener(this){ task ->
                    val location: Location?=task.result
                    if (location != null) {
                        mapFragment.setCurrentLocation(location.latitude, location?.longitude)

                    }
                }
            } else{
                Toast.makeText(this,"Please turn on location", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermission()
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_ACCESS_LOCATION = 100
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ){
            return true
        }
        return false;
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this,arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION
            , Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_ACCESS_LOCATION)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode== PERMISSION_REQUEST_ACCESS_LOCATION){
            if(grantResults.isNotEmpty() && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                getCurrentLocation()
            } else {
                Toast.makeText(this,"Failed to get permission for location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isLocationEnabled():Boolean {
        val locationManager: LocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)||locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }
}