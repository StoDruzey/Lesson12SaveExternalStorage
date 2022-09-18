package com.example.lesson12saveexternalstorage

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.lesson12saveexternalstorage.databinding.FragmentPrefsBinding
import java.io.IOException
import java.util.*

class PrefsFragment() : Fragment() {
    private var _binding: FragmentPrefsBinding? = null
    private val binding get() = requireNotNull(_binding)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        updateAdapter()
    }

    private val uploadImageLauncher = registerForActivityResult( //create launcher for uploading photos from camera
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        checkNotNull(bitmap)
        savePhoto(bitmap)
        binding.imageView.setImageBitmap(bitmap) //got an image in bitmat format
        updateAdapter()
    }

    private val adapter = ImageAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentPrefsBinding.inflate(inflater, container, false)
            .also { _binding = it }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )

        with(binding) {
            recyclerView.adapter = adapter
            button.setOnClickListener {
                uploadImageLauncher.launch(null)
            }
        }
        updateAdapter()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun savePhoto(bitmap: Bitmap) {
        if (requireContext().hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

        } else {

        }
    }

    private fun updateAdapter() {
        val photos = loadPhotos()
        adapter.submitList(photos)
    }

    private fun loadPhotos(): List<Image> {
        val imagesCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME)

        return requireContext().contentResolver.query(
            imagesCollection,
            projection,
            null,
            null,
            "${MediaStore.Images.Media.DISPLAY_NAME} ASC"
        ). use { cursor ->
            cursor ?: return emptyList()

            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)

            generateSequence { cursor.takeIf { it.moveToNext() } }
                .map {
                    val id = cursor.getLong(idColumn)
                    val fileName = cursor.getString(displayNameColumn)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    Image(fileName, contentUri)
                }
                .toList()
        }
    }
}
fun Context.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(
        this, permission
    ) == PackageManager.PERMISSION_GRANTED
}