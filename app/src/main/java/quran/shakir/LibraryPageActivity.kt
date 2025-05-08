package quran.shakir

import android.content.ContentValues
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
import android.text.Spanned
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.io.File



class LibraryPageActivity : ComponentActivity() {


    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 60_000  // Total request timeout (15 seconds)
            connectTimeoutMillis = 10_000  // Time to establish connection
            socketTimeoutMillis = 60_000   // Time waiting for data
        }
    }




    private var isOnLoading=false
    suspend fun translate(content: String?, pageNumber: Int, file: File,force:Boolean=false, retry:Boolean=true): String {
        var resp: String="No Response"


        var dbFile=File(appInstance.filesDir,"trans_malayalam_db_v2")
        if (!dbFile.exists()) {
            dbFile.createNewFile() // Create the file if it doesn't exist
        }
        val db = SQLiteDatabase.openDatabase(dbFile.absolutePath,
            null,
            SQLiteDatabase.OPEN_READWRITE
        )

        // Create table
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS trans_malayalam_table (fileName TEXT, page INTEGER, content TEXT)")
            println("Table trans_malayalam_table created successfully.")
        } catch (e: Exception) {
            e.printStackTrace()
        }


        if(!force) {
            val cursor = db.rawQuery(
                "SELECT content FROM trans_malayalam_table WHERE fileName = ? AND page = ?",
                arrayOf(file.name, pageNumber.toString())
            )
            if (cursor.moveToFirst()) {
                var content = cursor.getString(cursor.getColumnIndexOrThrow("content"))
                if (content.isNotBlank()) {
                    cursor.close()
                    return content
                }
            }
        }

        isOnLoading=true
        try {
            val apiKey = "AIzaSyB0H16kaQ9_F13n_95xB91-UXSx_LtwsR0" // Replace with your actual API key
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey"
            val escaped = content
                ?.replace("\\", "\\\\")
                ?.replace("\"", "\\\"")
                ?.replace("\n", "\\n")
            println("escaped $escaped")
            val response: HttpResponse = client.post(endpoint) {
                contentType(ContentType.Application.Json)
                setBody(
                    """
                    {
        "contents": [
          {
            "parts": [
              {
                "text": "Translate the following to Malayalam. Only give the translation in easily readable Malayalam  without any title or explanation, keep html styling in translation also :\n\n$escaped"
              }
            ]
          }
        ]
      }
                """.trimIndent()
                )
            }


            resp = response.bodyAsText()
            println(resp)

            var s=  JSONObject(resp).getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
            if(s.isNotBlank()){
                val values = ContentValues().apply {
                    put("fileName", file.name)
                    put("page", pageNumber)
                    put("content", s)
                }
                val rowsAffected = db.update(
                    "trans_malayalam_table",
                    values,
                    "fileName = ? AND page = ?",
                    arrayOf(file.name, pageNumber.toString())
                )
                println("rowsAffected ${rowsAffected}")
                if (rowsAffected == 0) {
                    val newRowId = db.insert("trans_malayalam_table", null, values)
                    println("newRowId ${newRowId}")
                }




            }
            if(retry)
            GlobalScope.launch {
                delay(3000L)
                if(!isOnLoading){
                    val cursor = db.rawQuery(
                        "SELECT content FROM trans_malayalam_table WHERE fileName = ? AND page = ?",
                        arrayOf(file.name, pageNumber.plus(1).toString())
                    )
                    if (cursor.moveToFirst()) {
                        var content = cursor.getString(cursor.getColumnIndexOrThrow("content"))
                        if (content.isNotBlank()){
                            cursor.close()
                            return@launch
                        }
                    }
                    translate(loadAPage(file,pageNumber+1),pageNumber+1,file,retry = false)

                }
            }

            isOnLoading=false;
            return  s;

        } catch (e: Exception) {
            e.printStackTrace()
            isOnLoading=false;
            return e.message+"\n\n\n"+resp
        }




    }


    fun loadAPage(file: File, pageNumber: Int): String {
        var content=""
        val db = SQLiteDatabase.openDatabase(
            file.absolutePath,
            null,
            SQLiteDatabase.OPEN_READONLY
        )
        val cursor = db.rawQuery("SELECT content FROM page LIMIT 1 OFFSET ${pageNumber}", null)
        if (cursor.moveToFirst()) {
            do {
                content = cursor.getString(0)  // Index 0 since only one column
                println(content)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        pref.edit().putInt("${file.name}_PageNumber",pageNumber).commit()
        return content;
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuranTheme {
                var pageNumber by remember { mutableStateOf(0) }
                var content by remember { mutableStateOf<String?>(null) }
                val scope = rememberCoroutineScope()
                var isLoading by remember { mutableStateOf(false) }
                var isSource by remember { mutableStateOf(false) }
                var isTranslation by remember { mutableStateOf(false) }
                val file = File(intent.getStringExtra("file"));
                val scrollState = rememberScrollState()


                LaunchedEffect(content) {
                    scrollState.scrollTo(0)
                }



                fun nextpage(i:Int=1) {
                    scope.launch {
                        isLoading=true;
                        pageNumber+=i
                        withContext(Dispatchers.IO) {
                            if (isTranslation) {
                                val loadAPage = loadAPage(file,pageNumber)
                                content= translate(loadAPage,pageNumber,file)
                            }else{
                                content=loadAPage(file, pageNumber)
                            }
                        }
                        isLoading=false;
                    }
                }


                LaunchedEffect("") {
                    isLoading=true;
                    content=loadAPage(file, pageNumber)
                    isLoading=false;

                }

                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF1E1F22)) {
                    Column {
                        Row {



                            Text(
                                text = "ML",
                                color = Color.White,
                                modifier = Modifier
                                    .background(Color(0x20E91E63))
                                    .padding(8.dp)
                                    .clickable {
                                        scope.launch {
                                            isLoading=true;
                                            withContext(Dispatchers.IO) {
                                                val loadAPage = loadAPage(file, pageNumber)
                                                content= translate(loadAPage, pageNumber, file)
                                                isTranslation=true;
                                            }
                                            isLoading=false;
                                        }
                                    }

                            )

                            Text(
                                text = "⟳",
                                color = Color.White,
                                fontSize = 20.sp,
                                modifier = Modifier
                                    .background(Color(0x20E91E63))
                                    .padding(8.dp)
                                    .clickable {
                                        scope.launch {
                                            isLoading=true;
                                            withContext(Dispatchers.IO) {
                                                val loadAPage = loadAPage(file, pageNumber)
                                                content= translate(loadAPage, pageNumber, file, force = true)
                                                isTranslation=true;
                                            }
                                            isLoading=false;
                                        }
                                    }

                            )


                            Text(
                                text = "og",
                                color = Color.White,
                                modifier = Modifier
                                    .background(Color(0x20E91E63))
                                    .padding(8.dp)
                                    .clickable {
                                        scope.launch {
                                            isLoading=true;
                                            withContext(Dispatchers.IO) {
                                               content=loadAPage(file, pageNumber)
                                               isTranslation=false;
                                            }
                                            isLoading=false;
                                        }
                                    }

                            )

                            Text(
                                text = "src",
                                color = Color.White,
                                modifier = Modifier
                                    .background(Color(0x20E91E63))
                                    .padding(8.dp)
                                    .clickable {
                                        isSource=!isSource
                                    }

                            )

                            CircularProgressIndicator(modifier = Modifier.alpha(if (isLoading) 1f else 0f))





                            Text(
                                text = "◀",
                                color = Color.White,
                                fontSize = 20.sp,
                                modifier = Modifier
                                    .background(Color(0x20E91E63))
                                    .padding(8.dp)
                                    .clickable {
                                        nextpage(i=-1)
                                    }

                            )
                            Text(
                                text = "${pageNumber + 1}",
                                color = Color.White,
                                modifier = Modifier
                                    .background(Color(0x20E91E63))
                                    .padding(8.dp)

                            )

                            Text(
                                text = "▶",
                                color = Color.White,
                                fontSize = 20.sp,
                                modifier = Modifier
                                    .background(Color(0x20E91E63))
                                    .padding(8.dp)
                                    .clickable {
                                        nextpage()
                                    }

                            )




                        }

                        if(isSource)
                        CompositionLocalProvider(LocalLayoutDirection provides if (isTranslation) LayoutDirection.Ltr else LayoutDirection.Rtl ) {
                            Text(
                                text =  content?:"",
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState)
                                    .clickable {

                                    }
                                    .padding(16.dp)

                            )
                        }
                        else

                        CompositionLocalProvider(LocalLayoutDirection provides if (isTranslation) LayoutDirection.Ltr else LayoutDirection.Rtl ) {
                            Text(
                                text =  htmlToAnnotatedString(content ?: ""),
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState)
                                    .clickable {

                                    }
                                    .padding(16.dp)

                            )
                        }




                    }


                }
            }
        }





    }



}





fun htmlToAnnotatedString(html: String): AnnotatedString {
    println("html")
    println(html)
    val spanned: Spanned = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
    val text = spanned.toString()

    // Regular expression to find spans with data-type="title"
    val titleSpanRegex = Regex("<span\\s+data-type=\"title\"[^>]*>(.*?)</span>", RegexOption.IGNORE_CASE)


    return buildAnnotatedString {
        var lastIndex = 0
        val results = titleSpanRegex.findAll(html) //find all occurrences

        results.forEach { result ->
            val matchRange = result.range
            val titleText = result.groupValues[1] // Get the text within the span
            val startIndex = matchRange.first
            val endIndex = matchRange.last + 1

            // Append the text before the title span with default style
            if (startIndex > lastIndex) {
                append(text.substring(lastIndex, startIndex))
            }

            // Apply heading style to the title text
            pushStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp)) // Example heading style.  Adjust as needed
            append("\n\n") // Add a newline before the heading
            append(titleText)
            append("\n\n") // Add a newline after the heading
            pop() // Reset to default style after the title

            lastIndex = endIndex //update the lastIndex to the end of the matched span
        }

        // Append any remaining text after the last title span
        if (lastIndex < text.length) {
            append(text.substring(lastIndex, text.length))
        }

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




