package com.appstock.app_stock.data.service

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.appstock.app_stock.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Servicio de subida de imágenes usando ImgBB (https://api.imgbb.com).
 * Plan gratuito sin límites de almacenamiento.
 *
 * La API key se lee de BuildConfig.IMGBB_API_KEY, generada desde
 * `local.properties` (propiedad `imgbb.api.key`).
 * Ver `local.properties.ejemplo`.
 */
object ImageUploadService {

    private val IMGBB_API_KEY: String
        get() = BuildConfig.IMGBB_API_KEY.ifBlank { System.getenv("IMGBB_API_KEY").orEmpty() }
    private const val IMGBB_UPLOAD_URL = "https://api.imgbb.com/1/upload"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Sube una imagen a ImgBB y devuelve la URL pública.
     * @return Result.success(url) en caso de éxito, Result.failure(exception) si falla.
     */
    suspend fun uploadImage(context: Context, imageUri: Uri): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                // Leer los bytes de la imagen desde el Uri
                val imageBytes = context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
                    ?: return@withContext Result.failure(Exception("No se pudo leer la imagen seleccionada"))

                if (imageBytes.isEmpty()) {
                    return@withContext Result.failure(Exception("La imagen está vacía"))
                }

                // Construir el cuerpo multipart
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "image",
                        "product_${System.currentTimeMillis()}.jpg",
                        imageBytes.toRequestBody("image/*".toMediaType())
                    )
                    .build()

                val request = Request.Builder()
                    .url("$IMGBB_UPLOAD_URL?key=$IMGBB_API_KEY")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()

                    if (!response.isSuccessful || responseBody == null) {
                        return@withContext Result.failure(
                            Exception("Error del servidor (${response.code}): ${response.message}")
                        )
                    }

                    val json = JSONObject(responseBody)
                    if (!json.optBoolean("success", false)) {
                        val errorMsg = json.optJSONObject("error")?.optString("message") ?: "Error desconocido"
                        return@withContext Result.failure(Exception("ImgBB: $errorMsg"))
                    }

                    // Extraer la URL de la imagen subida
                    Result.success(
                        json.getJSONObject("data").getString("url")
                    )
                }
                response

            } catch (e: Exception) {
                Result.failure(Exception("Error al subir imagen: ${e.message}"))
            }
        }

    /** Verifica si la API key ya fue configurada */
    fun isConfigured(): Boolean = IMGBB_API_KEY.isNotBlank() && IMGBB_API_KEY != "TU_API_KEY_AQUI"
}
