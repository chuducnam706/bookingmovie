package com.example.film.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.film.databinding.FragmentSettingBinding
import com.example.film.model.UserModel
import com.example.film.ui.activity.LoginActivity
import com.example.moneymanagement.presentation.view.base.BaseFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingFragment : BaseFragment<FragmentSettingBinding>(FragmentSettingBinding::inflate) {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var currentUserModel: UserModel? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        loadUserData()

        binding.btnUpdate.setOnClickListener {
            updateProfile()
        }

        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun loadUserData() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }
        
        val uid = user.uid
        binding.progressBar.visibility = View.VISIBLE

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (!isAdded) return@addOnSuccessListener
                binding.progressBar.visibility = View.GONE
                if (document != null && document.exists()) {
                    currentUserModel = document.toObject(UserModel::class.java)
                    currentUserModel?.let { userModel ->
                        binding.edtName.setText(userModel.name)
                        binding.edtEmail.setText(userModel.email)
                        binding.edtPhone.setText(userModel.phone)
                        Toast.makeText(requireContext(), "Data loaded successfully", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Nếu không có document, lấy thông tin mặc định từ Auth
                    binding.edtEmail.setText(user.email)
                    Toast.makeText(requireContext(), "No profile found. Please update your info.", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                if (!isAdded) return@addOnFailureListener
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Fetch failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun updateProfile() {
        val user = auth.currentUser ?: return
        val uid = user.uid
        val name = binding.edtName.text.toString().trim()
        val email = binding.edtEmail.text.toString().trim()
        val phone = binding.edtPhone.text.toString().trim()

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(context, "Please fill Name and Phone", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnUpdate.isEnabled = false

        // Sử dụng UserModel để đồng bộ với RegisterActivity
        val updatedUser = UserModel(uid, name, email, phone)

        db.collection("users").document(uid).set(updatedUser)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                binding.btnUpdate.isEnabled = true
                Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.btnUpdate.isEnabled = true
                Toast.makeText(context, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun logout() {
        auth.signOut()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}