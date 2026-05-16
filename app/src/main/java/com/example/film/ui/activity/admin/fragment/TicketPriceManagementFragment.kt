package com.example.film.ui.activity.admin.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.film.databinding.FragmentAdminTicketPriceManagementBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class TicketPriceManagementFragment : Fragment() {

    private var _binding: FragmentAdminTicketPriceManagementBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private var ticketPriceListener: ListenerRegistration? = null
    private var currentTicketPrice: Long = 70000L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminTicketPriceManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listenToTicketPrice()
        binding.btnIncreaseTicket.setOnClickListener { updateTicketPrice(currentTicketPrice + 5000) }
        binding.btnDecreaseTicket.setOnClickListener { updateTicketPrice(currentTicketPrice - 5000) }
    }

    private fun listenToTicketPrice() {
        ticketPriceListener = db.collection("settings").document("config")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    currentTicketPrice = snapshot.getLong("ticketPrice") ?: 70000L
                    binding.txtCurrentTicketPrice.text = String.format("Giá hiện tại: %,dđ", currentTicketPrice)
                }
            }
    }

    private fun updateTicketPrice(newPrice: Long) {
        if (newPrice < 0) return
        db.collection("settings").document("config")
            .set(mapOf("ticketPrice" to newPrice))
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Lỗi cập nhật giá vé", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ticketPriceListener?.remove()
        ticketPriceListener = null
        _binding = null
    }
}
