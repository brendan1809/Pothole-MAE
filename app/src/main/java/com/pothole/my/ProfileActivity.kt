package com.pothole.my

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.pothole.my.databinding.ActivityProfileBinding
import android.content.Intent
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase


class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var potholeArrayList: ArrayList<PotholeClass>
    private lateinit var potholeAdapter: PotholeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imageButton.setOnClickListener{
            FirebaseAuth.getInstance().signOut();
            startActivity(Intent(this, MainActivity::class.java))
            Toast.makeText(this,"Log out successfully", Toast.LENGTH_SHORT).show()
        }

        val user = Firebase.auth.currentUser
        if (user != null) {
            val email = user.email
            binding.emailText.text = email
        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        potholeArrayList = arrayListOf()

        potholeAdapter = PotholeAdapter(potholeArrayList)

        recyclerView.adapter = potholeAdapter

        EventChangeListener()



    }

    private fun EventChangeListener(){

        val db = FirebaseFirestore.getInstance()
        val user = Firebase.auth.currentUser
        if (user != null) {
            val email = user.email
            db.collection("pothole").whereEqualTo("email",email)
                .addSnapshotListener(object: EventListener<QuerySnapshot>{
                    override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                        if(error != null){
                            return
                        }

                        for(dc:DocumentChange in value?.documentChanges!!){
                            if(dc.type == DocumentChange.Type.ADDED){
                                potholeArrayList.add(dc.document.toObject(PotholeClass::class.java))
                            }
                        }

                        potholeAdapter.notifyDataSetChanged()
                    }
                })

        }

    }


}