package com.pothole.my

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.pothole.my.databinding.ActivityLoginBinding
import java.lang.NullPointerException

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        auth = FirebaseAuth.getInstance()

        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener{
            val email = binding.email.text.toString()
            val pass = binding.password.text.toString()

            if(email.isNotEmpty() && pass.isNotEmpty()){

                    auth.signInWithEmailAndPassword(email,pass).addOnCompleteListener{
                        if(it.isSuccessful){
                            Toast.makeText(this,"Sign In successfully!",Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, HomeActivity::class.java))
                        }
                    }.addOnFailureListener { error -> Toast.makeText(this,error.message.toString(),Toast.LENGTH_SHORT).show() }

            } else {
                Toast.makeText(this,"Please fill in all the empty field!",Toast.LENGTH_SHORT).show()
            }
        }


    }


}
