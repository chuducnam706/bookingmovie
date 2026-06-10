package com.example.film.ui.activity.admin.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.film.databinding.FragmentAdminFoodManagementBinding
import com.example.film.model.FoodItem
import com.example.film.ui.adapter.FoodManagementAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class FoodManagementFragment : Fragment() {

    private var _binding: FragmentAdminFoodManagementBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private lateinit var foodAdapter: FoodManagementAdapter
    private var foodListener: ListenerRegistration? = null
    private var selectedFoodImageUri: Uri? = null

    private val pickFoodImageLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@registerForActivityResult
        requireContext().contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        selectedFoodImageUri = uri
        binding.edtFoodImage.setText(uri.toString())
        binding.imgFoodPreview.visibility = View.VISIBLE
        Glide.with(this)
            .load(uri)
            .into(binding.imgFoodPreview)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminFoodManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFoodRecyclerView()
        listenToFoods()
        binding.btnPickFoodImage.setOnClickListener {
            pickFoodImageLauncher.launch(arrayOf("image/*"))
        }
        binding.btnAddFood.setOnClickListener { addNewFood() }
    }

    private fun setupFoodRecyclerView() {
        foodAdapter = FoodManagementAdapter(
            foods = emptyList(),
            onUpdatePrice = { food, newPrice ->
                db.collection("foods").document(food.id.toString())
                    .update("price", newPrice)
            },
            onDelete = { food ->
                db.collection("foods").document(food.id.toString()).delete()
            }
        )
        binding.rvFoodList.adapter = foodAdapter
    }

    private fun listenToFoods() {
        foodListener = db.collection("foods")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val foodList = snapshot.documents.mapNotNull { doc ->
                        val id = doc.id.toIntOrNull() ?: 0
                        val name = doc.getString("name") ?: ""
                        val price = doc.getLong("price") ?: 0L
                        val imageUrl = doc.getString("imageUrl") ?: ""
                        FoodItem(id, name, price, imageUrl)
                    }
                    foodAdapter.updateData(foodList)
                }
            }
    }

    private fun addNewFood() {
        val name = binding.edtFoodName.text.toString().trim()
        val priceStr = binding.edtFoodPrice.text.toString().trim()
        val imageUrl = binding.edtFoodImage.text.toString().trim()

        if (name.isEmpty() || priceStr.isEmpty() || imageUrl.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceStr.toLongOrNull() ?: 0L
        val id = System.currentTimeMillis().toInt()
        val food = hashMapOf(
            "name" to name,
            "price" to price,
            "imageUrl" to imageUrl
        )

        db.collection("foods").document(id.toString())
            .set(food)
            .addOnSuccessListener {
                binding.edtFoodName.text?.clear()
                binding.edtFoodPrice.text?.clear()
                binding.edtFoodImage.text?.clear()
                selectedFoodImageUri = null
                binding.imgFoodPreview.setImageDrawable(null)
                binding.imgFoodPreview.visibility = View.GONE
                Toast.makeText(requireContext(), "Đã thêm đồ ăn", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        foodListener?.remove()
        foodListener = null
        _binding = null
    }
}
