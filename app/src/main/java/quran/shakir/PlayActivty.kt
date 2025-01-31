package quran.shakir


import androidx.compose.ui.platform.LocalContext

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract.Colors
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import quran.shakir.ui.theme.QuranTheme
import quran.shakir.ui.theme.QuranThemeFullScreen
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale
import androidx.activity.compose.rememberLauncherForActivityResult as rememberLauncherForActivityResult1

class PlayActivty : ComponentActivity() {
    var lastHitTime = 0L

    override fun onPause() {
        super.onPause()


    }

    lateinit var chapterNumber: String

    val nf: NumberFormat = NumberFormat.getInstance(Locale.forLanguageTag("ar"))


    suspend fun getAyaths(chapterNumber: String) = withContext(Dispatchers.IO) {
        val ayaths = arrayListOf<String>()
        val ayathsTrans = arrayListOf<String>()
        var start = -1
        var end = -1
        assets.open("ara-quranuthmani.txt").bufferedReader().use { it.readText() }.lines()
            .forEachIndexed { index, it ->
                val splits = it.split("|")
                if (splits[0] == chapterNumber) {
                    ayaths.add(splits[2])
                    if (start == -1) {
                        start = index
                    }
                    end = index


                }
            }
        assets.open("malayalam_kunhi.txt").bufferedReader().use { it.readText() }.lines()
            .subList(start, end + 1).forEachIndexed { index, s ->
                ayathsTrans.add(s)
            }

        return@withContext ayaths to ayathsTrans
    }

    @OptIn(ExperimentalLayoutApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        chapterNumber = intent.getIntExtra("chapterNumber", 1).toString()
        val chapterName = intent.getStringExtra("chapterName") ?: ""

        try {
            filesDir.listFiles()?.forEach {
                println("${it.name} ${it.readText()}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        var file = File(filesDir, chapterNumber + "_" + System.currentTimeMillis())
        var timeStamps = arrayListOf<String>()


        println(
            "intent EXTRAS " +
                    intent.extras?.keySet()?.map {
                        "$it:${intent.extras?.get(it)}"
                    }?.joinToString("\n")
        )


        setContent {
            QuranTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121316)) {


                }
            }
        }

        lifecycleScope.launch {

            val gradientBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF121316), // Start color
                    Color(0xFF232425), // Mid color
                    Color(0xFF343637)  // End color
                )
            )


            val bismi = getAyaths("1").first.first()
            val at = getAyaths(chapterNumber)

            var file_edit_cut = File(filesDir, "edited_cut_aya_data_$chapterNumber")


            var ayaths: ArrayList<String>
            var ayathsTrans: ArrayList<String>
            var ayathNumbers: ArrayList<Int>
            var isEnd: ArrayList<Boolean>

            try {
                val text = file_edit_cut.readText()
                ayaths = ArrayList(text.split("||||")[0].split("||"))
                ayathsTrans = ArrayList(text.split("||||")[1].split("||"))
                ayathNumbers = ArrayList(text.split("||||")[2].split("||").map { it.toInt() })
                isEnd = ArrayList(text.split("||||")[3].split("||").map { it.toBoolean() })
                if (text.isNullOrBlank()) throw Exception("")
                if (ayaths.isEmpty()) throw Exception("")
                println("bshdsbjdjasd Read from edited/save")
            } catch (e: Exception) {
                ayaths = at.first
                ayathsTrans = at.second
                ayathNumbers = ArrayList(at.second.mapIndexed { index, s -> index + 1 })
                isEnd = ArrayList(at.second.mapIndexed { index, s -> true })
                println("bshdsbjdjasd Read from db")

            }


            var otherSuraAyas = 0


            val roboto_medium = FontFamily(
                Font(R.font.roboto_medium, FontWeight.Medium),

                )


            val kfgqpc_uthmanic_script_hafs_regular = FontFamily(
                Font(R.font.kfgqpc_uthmanic_script_hafs_regular, FontWeight.Medium),

                )


            var initSelected = try {
                val a = pref.getString("selected", null)?.split("|")
                val f = a?.find { it.startsWith("$chapterNumber:") }
                otherSuraAyas = 0
                a?.forEach {
                    if (it != f) {
                        otherSuraAyas += it.split(":").get(1).split(",").size
                    }
                }
                (f?.split(":")?.getOrNull(1)?.split(",")?.map { it.toInt() } ?: emptyList())
            } catch (e: Exception) {
                emptyList()
            }

            setContent {

                val scrollState = rememberScrollState()
                val scope = rememberCoroutineScope()

                var uri by remember { mutableStateOf<Uri?>(null) }
                val context = LocalContext.current

                val mediaPlayer = remember { MediaPlayer() }
                var isPlaying by remember { mutableStateOf(false) }
                var isEdit by remember { mutableStateOf(false) }

                // ActivityResultLauncher for the document picker
                val launcher = rememberLauncherForActivityResult1(
                    contract = ActivityResultContracts.OpenDocument()
                ) { uri1: Uri? ->
                    getContentResolver().takePersistableUriPermission(
                        uri1!!,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );

                    uri = uri1
                    println("mediaPlayer 0  ${uri}")
                    if (uri != null) {
                        try {
                            scope.launch {
                                val audioManager =
                                    context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

                                audioManager.requestAudioFocus(
                                    null,
                                    AudioManager.STREAM_MUSIC,
                                    AudioManager.AUDIOFOCUS_GAIN
                                )

                                mediaPlayer.reset()
                                mediaPlayer.setDataSource(context, uri!!)
                                mediaPlayer.prepare()

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    mediaPlayer.setOnMediaTimeDiscontinuityListener { mp, mts ->
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                            println("what ${mts.anchorSystemNanoTime}")
                                        }
                                        return@setOnMediaTimeDiscontinuityListener
                                    }
                                }
                                mediaPlayer.setOnInfoListener { mp, what, extra ->
                                    println("what $what")
                                    return@setOnInfoListener true
                                }
                                println("mediaPlayer ${mediaPlayer.isPlaying}")
                            }

                        } catch (e: Exception) {

                            e.printStackTrace()
                            println("mediaPlayer2 ")
                        }
                    }

                }



                DisposableEffect("") {


                    onDispose {
                        try {
                            mediaPlayer.release()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }



                    }
                }


                val lazyListState = rememberLazyListState()


                val selected = remember { mutableStateListOf<Int>() }

                var editAya by remember { mutableStateOf("") }
                var editTrans by remember { mutableStateOf("") }
                var indexPlus1 by remember { mutableStateOf(0) }
                var hideTopBar by remember { mutableStateOf(false) }


                val clipboardManager = LocalClipboardManager.current

                var selectionEnabledByLongPress by remember { mutableStateOf(false) }

                LaunchedEffect(key1 = "start") {
                    // Scroll to the desired item index
                    scope.launch {
                        lazyListState.scrollToItem(intent.getIntExtra("ScrollToAyaNumber", 0))
                    }
                }

                QuranTheme {


                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(gradientBrush)
                    ) {
                        Column {

                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(color = Color.White.copy(alpha = .05f))
                                ) {



                                    Image(
                                        painter = painterResource(id = if (!isEdit) R.drawable.baseline_edit_24 else R.drawable.baseline_save_24),
                                        contentDescription = "Share Clipboard Content",
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .clickable {

                                                if (!isEdit && indexPlus1 > 0) {


                                                    val index = indexPlus1 - 1
                                                    val an=  ayathNumbers[index]




                                                    editAya = ayaths
                                                        .filterIndexed { index, s ->
                                                            ayathNumbers[index]==an
                                                        }
                                                        .joinToString("\n\n")



                                                    editTrans =
                                                        ayathsTrans
                                                            .filterIndexed { index, s ->
                                                                ayathNumbers[index]==an
                                                            }
                                                            .joinToString("\n\n")

                                                    isEdit = !isEdit

                                                } else if (isEdit && indexPlus1 > 0) {

                                                    val index = indexPlus1 - 1
                                                    var an = ayathNumbers[index]
                                                    var fi = ayathNumbers.indexOfFirst { it == an }
                                                    val newAya = editAya
                                                        .trimIndent()
                                                        .split("\n")
                                                        .map { it.trimIndent() }
                                                        .filter { it.isNotBlank() }
                                                    val newTran = editTrans
                                                        .trimIndent()
                                                        .split("\n")
                                                        .map { it.trimIndent() }
                                                        .filter { it.isNotBlank() }
                                                    if (newAya.isEmpty()||newAya.size != newTran.size) {
                                                        throw Exception("newAya.size != newTran.size")
                                                    }

                                                    while (ayathNumbers.contains(an)) {
                                                        var i = ayathNumbers.indexOfFirst { it == an }
                                                        ayathNumbers.removeAt(i)
                                                        ayaths.removeAt(i)
                                                        ayathsTrans.removeAt(i)
                                                        isEnd.removeAt(i)
                                                    }
                                                    ayaths.addAll(fi,newAya)
                                                    ayathsTrans.addAll(fi,newTran)
                                                    ayathNumbers.addAll(fi,newAya.map { an })
                                                    isEnd.addAll(fi,newAya.map { it==newAya.last() })


                                                    try {

                                                        file_edit_cut.writeText(
                                                            ayaths.joinToString("||")
                                                                    + "||||"
                                                                    + ayathsTrans.joinToString("||")
                                                                    + "||||"
                                                                    + ayathNumbers.joinToString("||")
                                                                    + "||||"
                                                                    + isEnd.joinToString("||")
                                                        )
                                                        println("bshdsbjdjasd  wrote")
                                                    } catch (e: Exception) {
                                                        e.printStackTrace()
                                                    }

                                                    isEdit = !isEdit

                                                }


                                            },
                                    )
                                    if (isEdit)
                                        Image(
                                        painter = painterResource(id = R.drawable.baseline_close_24),
                                        contentDescription = "Share Clipboard Content",
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .clickable {
                                                    isEdit=false
                                            },
                                    )




                                    if (!isEdit) {
                                        Image(
                                            painter = painterResource(id = R.drawable.baseline_image_search_24),
                                            contentDescription = "Share Clipboard Content",
                                            modifier = Modifier
                                                .padding(12.dp)
                                                .clickable {
                                                    launcher.launch(
                                                        arrayOf(
                                                            "image/*",
                                                            "video/*",
                                                            "audio/*"
                                                        )
                                                    ) // MIME types for media
                                                    hideTopBar = true

                                                },
                                        )

                                        Image(
                                            painter = painterResource(id = R.drawable.baseline_navigate_next_24),
                                            contentDescription = "Share Clipboard Content",
                                            modifier = Modifier
                                                .rotate(180f)
                                                .padding(12.dp)
                                                .clickable {
                                                    if (indexPlus1 > 0)
                                                        indexPlus1--
                                                },
                                        )

                                        BasicTextField(
                                            cursorBrush = SolidColor(Color.Cyan),
                                            textStyle = TextStyle(
                                                color = Color.White,
                                                textAlign = TextAlign.Start,
                                            ),
                                            modifier = Modifier
                                                .width(50.dp)
                                                .background(Color.White.copy(alpha = .1f))
                                                .padding(start = 8.dp, end = 8.dp),
                                            maxLines = 1,
                                            keyboardActions = KeyboardActions {

                                            },

                                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                            value = if (indexPlus1 <= 0) "" else indexPlus1.toString(),
                                            onValueChange = { newText ->
                                                println("bshdsbjdjasdonValueChange")
                                                try {
                                                    if (newText.isBlank() || newText.trim()
                                                            .toInt() <= 0
                                                    )
                                                        indexPlus1 = 0

                                                    if (ayaths.getOrNull(
                                                            newText.trim().toInt() - 1
                                                        ) != null
                                                    )
                                                        indexPlus1 = newText.trim().toInt()
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            })


                                        Image(
                                            painter = painterResource(id = R.drawable.baseline_navigate_next_24),
                                            contentDescription = "Share Clipboard Content",
                                            modifier = Modifier
                                                .padding(12.dp)
                                                .clickable {
                                                    if (ayaths.getOrNull(indexPlus1) != null)
                                                        indexPlus1++
                                                },
                                        )


                                    }
                                }




                            Column(

                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(start = 16.dp, end = 16.dp)
                                    .align(alignment = Alignment.CenterHorizontally)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                        onClick = {
                                            if (ayaths.getOrNull(indexPlus1) != null) {
                                                if (System.currentTimeMillis() - lastHitTime > 300) {
                                                    try {

                                                        if (!mediaPlayer.isPlaying) {
                                                            timeStamps.clear()
                                                            mediaPlayer.start()
                                                        }
                                                    } catch (e: Exception) {
                                                        e.printStackTrace()
                                                    }
                                                    indexPlus1++
                                                    timeStamps.add("${indexPlus1} ${mediaPlayer.currentPosition}")
                                                    lastHitTime = System.currentTimeMillis()
                                                }


                                            } else {
                                                file.writeText(timeStamps.joinToString("\n"))
                                                finish()
                                                try {
                                                    mediaPlayer.release()
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            }
                                        }

                                    ),
                            ) {

                                if (isEdit && indexPlus1 > 0) {

                                    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
                                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                            BasicTextField(
                                                cursorBrush = SolidColor(Color.Cyan),
                                                textStyle = TextStyle(
                                                    color = Color.White,
                                                    textAlign = TextAlign.Start,
                                                    fontFamily = kfgqpc_uthmanic_script_hafs_regular,
                                                    fontSize = (pref.getInt("font_size_arabic", 20)).sp,
                                                ),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color.White.copy(alpha = .1f))
                                                    .padding(start = 8.dp, end = 8.dp),
                                                keyboardActions = KeyboardActions {

                                                },


                                                value = editAya,
                                                onValueChange = { newText ->
                                                    editAya=newText
                                                })
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                            BasicTextField(
                                                cursorBrush = SolidColor(Color.Cyan),
                                                textStyle = TextStyle(
                                                    color = Color.White,
                                                    textAlign = TextAlign.Start,
                                                    fontSize = pref.getInt("font_size_malayalam", 16).sp
                                                ),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color.White.copy(alpha = .1f))
                                                    .padding(start = 8.dp, end = 8.dp),
                                                keyboardActions = KeyboardActions {

                                                },

                                                value = editTrans,
                                                onValueChange = { newText ->
                                                    editTrans = newText
                                                })
                                        }


                                    }
                                } else {

                                    Spacer(modifier = Modifier.weight(2f))
                                    if (indexPlus1 == 0) {
                                        Text(
                                            text =
                                            if (chapterNumber == "1") "أعوذُ بِٱللَّهِ مِنَ ٱلشَّيۡطَٰنِ ٱلرَّجِيمِ" else
                                                bismi,
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .align(Alignment.CenterHorizontally),
                                            fontFamily = kfgqpc_uthmanic_script_hafs_regular,
                                            fontSize = 11.sp,
                                            color = Color.White,
                                            textAlign = TextAlign.Center


                                        )
                                    } else {

                                        val index = indexPlus1 - 1
                                        var number=if (isEnd[index]) "\u00A0${
                                            java.lang.String.valueOf(
                                               nf.format( ayathNumbers.get(index))
                                            )
                                        }" else ""


                                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                            Text(
                                                text = "${ayaths.get(index)}$number",
                                                modifier = Modifier
                                                    .padding(8.dp)
                                                    .align(Alignment.CenterHorizontally),
                                                fontFamily = kfgqpc_uthmanic_script_hafs_regular,
                                                fontSize = (pref.getInt("font_size_arabic", 20)).sp,
                                                lineHeight = 1.425.em,
                                                textAlign = TextAlign.Center,
                                                color = Color.White


                                            )
                                        }
                                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                            Text(
                                                ayathsTrans.get(index),
                                                modifier = Modifier
                                                    .padding(8.dp)
                                                    .align(Alignment.CenterHorizontally),
                                                color = Color.White,
                                                lineHeight = 1.4.em,
                                                textAlign = TextAlign.Center,
                                                fontSize = pref.getInt("font_size_malayalam", 16).sp
                                            )
                                        }

                                    }

                                    Spacer(modifier = Modifier.weight(6f))

                                }
                            }


                        }


                    }
                }
            }
        }


    }


}