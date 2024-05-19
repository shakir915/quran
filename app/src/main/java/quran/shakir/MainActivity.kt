package quran.shakir

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import quran.shakir.ui.theme.QuranTheme
import java.text.NumberFormat
import java.util.Locale


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuranTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121316)) {

                }
            }
        }

        lifecycleScope.launch {
            val chaptersList = arrayListOf<String>()
            withContext(Dispatchers.IO) {
                val chapters = JSONObject(assets.open("info.json").bufferedReader().use { it.readText() }).getJSONArray("chapters")
                for (i in 0 until chapters.length()) {
                    chaptersList.add(chapters.getJSONObject(i).getString("arabicname").replace("سُوْرَةُ", "").trim())
                }


            }
            setContent {
                QuranTheme {
                    // A surface container using the 'background' color from the theme
                    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121316)) {
                        LazyColumn(modifier = Modifier) {
                            items(chaptersList.size) { index ->
                                Row(modifier = Modifier
                                    .defaultMinSize(minWidth = 150.dp)
                                    .clickable {
                                        startActivity(Intent(this@MainActivity, VersActivty::class.java).apply {
                                            putExtra("chapterNumber", index + 1)
                                            putExtra("chapterName", chaptersList.get(index))
                                        })
                                    }) {
                                    Text((index + 1).toString(), modifier = Modifier.padding(8.dp), color = Color.White)
                                    Text(chaptersList.get(index), modifier = Modifier.padding(8.dp), color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        checkDeepLink(intent)
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        checkDeepLink(intent)
    }


    fun checkDeepLink(intent: Intent) {
        try {
            val action: String? = intent?.action
            val data: Uri? = intent?.data
            if (data.toString().contains("qml://enable_mail")) {
                pref.edit().putBoolean("enable_mail", true).commit()
                intent.data = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

}

class VersActivty : ComponentActivity() {

    override fun onPause() {
        super.onPause()


    }

    lateinit var chapterNumber: String

    val nf: NumberFormat = NumberFormat.getInstance(Locale.forLanguageTag("ar"))


    suspend fun getShareText() = withContext(Dispatchers.IO) {
        val chaptersList = arrayListOf<String>()
        val chapters = JSONObject(assets.open("info.json").bufferedReader().use { it.readText() }).getJSONArray("chapters")
        for (i in 0 until chapters.length()) {
            chaptersList.add(chapters.getJSONObject(i).getString("arabicname").replace("سُوْرَةُ", "").trim())
        }

        val a = pref.getString("selected", null)?.split("|")
        var s = ""
        a?.forEach {
            val chapterNumber = it.split(":").get(0).toInt()
            val ayaIndices = it.split(":").get(1).split(",").map { it.toInt() }
            val chapterName = chaptersList.get(chapterNumber - 1)
            val at = getAyaths(chapterNumber.toString())


            var prev = -222

            s += "\u202A\n-----------------------------\n"
            s += "Quran:$chapterNumber ($chapterName)"
            s += "\n----------------------------- \u202A"
            ayaIndices.sorted().distinct().forEachIndexed { index, ayaIndxe ->
                if (prev == -222 || prev == ayaIndxe - 1 || index == ayaIndices.last()) {
                    s += "\n\n"
                } else {
                    s += "\u202A \n\n....\n\n \u202A"
                }


                s += "\u202B${at.first.get(ayaIndxe)}  {${java.lang.String.valueOf(nf.format(ayaIndxe + 1))}}\u202B"
                s += "\n\n"
                s += "\u202A" + (ayaIndxe + 1).toString() + ". " + at.second.get(ayaIndxe) + "\u202A"


                prev = ayaIndxe
            }

            s += "\n\n"

        }


        return@withContext s


    }


    suspend fun getAyaths(chapterNumber: String) = withContext(Dispatchers.IO) {
        val ayaths = arrayListOf<String>()
        val ayathsTrans = arrayListOf<String>()
        var start = -1
        var end = -1
        assets.open("ara-quranuthmani.txt").bufferedReader().use { it.readText() }.lines().forEachIndexed { index, it ->
            val splits = it.split("|")
            if (splits[0] == chapterNumber) {
                ayaths.add(splits[2])
                if (start == -1) {
                    start = index
                }
                end = index


            }
        }
        assets.open("malayalam_kunhi.txt").bufferedReader().use { it.readText() }.lines().subList(start, end + 1).forEachIndexed { index, s ->
            ayathsTrans.add(s)
        }

        return@withContext ayaths to ayathsTrans
    }

    @OptIn(ExperimentalLayoutApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chapterNumber = intent.getIntExtra("chapterNumber", 1).toString()
        val chapterName = intent.getStringExtra("chapterName") ?: ""


        setContent {
            QuranTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121316)) {

                }
            }
        }

        lifecycleScope.launch {
            val at = getAyaths(chapterNumber)
            val ayaths = at.first
            val ayathsTrans = at.second
            var otherSuraAyas = 0


            val roboto_medium = FontFamily(
                Font(R.font.roboto_medium, FontWeight.Medium),

                )


            val kfgqpc_uthmanic_script_hafs_regular = FontFamily(
                Font(R.font.kfgqpc_uthmanic_script_hafs_regular, FontWeight.Medium),

                )





            var initSelected=  try {
                val a = pref.getString("selected", null)?.split("|")
                val f = a?.find { it.startsWith("$chapterNumber:") }
                otherSuraAyas = 0
                a?.forEach {
                    if (it != f) {
                        otherSuraAyas += it.split(":").get(1).split(",").size
                    }
                }
                (f?.split(":")?.getOrNull(1)?.split(",")?.map { it.toInt() }?: emptyList())
            } catch (e: Exception) {
                emptyList()
            }

            setContent {

                val scope = rememberCoroutineScope()


                val selected = remember{ mutableStateListOf<Int>() }



                val clipboardManager = LocalClipboardManager.current

                var selectionEnabledByLongPress by remember { mutableStateOf(false) }



                QuranTheme {
                    // A surface container using the 'background' color from the theme
                    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121316)) {
                        Column {
                            FlowRow {

//                                Image(
//                                    painter = painterResource(id = R.drawable.baseline_library_add_24),
//                                    contentDescription = "add To Clipboard",
////                                    textAlign = TextAlign.Right,
//                                    modifier = Modifier
//                                        .padding(8.dp)
//                                        .clickable {
//                                            if (selected.isNotEmpty()) {
//
//                                                var prev = -222
//                                                var s = ""
//                                                s += "\u202A\n-----------------------------\n"
//                                                s += "Quran:$chapterNumber ($chapterName)"
//                                                s += "\n-----------------------------\n\n \u202A"
//                                                selected
//                                                    .sorted()
//                                                    .distinct()
//                                                    .forEachIndexed { index, ayaIndxe ->
//
//                                                        if (prev == -222 || prev == ayaIndxe - 1 || index == selected.size - 1) {
//                                                            s += "\n\n"
//                                                        } else {
//                                                            s += "\u202A \n\n....\n\n \u202A"
//                                                        }
//
//
//                                                        s += "\u202B${ayaths.get(ayaIndxe)}  {${java.lang.String.valueOf(nf.format(ayaIndxe + 1))}}\u202B"
//                                                        s += "\n\n"
//                                                        s += "\u202A" + (ayaIndxe + 1).toString() + ". " + ayathsTrans.get(ayaIndxe) + "\u202A"
//
//
//                                                        prev = ayaIndxe
//                                                    }
//
//                                                s += "\n\n"
//
//                                                try {
//                                                    File(filesDir, "tempClip").appendText(s)
//                                                } catch (e: Exception) {
//                                                    File(filesDir, "tempClip").writeText(s)
//                                                }
//
//
//                                                clipboardManager.setText(AnnotatedString(File(filesDir, "tempClip").readText()))
//
//
//                                            }
//                                            tempClip = File(filesDir, "tempClip").readText()
//                                        },
////                                    fontFamily = kfgqpc_uthmanic_script_hafs_regular,
//                                )


                                if (pref.getBoolean("enable_mail", false)) Text(
                                    text = "mail", textAlign = TextAlign.Right, modifier = Modifier
                                        .padding(8.dp)
                                        .clickable {

                                            println(" clickable clicked ")

                                            scope.launch {


                                                try {


                                                    fun openEmailApp(
                                                        subject: String, recipients: Array<String>, message: String, cc: Array<String>? = null, bcc: Array<String>? = null, packageName: String
                                                    ) {
                                                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                                                            data = Uri.parse("mailto:")
                                                            putExtra(Intent.EXTRA_EMAIL, recipients)
                                                            putExtra(Intent.EXTRA_SUBJECT, subject)
                                                            putExtra(Intent.EXTRA_TEXT, message)
                                                            cc?.let { putExtra(Intent.EXTRA_CC, it) }
                                                            bcc?.let { putExtra(Intent.EXTRA_BCC, it) }
                                                        }

                                                        val emailAppIntent = Intent.createChooser(intent, "Choose an Email Client")
                                                        emailAppIntent.setPackage(packageName)

                                                        try {
                                                            startActivity(emailAppIntent)
                                                        } catch (e: Exception) {
                                                            emailAppIntent.setPackage(null)
                                                            startActivity(emailAppIntent)
                                                        }
                                                    }


                                                    var s = getShareText()

                                                    val splits = s
                                                        .split("-----------------------------")
                                                        .filter {
                                                            it.contains("Quran:")
                                                        }
                                                        .map {
                                                            it
                                                                .trimIndent()
                                                                .trim()
                                                                .trimIndent()
                                                                .trim()
                                                        }


                                                    val recipients = arrayOf("nazmiyapallikkara@gmail.com")
                                                    val cc = arrayOf<String>()
                                                    val bcc = arrayOf("nazmiyapallikkara03@gmail.com", "nazmiyapallikkara1@gmail.com")
                                                    val subject = splits.joinToString(",")
                                                    val message = s

                                                    openEmailApp(subject, recipients, message, cc, bcc, "com.google.android.gm")
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            }

                                        }, fontFamily = kfgqpc_uthmanic_script_hafs_regular, color = Color.White
                                )

                                Image(
                                    painter = painterResource(id = R.drawable.baseline_share_24),
                                    contentDescription = "Share Clipboard Content",
//                                    textAlign = TextAlign.Right,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .clickable {


                                            if (selected.size + otherSuraAyas == 0) {
                                                Toast
                                                    .makeText(this@VersActivty, "Long-press to select an aya ", Toast.LENGTH_SHORT)
                                                    .show()
                                            } else {

                                                scope.launch {
                                                    var s = getShareText()
                                                    val sendIntent = Intent()
                                                    sendIntent.action = Intent.ACTION_SEND
                                                    sendIntent.putExtra(Intent.EXTRA_TEXT, s)
                                                    sendIntent.type = "text/plain"

                                                    val shareIntent = Intent.createChooser(sendIntent, null)
                                                    startActivity(shareIntent)

                                                }


                                            }


                                        },
                                    //fontFamily = kfgqpc_uthmanic_script_hafs_regular,
                                )

                                Text(
                                    (otherSuraAyas + selected.size).toString(), color = Color.White

                                )

                                Image(
                                    painter = painterResource(id = R.drawable.baseline_delete_24),
                                    contentDescription = "Clear Clipboard ",
//                                    textAlign = TextAlign.Right,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .clickable {
                                            otherSuraAyas = 0
                                            selected.clear()
                                            pref
                                                .edit()
                                                .remove("selected")
                                                .apply()
                                        },
//                                    fontFamily = kfgqpc_uthmanic_script_hafs_regular,
                                )


                            }
                            LazyColumn(
                                modifier = Modifier.fillMaxSize()


                            ) {
                                items(ayaths.size) { index ->


                                    // /* "\u06DD"*/,
                                    Column(modifier = Modifier
                                        .pointerInput(Unit) {


                                            fun tapppp() {
                                                println("a ${selected.joinToString()}")
                                                if (selectionEnabledByLongPress) {
                                                    if (selected.contains(index)) {
                                                        selected.remove(index)
                                                    } else {
                                                        selected.add(index)
                                                    }

                                                    var s = pref.getString("selected", "") ?: ""
                                                    var c = pref
                                                        .getString("selected", null)
                                                        ?.split("|")
                                                        ?.find { it.startsWith("$chapterNumber:") }
                                                    val cNew = "$chapterNumber:${selected.joinToString(",")}"
                                                    if (c != null) {
                                                        s = s.replace(c, cNew)
                                                    } else {
                                                        if (s.isBlank()) s = cNew
                                                        else s = s + "|" + cNew
                                                    }

                                                    println("a ${selected.joinToString()}")
                                                    println("sssssssssss $s")
                                                    pref
                                                        .edit()
                                                        .putString("selected", s)
                                                        .commit()
                                                }


                                            }

                                            detectTapGestures(onLongPress = {
                                                selectionEnabledByLongPress = true
                                                tapppp()
                                            }, onTap = {
                                                tapppp()
                                            }

                                            )
                                        }
//                                            .clickable {
//
//                                            }
                                        .background(
                                            if (selectionEnabledByLongPress && selected.contains(index)) Color.White.copy(alpha = .1f)
                                            else Color(0)
                                        )


                                    ) {
                                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                            Text(
                                                text = "${ayaths.get(index)} ${java.lang.String.valueOf(nf.format(index + 1))}",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp)
                                                    .align(Alignment.End),
                                                fontFamily = kfgqpc_uthmanic_script_hafs_regular,
                                                fontSize = 20.sp,
                                                color = Color.White


                                            )
                                        }
                                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                            Text(
                                                (index + 1).toString() + ". " + ayathsTrans.get(index), modifier = Modifier.padding(8.dp), color = Color.White
                                            )
                                        }

                                    }
                                }


                            }
                        }


                    }
                }
            }
        }


    }
}


@Composable
fun DrawScrollableView(content: @Composable () -> Unit, modifier: Modifier) {
    AndroidView(modifier = modifier, factory = {
        val scrollView = ScrollView(it)
        val layout = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        scrollView.layoutParams = layout
        scrollView.isVerticalFadingEdgeEnabled = true
        scrollView.isScrollbarFadingEnabled = false
        scrollView.addView(ComposeView(it).apply {
            setContent {
                content()
            }
        })
        val linearLayout = LinearLayout(it)
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        linearLayout.addView(scrollView)
        linearLayout
    })
}




