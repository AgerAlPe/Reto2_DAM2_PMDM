package com.grupo2.elorchat.ui.socket

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.grupo2.elorchat.data.Message
import com.grupo2.elorchat.databinding.ItemMessageBinding
import com.grupo2.elorchat.databinding.ItemMessageGpsBinding
import java.util.Base64

class MessageAdapter : ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_COORDINATE -> {
                val binding = ItemMessageGpsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                CoordinateMessageViewHolder(binding)
            }
            else -> {
                val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                MessageViewHolder(binding)
            }
        }
    }
    var coordinateString : String = ""

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is MessageViewHolder -> holder.bind(message)
            is CoordinateMessageViewHolder -> holder.bind(message)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        coordinateString = message.message
        return if (message.isCoordinate()) {
            TYPE_COORDINATE
        } else {
            TYPE_NORMAL
        }
    }

    inner class MessageViewHolder(private val binding: ItemMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            if (message.message.length > 500) {
                val bitmap = decodeBase64ToBitmap(message.message)
                val resizedBitmap = resizeBitmap(bitmap, 400, 400) // Adjust the width and height as needed
                binding.messageText.background = BitmapDrawable(itemView.resources, resizedBitmap)
                binding.messagerName.text = message.name
                binding.messageDate.text = message.createdAt
            } else {
                binding.messageText.text = message.message
                binding.messagerName.text = message.name
                binding.messageDate.text = message.createdAt
                // You can bind other properties as needed
            }
        }
        private fun decodeBase64ToBitmap(base64String: String): Bitmap {
            val decodedBytes = Base64.getDecoder().decode(base64String)
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        }

        private fun resizeBitmap(bitmap: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
            return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        }
    }

    inner class CoordinateMessageViewHolder(private val binding: ItemMessageGpsBinding) :
        RecyclerView.ViewHolder(binding.root), OnMapReadyCallback {

        private var mapView: MapView = binding.mapView
        private var googleMap: GoogleMap? = null
        private var coordinateString: String = ""

        fun bind(message: Message) {
            binding.messagerName.text = message.name
            binding.messageDate.text = message.createdAt
            coordinateString = message.message // Store the message string
            // Initialize the MapView and request the map asynchronously
            mapView.onCreate(null)
            mapView.getMapAsync(this)
        }

        override fun onMapReady(map: GoogleMap) {
            googleMap = map
            // Customize the map as needed
            googleMap?.uiSettings?.isZoomControlsEnabled = true
            googleMap?.uiSettings?.isCompassEnabled = true

            // Example coordinates
            val coordinates = extractCoordinatesFromString(coordinateString)
            coordinateString = "" // Clear the stored message string after extracting coordinates
            coordinates?.let { MarkerOptions().position(it).title("Coordinates") }?.let {
                googleMap?.addMarker(
                    it
                )
            }
            coordinates?.let { CameraUpdateFactory.newLatLngZoom(it, 10f) }
                ?.let { googleMap?.moveCamera(it) }
        }

        private fun extractCoordinatesFromString(message: String): LatLng? {
            val pattern = Regex("Latitude: (-?\\d+\\.\\d+), Longitude: (-?\\d+\\.\\d+)")
            val matchResult = pattern.find(message)
            if (matchResult != null) {
                val (latitude, longitude) = matchResult.destructured
                return LatLng(latitude.toDouble(), longitude.toDouble())
            }
            return null
        }

        fun onResume() {
            mapView.onResume()
        }

        fun onPause() {
            mapView.onPause()
        }

        fun onDestroy() {
            mapView.onDestroy()
        }

        fun onLowMemory() {
            mapView.onLowMemory()
        }
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.message == newItem.message &&
                    oldItem.userId == newItem.userId &&
                    oldItem.chatId == newItem.chatId &&
                    oldItem.createdAt == newItem.createdAt
        }
    }

    private fun Message.isCoordinate(): Boolean {
        // Define your logic here to determine if the message is a coordinate
        val regex = """Latitude:\s*(-?\d+\.\d+),\s*Longitude:\s*(-?\d+\.\d+)""".toRegex()
        val matchResult = regex.find(message)
        if (matchResult != null) {
            val (latitude, longitude) = matchResult.destructured
            println("Message '$message' is considered to be a coordinate. Latitude: $latitude, Longitude: $longitude")
            return true // Message represents coordinates
        }
        println("Message '$message' is not considered to be a coordinate.")
        return false // Message does not represent coordinates
    }
    companion object {
        private const val TYPE_NORMAL = 0
        private const val TYPE_COORDINATE = 1
    }
}