package com.example.bookapp.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.example.bookapp.MyApplication
import com.example.bookapp.R
import com.example.bookapp.databinding.FragmentProfileEditBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage


class ProfileEditFragment : Fragment() {

    private lateinit var binding: FragmentProfileEditBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    private var imageUri: Uri? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog = ProgressDialog(this.context)
        progressDialog.setTitle("Խնդրում եմ սպասել")
        progressDialog.setCanceledOnTouchOutside(false)


        firebaseAuth = FirebaseAuth.getInstance()
        loadUserInfo()

        binding.backBtn.setOnClickListener {
            view.findNavController().navigate(R.id.action_profileEditFragment_to_profileFragment)
        }
        binding.updateBtn.setOnClickListener {
            validateData()

        }
        binding.profileIv.setOnClickListener {
            showImageAttachMenu()

        }

    }

    private var name = ""
    private fun validateData() {

        name = binding.nameEt.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(this.context, "Անունը!!", Toast.LENGTH_SHORT).show()
        } else {
            if (imageUri == null) {
                updateProfile("")
            } else {
                uploadImage()
            }
        }
    }

    private fun loadUserInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"

                    val formattedDate = MyApplication.formatTimeStamp(timestamp.toLong())
                    binding.nameEt.setText(name)


                    try {
                        Glide.with(this@ProfileEditFragment).load(profileImage)
                            .placeholder(R.drawable.ic_person_gray).into(binding.profileIv)
                    } catch (e: Exception) {


                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }


            })

    }

    private fun showImageAttachMenu() {

        val popupMenu = this.context?.let { PopupMenu(it, binding.profileIv) }
        popupMenu?.menu?.add(Menu.NONE, 0, 0, "Տեսախցիկ")
        popupMenu?.menu?.add(Menu.NONE, 1, 1, "Նկարներ")
        popupMenu?.show()
        popupMenu?.setOnMenuItemClickListener { item ->
            val id = item.itemId
            if (id == 0) {
                pickImageCamera()
            } else if (id == 1) {
                pickImageGallery()
            }
            true
        }

    }

    private fun pickImageCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Temp_Title")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Description")

        imageUri = requireContext().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private fun pickImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                binding.profileIv.setImageURI(imageUri)
            } else {
                Toast.makeText(this.context, "Չեղարկված է", Toast.LENGTH_SHORT).show()
            }
        }
    )

    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                imageUri = data!!.data
                binding.profileIv.setImageURI(imageUri)
            } else {
                Toast.makeText(this.context, "Չեղարկված է", Toast.LENGTH_SHORT).show()
            }
        }
    )


    private fun updateProfile(uploadedImageUrl: String) {
        progressDialog.setMessage("Պրոֆիլի թարմացում")
        val hashmap: HashMap<String, Any> = HashMap()
        hashmap["name"] = "$name"
        if (imageUri != null) {
            hashmap["profileImage"] = uploadedImageUrl
        }
        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child(firebaseAuth.uid!!)
            .updateChildren(hashmap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this.context, "Պրոֆիլը թարմացվել է", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this.context,
                    "Չհաջողվեց թարմացնել պրոֆիլը, քանի որ${e.message})",
                    Toast.LENGTH_SHORT
                ).show()
            }

    }


    private fun uploadImage() {
        progressDialog.setMessage("Պրոֆիլի պատկերի վերբեռնում")
        progressDialog.show()

        val filePathAndName = "Profileimages/" + firebaseAuth.uid
        val reference = FirebaseStorage.getInstance().getReference(filePathAndName)
        reference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedImageUrl = "${uriTask.result}"
                updateProfile(uploadedImageUrl)
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this.context,
                    "Չհաջողվեց վերբեռնել պատկերը, քանի որ ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


}