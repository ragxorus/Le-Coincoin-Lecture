
package com.example.coincoinlecture.data

import android.util.Log
import com.example.coincoinlecture.model.GoogleBookInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class GoogleBooksRepository(
    private val apiKey: String
) {

    suspend fun searchByIsbn(rawIsbn: String): GoogleBookInfo? =
        withContext(Dispatchers.IO) {

            val isbn = normalizeIsbn(rawIsbn)

            if (isbn.length != 10 && isbn.length != 13) {
                return@withContext null
            }

            val encodedKey = URLEncoder.encode(apiKey, "UTF-8")
            val query = URLEncoder.encode("isbn:$isbn", "UTF-8")

            // 1. Recherche du livre par ISBN
            val searchUrl = URL(
                "https://www.googleapis.com/books/v1/volumes" +
                        "?q=$query&maxResults=10&key=$encodedKey"
            )

            val searchJson = requestJson(searchUrl)
                ?: return@withContext null

            val items = searchJson.optJSONArray("items")
                ?: return@withContext null

            // Recherche d'une fiche contenant exactement notre ISBN
            var volumeId: String? = null

            for (i in 0 until items.length()) {

                val item = items.optJSONObject(i) ?: continue

                val volumeInfo = item.optJSONObject("volumeInfo")
                    ?: continue

                val identifiers = volumeInfo.optJSONArray("industryIdentifiers")

                val matchesIsbn = identifiers?.let { ids ->
                    (0 until ids.length()).any { index ->
                        val identifier = ids.optJSONObject(index)
                            ?.optString("identifier")
                            .orEmpty()

                        normalizeIsbn(identifier) == isbn
                    }
                } ?: false

                if (matchesIsbn) {
                    volumeId = item.optString("id")
                        .takeIf { it.isNotBlank() && it != "null" }

                    if (volumeId != null) break
                }
            }

            val id = volumeId
                ?: return@withContext null

            Log.d("GoogleBooks", "Fiche trouvée : $id")

            // 2. Récupération complète de la fiche par son identifiant
            val detailsUrl = URL(
                "https://www.googleapis.com/books/v1/volumes/" +
                        "${URLEncoder.encode(id, "UTF-8")}?key=$encodedKey"
            )

            val detailsJson = requestJson(detailsUrl)
                ?: return@withContext null

            val volumeInfo = detailsJson.optJSONObject("volumeInfo")
                ?: return@withContext null

            // 3. Extraction du titre
            val title = volumeInfo.optString("title")
                .takeIf { it.isNotBlank() && it != "null" }

            // 4. Extraction des auteurs
            val authorsArray = volumeInfo.optJSONArray("authors")

            val author = authorsArray?.let { authors ->
                (0 until authors.length())
                    .mapNotNull { index ->
                        authors.optString(index)
                            .takeIf { it.isNotBlank() && it != "null" }
                    }
                    .joinToString(", ")
                    .takeIf { it.isNotBlank() }
            }

            Log.d("GoogleBooks", "Titre final : $title")
            Log.d("GoogleBooks", "Auteur final : $author")

            if (title == null && author == null) {
                null
            } else {
                GoogleBookInfo(
                    title = title,
                    author = author
                )
            }
        }

    // Effectue une requête HTTP et retourne le JSON reçu
    private fun requestJson(url: URL): JSONObject? {

        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            if (connection.responseCode !in 200..299) {
                Log.e(
                    "GoogleBooks",
                    "Erreur HTTP : ${connection.responseCode}"
                )
                return null
            }

            val response = connection.inputStream
                .bufferedReader()
                .use { it.readText() }

            return JSONObject(response)

        } finally {
            connection.disconnect()
        }
    }

    private fun normalizeIsbn(value: String): String {
        return value
            .filter { it.isDigit() || it == 'X' || it == 'x' }
            .uppercase()
    }
}
