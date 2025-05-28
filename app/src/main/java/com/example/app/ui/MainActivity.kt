package com.example.app.ui

import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.app.R
import com.example.app.data.Receipt
import com.example.app.viewmodel.ReceiptViewModel
import com.example.app.viewmodel.ReceiptViewModelFactory
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream

class MainActivity : AppCompatActivity() {
    private val vm: ReceiptViewModel by viewModels {
        ReceiptViewModelFactory(applicationContext)
    }

    private lateinit var imageView: ImageView
    private lateinit var takePhotoButton: Button
    private lateinit var editTextAmount: EditText
    private lateinit var editTextDate: EditText
    private lateinit var saveButton: Button
    private lateinit var totalTextView: TextView
    private lateinit var resetButton: Button

    private var tempImageFile: File? = null

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempImageFile != null) {
            imageView.setImageURI(FileProvider.getUriForFile(this, "${packageName}.fileprovider", tempImageFile!!))
        } else {
            tempImageFile = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        takePhotoButton = findViewById(R.id.takePhotoButton)
        editTextAmount = findViewById(R.id.input_amount)
        editTextDate = findViewById(R.id.input_date)
        saveButton = findViewById(R.id.saveButton)
        totalTextView = findViewById(R.id.totalText)
        resetButton = findViewById(R.id.resetButton)

        takePhotoButton.setOnClickListener {
            tempImageFile = createTempImageFile()
            val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", tempImageFile!!)
            takePictureLauncher.launch(uri)
        }

        saveButton.setOnClickListener {
            val amount = editTextAmount.text.toString().toDoubleOrNull()
            val date = editTextDate.text.toString()

            if (amount != null && date.isNotBlank() && tempImageFile != null && tempImageFile!!.exists()) {
                val finalUri = moveImageToGallery(tempImageFile!!, date)

                if (finalUri != null) {
                    vm.add(Receipt(amount = amount, date = date, photoPath = finalUri.toString()))
                    Toast.makeText(this, "Receipt saved", Toast.LENGTH_SHORT).show()

                    editTextAmount.text.clear()
                    editTextDate.text.clear()
                    imageView.setImageDrawable(null)
                    tempImageFile = null
                } else {
                    Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill all fields and take a photo", Toast.LENGTH_SHORT).show()
            }
        }

        resetButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Confirm Reset")
                .setMessage("Are you sure you want to reset all receipts? This cannot be undone.")
                .setPositiveButton("Yes") { _, _ ->
                    vm.reset()
                    Toast.makeText(this, "Receipts cleared", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        vm.total.observe(this) { total ->
            totalTextView.text = "Total: £%.2f".format(total)
        }
    }

    private fun createTempImageFile(): File {
        val tempDir = File(cacheDir, "temp_receipts").apply { mkdirs() }
        return File(tempDir, "temp_photo.jpg")
    }

    private fun moveImageToGallery(sourceFile: File, date: String): Uri? {
        val filename = "receipt_$date.jpg"
        val resolver = contentResolver

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Receipts")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        imageUri?.let {
            try {
                resolver.openOutputStream(it)?.use { outputStream ->
                    FileInputStream(sourceFile).use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }


                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(it, contentValues, null, null)

                // delete the orginal photo
                sourceFile.delete()

                return it
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }

        return null
    }
}
