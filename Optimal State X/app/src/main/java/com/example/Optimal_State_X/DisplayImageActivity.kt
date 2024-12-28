package com.example.Optimal_State_X

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.Optimal_State_X.databinding.ActivityDisplayImageBinding
//import com.example.loginapplication.R
//import com.example.loginapplication.databinding.ActivityDisplayImageBinding
//import com.example.Optimal_State_X.databinding.ActivityDisplayImageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.atan2
import kotlin.math.pow

import kotlin.math.sqrt

class DisplayImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDisplayImageBinding
    private val TAG = "DisplayImageActivity"
    private lateinit var auth: FirebaseAuth
    private var region: String = ""

    private var sourceBitmap: Bitmap? = null
    private var canvasBitmap: Bitmap? = null
    private var canvas: Canvas? = null
    private val touchRegionRadius = 500f // Radius of the outer clickable region

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDisplayImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "Activity created. Fetching image URL...")
        fetchImageUrl()


        // Handle touch events on the ImageView
        binding.imageView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                // Map touch coordinates to the bitmap
                val (mappedX, mappedY) = mapTouchToBitmap(event.x, event.y)
                if (mappedX != null && mappedY != null) {
                     region = getTouchedRegion(mappedX, mappedY)
                    // Update the TextView with the region type and color
                    binding.regionTypeTextView.text = region
                    binding.regionTypeTextView.visibility = View.VISIBLE
                    binding.regionTypeTextView.setTextColor(
                        when (region) {
                            "Red" -> Color.RED
                            "White" -> Color.parseColor("#808080")
                            "Blue" -> Color.BLUE
                            "Gold" -> Color.parseColor("#ffc476")
                            else -> Color.BLACK
                        })
                   // Toast.makeText(this, "Touched region: $region", Toast.LENGTH_SHORT).show()

                    if (region != "Out of bounds") {
                        drawPointer(mappedX, mappedY)
                    } else {
                        Toast.makeText(this, "Please touch inside the circular region", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            true
        }

        binding.save1.setOnClickListener {
            val auth = FirebaseAuth.getInstance()
            val userId = auth.currentUser?.uid

            if (userId.isNullOrEmpty()) {
                Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Reference Firestore
            val db = Firebase.firestore
            val selectedRegion = region // The region currently selected

            db.collection("state").document(userId).get()
                .addOnSuccessListener { document ->
                    val timestamps = document.get("timestamps") as? Map<String, String> ?: emptyMap<String, String>().toMutableMap()

                    // Add the new timestamp with the selected region
                    val currentTimestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)

                    (timestamps as MutableMap)[currentTimestamp] = selectedRegion

                    // Count occurrences of each region
                    val regionCounts = timestamps.values.groupingBy { it }.eachCount()

                    // Calculate total count of all timestamps
                    val totalCount = timestamps.size

                    // Calculate percentages
                    val percentages = regionCounts.mapValues { (_, count) ->
                        (count.toDouble() / totalCount) * 100
                    }

                    // Update Firestore
                    val updatedData = mapOf(
                        "timestamps" to timestamps,
                        "percentages" to percentages
                    )

                    db.collection("state").document(userId).set(updatedData, SetOptions.merge())
                        .addOnSuccessListener {
                            Toast.makeText(this, "Successfully Updated!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { exception ->
                            Log.e("FirestoreError", "Failed to update data", exception)
                            Toast.makeText(this, "Failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { exception ->
                    Log.e("FirestoreError", "Failed to retrieve data", exception)
                    Toast.makeText(this, "Failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }




        binding.goback2.setOnClickListener {
                val intent1 = Intent(this, MainActivity::class.java)
                startActivity(intent1)
                finish()
            }

    }

    private fun fetchImageUrl() {
        val db = Firebase.firestore
        val docRef = db.collection("test").document("VUyGCMUv1RSlj1uPTyRI")

        binding.progressBar.visibility = View.VISIBLE
        Log.d(TAG, "Querying Firestore document: ${docRef.path}")

        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    Log.d(TAG, "Document found: ${document.id}")
                    val imageUrl = document.getString("assesmentUrl")

                    if (imageUrl != null && imageUrl != "null") {
                        Log.d(TAG, "Image URL retrieved: $imageUrl")
                        loadImage(imageUrl)
                    } else {
                        Log.e(TAG, "Image URL field is empty or missing.")
                        Toast.makeText(this, "Image URL not found in document", Toast.LENGTH_SHORT).show()
                        binding.progressBar.visibility = View.GONE
                    }
                } else {
                    Log.e(TAG, "No such document in Firestore.")
                    Toast.makeText(this, "Document does not exist", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching document: ${exception.message}", exception)
                Toast.makeText(this, "Failed to fetch data: ${exception.message}", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
            }
    }

    private fun loadImage(imageUrl: String) {
        Log.d(TAG, "Loading image with Glide...")
        Glide.with(this)
            .asBitmap()
            .load(imageUrl)
            .into(binding.imageView)

        Glide.with(this)
            .asBitmap()
            .load(imageUrl)
            .into(object : com.bumptech.glide.request.target.CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                    sourceBitmap = resource
                    canvasBitmap = Bitmap.createBitmap(
                        resource.width,
                        resource.height,
                        resource.config ?: Bitmap.Config.ARGB_8888
                    )
                    canvas = Canvas(canvasBitmap!!)
                    canvas?.drawBitmap(resource, 0f, 0f, null)
                    binding.imageView.setImageBitmap(canvasBitmap)
                    binding.progressBar.visibility = View.GONE
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Handle Glide cleanup if needed
                }
            })
    }

    private fun drawPointer(x: Float, y: Float) {
        // Clear the canvas
        canvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        canvas?.drawBitmap(sourceBitmap!!, 0f, 0f, null) // Redraw the base image

        // Get the pin drawable
        val pinDrawable = getDrawable(R.drawable.pointer111) ?: return

        // Scale the drawable to a suitable size
        val scaledWidth = 150 // Adjust the width (in pixels) to the desired size
        val scaledHeight = 150 // Adjust the height (in pixels) to the desired size
        pinDrawable.setBounds(
            (x - scaledWidth / 2).toInt(),
            (y - scaledHeight).toInt(),
            (x + scaledWidth / 2).toInt(),
            y.toInt()
        )

        // Draw the scaled drawable
        pinDrawable.draw(canvas!!)

        // Update the ImageView
        binding.imageView.setImageBitmap(canvasBitmap)
    }

    private fun mapTouchToBitmap(touchX: Float, touchY: Float): Pair<Float?, Float?> {
        val imageView = binding.imageView
        val drawable = imageView.drawable ?: return Pair(null, null)
        val matrixValues = FloatArray(9)
        imageView.imageMatrix.getValues(matrixValues)

        // Get the scaled dimensions of the drawable
        val scaleX = matrixValues[Matrix.MSCALE_X]
        val scaleY = matrixValues[Matrix.MSCALE_Y]

        val drawableWidth = drawable.intrinsicWidth * scaleX
        val drawableHeight = drawable.intrinsicHeight * scaleY

        // Calculate offsets
        val offsetX = (imageView.width - drawableWidth) / 2
        val offsetY = (imageView.height - drawableHeight) / 2

        // Map touch coordinates to the drawable coordinates
        val mappedX = (touchX - offsetX) / scaleX
        val mappedY = (touchY - offsetY) / scaleY

        if (mappedX < 0 || mappedX > drawable.intrinsicWidth || mappedY < 0 || mappedY > drawable.intrinsicHeight) {
            return Pair(null, null) // Out of bounds
        }

        return Pair(mappedX, mappedY)
    }

    private fun getTouchedRegion(touchX: Float, touchY: Float): String {
        val drawable = binding.imageView.drawable ?: return "Unknown"

        // Define the center of the image
        val centerX = drawable.intrinsicWidth / 2f
        val centerY = drawable.intrinsicHeight / 2f

        // Calculate the distance from the center
        val dx = touchX - centerX
        val dy = touchY - centerY
        val distance = sqrt(dx.pow(2) + dy.pow(2))

        // Check if the touch is in the gold region (inner circle)
        if (distance <= 230f) {
            return "Gold"
        }

        // Check if the touch is outside the outer circle
        if (distance > 500f) {
            return "Out of bounds"
        }

        // Calculate the angle in degrees
        val angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
        val normalizedAngle = if (angle < 0) angle + 360 else angle

        // Determine the region in the ring (120° sectors for Red, White, Blue)
        return when {
            normalizedAngle in 91f..210f -> "Red"
            normalizedAngle in 211f..331f -> "White"
            normalizedAngle in 0f..90f || normalizedAngle in 331f..360f -> "Blue"
            else -> "Unknown"
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
