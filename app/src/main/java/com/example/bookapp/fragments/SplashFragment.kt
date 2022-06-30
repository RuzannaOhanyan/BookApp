package com.example.bookapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.navigation.findNavController
import com.example.bookapp.R
import com.example.bookapp.databinding.FragmentSplashBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {
    private lateinit var binding: FragmentSplashBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view= inflater.inflate(R.layout.fragment_splash, container, false)
        auth= FirebaseAuth.getInstance()

//        val sideAnimation = AnimationUtils.loadAnimation(context,R.anim.slide)
//        binding.logo.startAnimation(sideAnimation)

        CoroutineScope(Dispatchers.Main).launch {
            delay(2000L)
            checkUser()
        }
        return view
    }

    private fun checkUser(){
        val user=auth.currentUser
        if(user==null){
            view?.findNavController()?.navigate(R.id.action_splashFragment_to_betweenFragment)
        }
        else{
            val ref= FirebaseDatabase.getInstance().getReference("Users")
            ref.child(user.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userType=snapshot.child("userType").value
                        if(userType=="user"){
                            view?.findNavController()?.navigate(R.id.action_splashFragment_to_dashboardUserFragment)

                        }
                        else if(userType=="admin"){
                            view?.findNavController()?.navigate(R.id.action_splashFragment_to_dashboardAdminFragment)

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
        }
    }
}