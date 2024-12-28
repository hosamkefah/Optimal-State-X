package com.example.Optimal_State_X

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.Optimal_State_X.databinding.ActivityManageClientsBinding
import com.example.Optimal_State_X.databinding.ItemClientBinding
//import com.example.loginapplication.databinding.ActivityManageClientsBinding
//import com.example.loginapplication.databinding.ItemClientBinding
//import com.example.Optimal_State_X.databinding.ActivityManageClientsBinding
//import com.example.Optimal_State_X.databinding.ItemClientBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ManageClientsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityManageClientsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var clientsAdapter: ClientsAdapter
    private val TAG = "ManageClientsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageClientsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.progressBar.visibility = View.VISIBLE
        auth = FirebaseAuth.getInstance()

        setupRecyclerView()
        loadClients()

        binding.backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        clientsAdapter = ClientsAdapter(mutableListOf()) { clientId ->
            showDeleteConfirmation(clientId)
        }
        binding.clientsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ManageClientsActivity)
            adapter = clientsAdapter
        }
    }

    private fun loadClients() {
        val currentProviderId = auth.currentUser?.uid ?: return
        val firestoreDb = Firebase.firestore
        val realtimeDb = Firebase.database("https://optimal-state-x-8b723-default-rtdb.firebaseio.com").reference

        firestoreDb.collection("proLink").document(currentProviderId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val clients = document.get("clients") as? List<String> ?: listOf()
                    val clientsList = mutableListOf<ClientData>()

                    // For each client ID, fetch their details from Realtime Database
                    clients.forEach { clientId ->
                        realtimeDb.child("Users").child("Patients").child(clientId).get()
                            .addOnSuccessListener { snapshot ->
                                val firstname = snapshot.child("firstname").getValue(String::class.java) ?: ""
                                val lastname = snapshot.child("lastname").getValue(String::class.java) ?: ""
                                val email = snapshot.child("email").getValue(String::class.java) ?: ""

                                clientsList.add(ClientData(clientId, "$firstname $lastname", email))
                                clientsAdapter.updateClients(clientsList)
                            }
                    }
                }
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading clients", e)
                Toast.makeText(this, "Error loading clients", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDeleteConfirmation(clientId: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Client")
            .setMessage("Are you sure you want to remove this client?")
            .setPositiveButton("Yes") { _, _ ->
                deleteClient(clientId)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteClient(clientId: String) {
        val currentProviderId = auth.currentUser?.uid ?: return
        val firestoreDb = Firebase.firestore
        val proLinkRef = firestoreDb.collection("proLink").document(currentProviderId)

        proLinkRef.update("clients", FieldValue.arrayRemove(clientId))
            .addOnSuccessListener {
                Toast.makeText(this, "Client removed successfully", Toast.LENGTH_SHORT).show()
                loadClients() // Reload the list
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error removing client", e)
                Toast.makeText(this, "Error removing client", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivityProvider::class.java)
        startActivity(intent)
        finish()
    }
}

data class ClientData(
    val id: String,
    val name: String,
    val email: String
)

class ClientsAdapter(
    private var clients: MutableList<ClientData>,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<ClientsAdapter.ClientViewHolder>() {

    class ClientViewHolder(private val binding: ItemClientBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(client: ClientData, onDeleteClick: (String) -> Unit) {
            binding.clientNameText.text = client.name
            binding.clientEmailText.text = client.email
            binding.deleteButton.setOnClickListener {
                onDeleteClick(client.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientViewHolder {
        val binding = ItemClientBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ClientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ClientViewHolder, position: Int) {
        holder.bind(clients[position], onDeleteClick)
    }

    override fun getItemCount() = clients.size

    fun updateClients(newClients: List<ClientData>) {
        clients.clear()
        clients.addAll(newClients)
        notifyDataSetChanged()
    }
}