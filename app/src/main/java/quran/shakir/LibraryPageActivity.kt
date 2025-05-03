package quran.shakir

import kotlinx.serialization.Serializable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import quran.shakir.ui.theme.QuranTheme
import android.database.sqlite.SQLiteDatabase
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.core.text.HtmlCompat
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.io.File


val client = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        })
    }
}




suspend fun fetchData(content: String?): String {
    val apiKey = "AIzaSyB0H16kaQ9_F13n_95xB91-UXSx_LtwsR0" // Replace with your actual API key
    val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey"
    val escaped = content
        ?.replace("\\", "\\\\")
        ?.replace("\"", "\\\"")
        ?.replace("\n", "\\n")
    val response: HttpResponse = client.post(endpoint) {
        contentType(ContentType.Application.Json)
        setBody(
            """
                {
    "contents": [
      {
        "parts": [
          {
            "text": "Translate the following to Malayalam. Only give the translation without any title or explanation:\n\n$escaped"
          }
        ]
      }
    ]
  }
            """.trimIndent()
        )
    }

    var resp= response.bodyAsText()
    println(resp)

  return  JSONObject(resp).getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")

//    // Extract text: candidates[0].content.parts[0].text
//    return parsed.candidates.firstOrNull()
//        ?.content?.parts?.firstOrNull()
//        ?.text ?: "No translation found"
}


class LibraryPageActivity : ComponentActivity() {





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuranTheme {
                var refresh by remember { mutableStateOf(0) }
                var content by remember { mutableStateOf<String?>(null) }
                val scope = rememberCoroutineScope()


                LaunchedEffect(refresh) {

                    val db = SQLiteDatabase.openDatabase(
                        File(intent.getStringExtra("file")).absolutePath,
                        null,
                        SQLiteDatabase.OPEN_READONLY
                    )
                    val cursor = db.rawQuery("SELECT content FROM page LIMIT 1 OFFSET ${refresh}", null)
                    if (cursor.moveToFirst()) {
                        do {
                             content = cursor.getString(0)  // Index 0 since only one column
                            println(content)
                        } while (cursor.moveToNext())
                    }
                    cursor.close()
                    db.close()



                }

                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121316)) {
                    Column {
                        Text(
                            text = "${refresh + 1}",
                            color = Color.White,
                            modifier = Modifier
                                .background(Color(0xFFE91E63))
                                .padding(8.dp)
                                .clickable {
                                    scope.launch {
                                     withContext(Dispatchers.IO){
                                         content = fetchData(content)
                                     }
                                    }
                                }

                        )
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                            Text(
                                text = htmlToAnnotatedString(content ?: ""),
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(16.dp)
                                    .clickable {
                                        refresh++
                                    }
                            )
                        }
                    }


                }
            }
        }





    }



}


fun htmlToAnnotatedString(html: String): AnnotatedString {
    val spanned = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
    return buildAnnotatedString {
        append(spanned.toString()) // No spans, plain text only
    }
}




@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>,
    val usageMetadata: UsageMetadata? = null,
    val modelVersion: String? = null
)

@Serializable
data class Candidate(
    val content: Content,
    val finishReason: String? = null,
    val avgLogprobs: Double? = null
)

@Serializable
data class Content(
    val parts: List<Part>,
    val role: String? = null
)

@Serializable
data class Part(
    val text: String
)

@Serializable
data class UsageMetadata(
    val promptTokenCount: Int,
    val candidatesTokenCount: Int,
    val totalTokenCount: Int,
    val promptTokensDetails: List<TokenDetail>? = null,
    val candidatesTokensDetails: List<TokenDetail>? = null
)

@Serializable
data class TokenDetail(
    val modality: String,
    val tokenCount: Int
)




