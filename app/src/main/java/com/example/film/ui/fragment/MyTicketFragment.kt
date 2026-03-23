package com.example.film.ui.fragment

import android.os.Bundle

import android.view.View
import com.example.film.databinding.FragmentMyTicketBinding
import com.example.film.model.BookingModel
import com.example.film.ui.adapter.TicketAdapter
import com.example.moneymanagement.presentation.view.base.BaseFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MyTicketFragment : BaseFragment<FragmentMyTicketBinding>(FragmentMyTicketBinding::inflate) {

    private lateinit var adapter: TicketAdapter
    private val bookingList = mutableListOf<BookingModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchBookings()
    }

    private fun fetchBookings() {
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("bookings")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { result ->
                bookingList.clear()
                for (document in result) {
                    val booking = document.toObject(BookingModel::class.java)
                    bookingList.add(booking)
                }
                // In-memory sort to avoid needing a Firestore composite index
                bookingList.sortByDescending { it.timestamp }
                
                adapter = TicketAdapter(bookingList)
                binding.lstTicket.adapter = adapter
            }

            .addOnFailureListener { e ->
                android.util.Log.e("MyTicketFragment", "Error fetching bookings", e)
                android.widget.Toast.makeText(context, "Failed to load tickets: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                if (e.message?.contains("index") == true) {
                    android.util.Log.d("MyTicketFragment", "Missing index. Check Logcat for the link to create it.")
                }
            }

    }
}