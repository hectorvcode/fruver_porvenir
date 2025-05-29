package com.example.online_store.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object ImageUtils {

    /**
     * Crea un archivo temporal para la foto
     */
    @Throws(IOException::class)
    fun createImageFile(context: Context): File {
        // Crear un nombre único para la imagen
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "PRODUCT_${timeStamp}_"

        // Directorio donde se guardará la imagen
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(
            imageFileName, /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    }

    /**
     * Obtiene la URI para el archivo usando FileProvider
     */
    fun getUriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    /**
     * Comprime y rota la imagen si es necesario
     */
    fun compressAndRotateImage(imagePath: String, maxWidth: Int = 800, maxHeight: Int = 600): Bitmap? {
        try {
            // Leer la imagen
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(imagePath, options)

            // Calcular el factor de escalado
            val scaleFactor = calculateInSampleSize(options, maxWidth, maxHeight)

            // Decodificar la imagen con el factor de escalado
            options.inJustDecodeBounds = false
            options.inSampleSize = scaleFactor
            val bitmap = BitmapFactory.decodeFile(imagePath, options)

            // Rotar la imagen si es necesario
            return rotateImageIfRequired(bitmap, imagePath)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Calcula el factor de escalado para redimensionar la imagen
     */
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    /**
     * Rota la imagen según la información EXIF
     */
    private fun rotateImageIfRequired(bitmap: Bitmap, imagePath: String): Bitmap {
        try {
            val exif = ExifInterface(imagePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            return when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
                else -> bitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return bitmap
        }
    }

    /**
     * Rota una imagen por los grados especificados
     */
    private fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    /**
     * Guarda un bitmap comprimido en un archivo
     */
    fun saveBitmapToFile(bitmap: Bitmap, file: File, quality: Int = 85): Boolean {
        return try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Elimina un archivo de imagen
     */
    fun deleteImageFile(imagePath: String?): Boolean {
        return if (!imagePath.isNullOrEmpty()) {
            try {
                val file = File(imagePath)
                file.delete()
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        } else {
            false
        }
    }

    /**
     * Verifica si un archivo de imagen existe
     */
    fun imageFileExists(imagePath: String?): Boolean {
        return if (!imagePath.isNullOrEmpty()) {
            File(imagePath).exists()
        } else {
            false
        }
    }
}