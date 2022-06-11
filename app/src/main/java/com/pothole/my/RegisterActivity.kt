package com.pothole.my

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pothole.my.databinding.ActivityRegisterBinding
import java.lang.NullPointerException



class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        auth = FirebaseAuth.getInstance()
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.registerButton.setOnClickListener {
           val email = binding.email.text.toString()
            val pass = binding.password.text.toString()
            val confirmPass = binding.confirmPassword.text.toString()
            val userName = binding.userName.text.toString()

            val db = Firebase.firestore

            if(userName.isNotEmpty()&& email.isNotEmpty() && pass.isNotEmpty()&&confirmPass.isNotEmpty()){
                if(pass == confirmPass){
                    auth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener{
                        if(it.isSuccessful){
                            val user: MutableMap<String, Any> = HashMap()
                            user["username"] = userName
                            user["email"] = email

                            db.collection("users").add(user)
                                .addOnSuccessListener { documentReference ->
                                    Toast.makeText(this,"Sign up successfully!",Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, MainActivity::class.java))
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this,"Failed to sign up!",Toast.LENGTH_SHORT).show()
                                }
                        }
                    }.addOnFailureListener { error -> Toast.makeText(this,error.message.toString(),Toast.LENGTH_SHORT).show() }
                } else {
                    Toast.makeText(this,"Password is not the match!",Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this,"Please fill in all the empty field!",Toast.LENGTH_SHORT).show()
            }
        }

    }

}