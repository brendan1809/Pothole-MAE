package com.pothole.my

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.GeoPoint
import com.pothole.my.databinding.ActivityAddPotholeBinding
import java.util.*


class MapsFragment : Fragment() {

    private var currentMarker:Marker? = null
    private var db = Firebase.firestore
    private var currentLatitude  = 3.1390
    private var currentLongitude  = 101.6869
    private var currentLayout  = "normal"


    private val callback = OnMapReadyCallback { gMap ->
        db = FirebaseFirestore.getInstance()

        db.collection("pothole")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val pos: GeoPoint = document.data["geopoint"] as GeoPoint
                    val latLng = LatLng(pos.latitude, pos.longitude)
                    gMap.addMarker(MarkerOptions().position(latLng).title(getAddressDefault(pos.latitude,pos.longitude)))
                }
            }
            .addOnFailureListener {  error ->
                Toast.makeText(activity,error.message.toString(),Toast.LENGTH_SHORT).show()
            }

        val malaysia = LatLng(currentLatitude, currentLongitude)
        val center = CameraUpdateFactory.newLatLng(
            malaysia
        )
        val zoom = CameraUpdateFactory.zoomTo(16f)
        gMap.moveCamera(center)
        gMap.animateCamera(zoom)

       if(currentLayout == "add"){
           val point = GeoPoint(currentLatitude,currentLongitude)
           (activity as AddPotholeActivity).setCurrentGeo(point)
           drawMarker(malaysia,gMap)
           gMap.setOnMarkerDragListener(object: GoogleMap.OnMarkerDragListener{
               override fun onMarkerDrag(p0: Marker) {
//              ignore
               }

               override fun onMarkerDragEnd(p0: Marker) {
                    if(currentMarker!=null){
                        currentMarker?.remove()

                        val newLatlng = LatLng(p0?.position!!.latitude,p0?.position!!.longitude)
                        drawMarker(newLatlng,gMap)
                        val point = GeoPoint(p0?.position!!.latitude,p0?.position!!.longitude)
                        (activity as AddPotholeActivity).setCurrentGeo(point)

                    }
               }

               override fun onMarkerDragStart(p0: Marker) {
//              ignore
               }
           })
       }

    }

    private fun drawMarker(latlong:LatLng,googleMap: GoogleMap) {
        val markerOptions = MarkerOptions().position(latlong).title(getAddress(latlong.latitude,latlong.longitude)).draggable(true)

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlong,10f))
        currentMarker = googleMap.addMarker(markerOptions)
        currentMarker?.showInfoWindow()
    }

    private fun getAddressDefault(lat: Double, long: Double): String? {
        val geoCoder = Geocoder(this.context, Locale.getDefault())
        val address = geoCoder.getFromLocation(lat,long,1)
        return address[0].getAddressLine(0).toString()
    }

    private fun getAddress(lat: Double, long: Double): String? {
        val geoCoder = Geocoder(this.context, Locale.getDefault())
        val address = geoCoder.getFromLocation(lat,long,1)
        (activity as AddPotholeActivity).setAddressText(address[0].getAddressLine(0).toString())
        return address[0].getAddressLine(0).toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

    }

    public fun setCurrentLocation (lat:Double,long:Double) {
        currentLatitude = lat
        currentLongitude = long
    }

    public fun getCurrentLayout (layout:String){
        currentLayout = layout
    }



}
