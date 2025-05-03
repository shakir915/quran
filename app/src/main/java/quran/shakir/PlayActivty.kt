package quran.shakir


import androidx.compose.ui.platform.LocalContext

import android.content.Context
import android.content.Intent
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalLifecycleOwner
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import quran.shakir.ui.theme.QuranTheme
import java.io.File
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


        val chaptersList = arrayListOf<String>()
        val chapters = JSONObject(
            assets.open("info.json").bufferedReader()
                .use { it.readText() }).getJSONArray("chapters")
        for (i in 0 until chapters.length()) {
            chaptersList.add(
                chapters.getJSONObject(i).getString("arabicname").replace("سُوْرَةُ", "").trim()
            )
        }

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


            var file_edit_cut =

                if (chapterNumber == "0")
                    File(filesDir, "edited_cut_aya_data_SPECIAL")
                else
                    File(filesDir, "edited_cut_aya_data_$chapterNumber")


            var ayaths: ArrayList<String>
            var ayathsTrans: ArrayList<String>
            var ayathNumbers: ArrayList<Int>
            var isEnd: ArrayList<Boolean>
            var chanpterNumbers: ArrayList<Int>

            try {
                var text = file_edit_cut.readText()

                try {
                    var i=0
                    while ( File(getExternalFilesDirs(null).first(), "backup_$i").exists()){
                        i++
                    }
                    File(getExternalFilesDirs(null).first(), "backup_$i").writeText(text)
                } catch (e: Exception) {
                  e.printStackTrace()
                }


//                text= text.replaceFirst("||||","||-||")
//                text= text.replaceFirst("||||","--------")
//                text= text.replaceFirst("||||","||-||")
//                text= text.replaceFirst("--------","||||")
//                file_edit_cut.writeText(text)
//                delay(1000)
//                finishAffinity()
//                println("file_edit_cut")
//                println(text)
//                println("file_edit_cut")
//                val sendIntent = Intent()
//                sendIntent.action = Intent.ACTION_SEND
//                sendIntent.putExtra(Intent.EXTRA_TEXT, text)
//                sendIntent.type = "text/plain"
//
//                val shareIntent =
//                    Intent.createChooser(sendIntent, null)
//                startActivity(shareIntent)
//
//                return@launch


                if (text.isNullOrBlank()) throw Exception("")
                ayaths = ArrayList(text.split("||||")[0].split("||"))
                if (ayaths.isEmpty()) throw Exception("")
                ayathsTrans = ArrayList(text.split("||||")[1].split("||"))
                ayathNumbers = ArrayList(text.split("||||")[2].split("||").map { it.toInt() })
                isEnd = ArrayList(text.split("||||")[3].split("||").map { it.toBoolean() })
                try {
                    chanpterNumbers =
                        ArrayList(text.split("||||")[4].split("||").map { it.toInt() })
                } catch (e: Exception) {
                    chanpterNumbers = ArrayList(ayaths.map { chapterNumber.toInt() })
                }
                println("bshdsbjdjasd Read from edited/save")
            } catch (e: Exception) {
                e.printStackTrace()
                val at = getAyaths(chapterNumber)
                ayaths = at.first
                ayathsTrans = at.second
                ayathNumbers = ArrayList(at.second.mapIndexed { index, s -> index + 1 })
                isEnd = ArrayList(at.second.mapIndexed { index, s -> true })
                chanpterNumbers = ArrayList(ayaths.map { chapterNumber.toInt() })
                println("bshdsbjdjasd Read from db")

            }


//            var otherSuraAyas = 0


            val roboto_medium = FontFamily(
                Font(R.font.roboto_medium, FontWeight.Medium),

                )


            val kfgqpc_uthmanic_script_hafs_regular = FontFamily(
                Font(R.font.kfgqpc_uthmanic_script_hafs_regular, FontWeight.Medium),

                )


//            var initSelected = try {
//                val a = pref.getString("selected", null)?.split("|")
//                val f = a?.find { it.startsWith("$chapterNumber:") }
//                otherSuraAyas = 0
//                a?.forEach {
//                    if (it != f) {
//                        otherSuraAyas += it.split(":").get(1).split(",").size
//                    }
//                }
//                (f?.split(":")?.getOrNull(1)?.split(",")?.map { it.toInt() } ?: emptyList())
//            } catch (e: Exception) {
//                emptyList()
//            }

            setContent {
                val lifecycleOwner = LocalLifecycleOwner.current
                var forceUIRefresh by remember { mutableStateOf(0) }

                val scrollState = rememberScrollState()
                val scope = rememberCoroutineScope()

                var uri by remember { mutableStateOf<Uri?>(null) }
                var uriBG by remember { mutableStateOf<Uri?>(null) }
                val context = LocalContext.current

                val mediaPlayer = remember { MediaPlayer() }
                var isPlaying by remember { mutableStateOf(false) }
                var isEdit by remember { mutableStateOf(false) }
                var isRedBG by remember { mutableStateOf(false) }
                var mediaPlayerBG: MediaPlayer? by remember { mutableStateOf(null) }

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



                 val launcherBG = rememberLauncherForActivityResult1(
                    contract = ActivityResultContracts.OpenDocument()
                ) { uri1: Uri? ->
                    getContentResolver().takePersistableUriPermission(
                        uri1!!,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );

                     if(uri!=null)
                         uriBG = uri1


                }






                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_RESUME -> {
                            forceUIRefresh++
                            }
                            else -> {
                                // Log.d(TAG, "Lifecycle event: $event")
                            }
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
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
                var editChapterVerse by remember { mutableStateOf("") }
                var editDelete by remember { mutableStateOf("D-e-l-e-t-e A-d-d") }

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
                    forceUIRefresh.let {
                        Surface(
                            color = Color(0xFF121316),
                            modifier = Modifier
                                .fillMaxSize()
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
                                        painter = painterResource(id = R.drawable.baseline_settings_24),
                                        contentDescription = "settings",
                                        modifier = Modifier
                                            .align(alignment = Alignment.CenterVertically)
                                            .padding(12.dp)
                                            .alpha(alpha = 1f)
                                            .clickable {
                                                startActivity(
                                                    Intent(
                                                        this@PlayActivty,
                                                        SettingsActivity::class.java
                                                    )
                                                )

                                            },
                                    )


                                    Image(
                                        painter = painterResource(id = R.drawable.baseline_play_circle_24),
                                        contentDescription = "Share Clipboard Content",
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .clickable {

                                                try {
                                                    if (!mediaPlayer.isPlaying) {
                                                        timeStamps.clear()
                                                        mediaPlayer.start()
                                                    }
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            },
                                    )



                                    Image(
                                        painter = painterResource(id = if (!isEdit) R.drawable.baseline_edit_24 else R.drawable.baseline_save_24),
                                        contentDescription = "Share Clipboard Content",
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .clickable {

                                                if (!isEdit && indexPlus1 > 0) {


                                                    val index = indexPlus1 - 1
                                                    val an = ayathNumbers[index]
                                                    val cn = chanpterNumbers[index]




                                                    editAya = ayaths
                                                        .filterIndexed { index, s ->
                                                            ayathNumbers[index] == an && chanpterNumbers[index] == cn
                                                        }
                                                        .joinToString("\n\n")



                                                    editTrans =
                                                        ayathsTrans
                                                            .filterIndexed { index, s ->
                                                                ayathNumbers[index] == an && chanpterNumbers[index] == cn
                                                            }
                                                            .joinToString("\n\n")


                                                    editChapterVerse = "" + cn + " " + an








                                                    isEdit = !isEdit

                                                } else if (isEdit && indexPlus1 > 0) {

                                                    val index = indexPlus1 - 1
                                                    var an = ayathNumbers[index]
                                                    var cn = chanpterNumbers[index]
                                                    var fi = ayathNumbers.indices.first { index ->
                                                        ayathNumbers[index] == an && chanpterNumbers[index] == cn
                                                    }
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


                                                    val newChapterNumber = editChapterVerse
                                                        .trimIndent()
                                                        .split(" ")[0]

                                                    val newVersNumber = editChapterVerse
                                                        .trimIndent()
                                                        .split(" ")[1]



                                                    if (newAya.isEmpty() || newTran.isEmpty() || newAya.contains(
                                                            ""
                                                        ) || newTran.contains("") || newAya.size != newTran.size
                                                    ) {
                                                        throw Exception("newAya.size != newTran.size")
                                                    }




                                                    while (ayathNumbers.indices.any { index ->
                                                            ayathNumbers[index] == an && chanpterNumbers[index] == cn
                                                        }) {
                                                        var i = ayathNumbers.indices.filter { index ->
                                                            ayathNumbers[index] == an && chanpterNumbers[index] == cn
                                                        }.first()
                                                        ayathNumbers.removeAt(i)
                                                        ayaths.removeAt(i)
                                                        ayathsTrans.removeAt(i)
                                                        isEnd.removeAt(i)
                                                        chanpterNumbers.removeAt(i)
                                                    }
                                                    ayaths.addAll(fi, newAya)
                                                    ayathsTrans.addAll(fi, newTran)
                                                    ayathNumbers.addAll(
                                                        fi,
                                                        newAya.map { newVersNumber.toInt() })
                                                    isEnd.addAll(fi, newAya.map { it == newAya.last() })
                                                    chanpterNumbers.addAll(
                                                        fi,
                                                        newAya.map { newChapterNumber.toInt() })



                                                    file_edit_cut.writeText(
                                                        ayaths.joinToString("||")
                                                                + "||||"
                                                                + ayathsTrans.joinToString("||")
                                                                + "||||"
                                                                + ayathNumbers.joinToString("||")
                                                                + "||||"
                                                                + isEnd.joinToString("||")
                                                                + "||||"
                                                                + chanpterNumbers.joinToString("||")


                                                    )

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
                                                    isEdit = false
                                                },
                                        )




                                    if (!isEdit) {
                                        Image(
                                            painter = painterResource(id = R.drawable.baseline_volume_down_24),
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
                                            painter = painterResource(id = R.drawable.baseline_image_search_24),
                                            contentDescription = "",
                                            modifier = Modifier
                                                .padding(12.dp)
                                                .clickable {
                                                    uriBG=null
                                                    launcherBG.launch(
                                                        arrayOf(
                                                            "image/*",
                                                            "video/*",
                                                            "audio/*"
                                                        )
                                                    ) // MIME types for media

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
                                                .background(
                                                    if (!isRedBG)
                                                        Color.White.copy(alpha = .1f)
                                                    else
                                                        Color.Red.copy(alpha = 1f)


                                                )
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




                                Box {








                                    uriBG?.let { uri ->
                                        AndroidView(
                                            factory = { ctx ->
                                                val textureView = TextureView(ctx)

                                                textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                                                    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                                                        val surface = android.view.Surface(surface)

                                                        mediaPlayerBG = MediaPlayer().apply {
                                                            setDataSource(ctx, uri)
                                                            setSurface(surface)
                                                            isLooping = true
                                                            setVolume(0f, 0f) // Mute audio
                                                            setOnPreparedListener { mp ->
                                                                adjustVideoScaling(textureView, mp.videoWidth, mp.videoHeight)
                                                                mp.start()
                                                            }
                                                            prepareAsync()
                                                        }
                                                    }

                                                    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
                                                    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                                                        mediaPlayerBG?.release()
                                                        mediaPlayerBG = null
                                                        return true
                                                    }

                                                    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
                                                }

                                                textureView
                                            },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }




                                    Column(

                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clickable(
                                                indication = null,
                                                interactionSource = remember { MutableInteractionSource() },
                                                onClick = {
                                                    if (!isEdit) {
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
                                                }

                                            ),
                                    ) {

                                        if (isEdit && indexPlus1 > 0) {

                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .verticalScroll(scrollState)
                                            ) {
                                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                                    BasicTextField(
                                                        cursorBrush = SolidColor(Color.Cyan),
                                                        textStyle = TextStyle(
                                                            color = Color.White,
                                                            textAlign = TextAlign.Start,
                                                            fontFamily = kfgqpc_uthmanic_script_hafs_regular,
                                                            fontSize = (pref.getInt(
                                                                "font_size_arabic",
                                                                20
                                                            )).sp,
                                                        ),
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .background(Color.White.copy(alpha = .1f))
                                                            .padding(start = 8.dp, end = 8.dp),
                                                        keyboardActions = KeyboardActions {

                                                        },


                                                        value = editAya,
                                                        onValueChange = { newText ->
                                                            editAya = newText
                                                        })
                                                }

                                                Spacer(modifier = Modifier.height(10.dp))

                                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                                    BasicTextField(
                                                        cursorBrush = SolidColor(Color.Cyan),
                                                        textStyle = TextStyle(
                                                            color = Color.White,
                                                            textAlign = TextAlign.Start,
                                                            fontSize = pref.getInt(
                                                                "font_size_malayalam",
                                                                16
                                                            ).sp
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

                                                Spacer(modifier = Modifier.height(10.dp))

                                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                                    BasicTextField(
                                                        cursorBrush = SolidColor(Color.Cyan),
                                                        textStyle = TextStyle(
                                                            color = Color.White,
                                                            textAlign = TextAlign.Start,
                                                            fontSize = pref.getInt(
                                                                "font_size_malayalam",
                                                                16
                                                            ).sp
                                                        ),
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .background(Color.White.copy(alpha = .1f))
                                                            .padding(start = 8.dp, end = 8.dp),
                                                        keyboardActions = KeyboardActions {

                                                        },

                                                        value = editChapterVerse,
                                                        onValueChange = { newText ->
                                                            editChapterVerse = newText
                                                        })
                                                }

                                                Spacer(modifier = Modifier.height(10.dp))

                                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                                    BasicTextField(
                                                        cursorBrush = SolidColor(Color.Cyan),
                                                        textStyle = TextStyle(
                                                            color = Color.White,
                                                            textAlign = TextAlign.Start,
                                                            fontSize = pref.getInt(
                                                                "font_size_malayalam",
                                                                16
                                                            ).sp
                                                        ),
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .background(Color.White.copy(alpha = .1f))
                                                            .padding(start = 8.dp, end = 8.dp),
                                                        keyboardActions = KeyboardActions {

                                                        },

                                                        value = editDelete,
                                                        onValueChange = { newText ->
                                                            editDelete = newText
                                                            if (editDelete.lowercase() == "Delete".lowercase()) {


                                                                val index = indexPlus1 - 1
                                                                var an = ayathNumbers[index]
                                                                var cn = chanpterNumbers[index]




                                                                while (ayathNumbers.indices.any { index ->
                                                                        ayathNumbers[index] == an && chanpterNumbers[index] == cn
                                                                    }) {
                                                                    try {
                                                                        var i =
                                                                            ayathNumbers.indices.filter { index ->
                                                                                ayathNumbers[index] == an && chanpterNumbers[index] == cn
                                                                            }.first()
                                                                        ayathNumbers.removeAt(i)
                                                                        ayaths.removeAt(i)
                                                                        ayathsTrans.removeAt(i)
                                                                        isEnd.removeAt(i)
                                                                        chanpterNumbers.removeAt(i)
                                                                    } catch (e: Exception) {
                                                                        e.printStackTrace()
                                                                    }
                                                                }

                                                                file_edit_cut.writeText(
                                                                    ayaths.joinToString("||")
                                                                            + "||||"
                                                                            + ayathsTrans.joinToString("||")
                                                                            + "||||"
                                                                            + ayathNumbers.joinToString(
                                                                        "||"
                                                                    )
                                                                            + "||||"
                                                                            + isEnd.joinToString("||")
                                                                            + "||||"
                                                                            + chanpterNumbers.joinToString(
                                                                        "||"
                                                                    )


                                                                )

                                                                if (ayaths.getOrNull(indexPlus1 - 1) == null)
                                                                    indexPlus1 =
                                                                        ayaths.indexOfLast { true } + 1



                                                                isEdit = !isEdit
                                                                editDelete = "D-e-l-e-t-e A-d-d"

                                                            }

                                                            if (editDelete.lowercase() == "Add".lowercase()) {


                                                                var fi = indexPlus1
                                                                val newAya = arrayListOf(" ")
                                                                val newTran = arrayListOf(" ")


                                                                val newChapterNumber =
                                                                    System.currentTimeMillis().toInt()

                                                                val newVersNumber =
                                                                    System.currentTimeMillis().toInt()

                                                                ayaths.addAll(fi, newAya)
                                                                ayathsTrans.addAll(fi, newTran)
                                                                ayathNumbers.addAll(
                                                                    fi,
                                                                    newAya.map { newVersNumber.toInt() })
                                                                isEnd.addAll(
                                                                    fi,
                                                                    newAya.map { it == newAya.last() })
                                                                chanpterNumbers.addAll(
                                                                    fi,
                                                                    newAya.map { newChapterNumber.toInt() })



                                                                file_edit_cut.writeText(
                                                                    ayaths.joinToString("||")
                                                                            + "||||"
                                                                            + ayathsTrans.joinToString("||")
                                                                            + "||||"
                                                                            + ayathNumbers.joinToString(
                                                                        "||"
                                                                    )
                                                                            + "||||"
                                                                            + isEnd.joinToString("||")
                                                                            + "||||"
                                                                            + chanpterNumbers.joinToString(
                                                                        "||"
                                                                    )


                                                                )

                                                                if (ayaths.getOrNull(indexPlus1 - 1) == null)
                                                                    indexPlus1 =
                                                                        ayaths.indexOfLast { true } + 1



                                                                isEdit = !isEdit
                                                                editDelete = "D-e-l-e-t-e A-d-d"

                                                            }
                                                            if (editDelete.lowercase() == "Original".lowercase()) {
                                                                lifecycleScope.launch {
                                                                    var fi = indexPlus1
                                                                    val newAya = arrayListOf(
                                                                        getAyaths(
                                                                            editChapterVerse.split(
                                                                                " "
                                                                            )[0]
                                                                        ).first.get(
                                                                            editChapterVerse.split(
                                                                                " "
                                                                            )[1].toInt() - 1
                                                                        )
                                                                    )
                                                                    val newTran = arrayListOf(
                                                                        getAyaths(
                                                                            editChapterVerse.split(
                                                                                " "
                                                                            )[0]
                                                                        ).second.get(
                                                                            editChapterVerse.split(
                                                                                " "
                                                                            )[1].toInt() - 1
                                                                        )
                                                                    )


                                                                    val newChapterNumber =
                                                                        editChapterVerse.split(" ")[0].toInt()

                                                                    val newVersNumber =
                                                                        editChapterVerse.split(" ")[1].toInt()

                                                                    ayaths.addAll(fi, newAya)
                                                                    ayathsTrans.addAll(fi, newTran)
                                                                    ayathNumbers.addAll(
                                                                        fi,
                                                                        newAya.map { newVersNumber.toInt() })
                                                                    isEnd.addAll(
                                                                        fi,
                                                                        newAya.map { it == newAya.last() })
                                                                    chanpterNumbers.addAll(
                                                                        fi,
                                                                        newAya.map { newChapterNumber.toInt() })



                                                                    file_edit_cut.writeText(
                                                                        ayaths.joinToString("||")
                                                                                + "||||"
                                                                                + ayathsTrans.joinToString(
                                                                            "||"
                                                                        )
                                                                                + "||||"
                                                                                + ayathNumbers.joinToString(
                                                                            "||"
                                                                        )
                                                                                + "||||"
                                                                                + isEnd.joinToString("||")
                                                                                + "||||"
                                                                                + chanpterNumbers.joinToString(
                                                                            "||"
                                                                        )


                                                                    )





                                                                    isEdit = !isEdit
                                                                    editDelete = "D-e-l-e-t-e A-d-d"
                                                                }


                                                            }


                                                        })
                                                }


                                            }
                                        } else {

                                            Spacer(modifier = Modifier.height(pref.getInt("space_above_sura_name",20).dp))


                                            val index = indexPlus1 - 1


                                            var numText = try {
                                                if (ayathNumbers.get(index) > 0)
                                                    chanpterNumbers.get(index)
                                                        .toString() + ":" + ayathNumbers.get(index)
                                                        .toString() + "    " + chaptersList.get(
                                                        chanpterNumbers.get(index) - 1
                                                    )
                                                else ""

                                            } catch (e: Exception) {
                                                null
                                            }

                                            if (numText != null)
                                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                                    Text(
                                                        numText,
                                                        modifier = Modifier
                                                            .align(Alignment.CenterHorizontally),
                                                        color = Color.White,
                                                        textAlign = TextAlign.Center,
                                                        fontSize = pref.getInt("font_size_sura",10).sp,
                                                        style = TextStyle(
                                                            shadow = Shadow(
                                                                color = Color.Black,
                                                                offset = Offset(pref.getFloat("shadow_offset",2f), pref.getFloat("shadow_offset",2f)),
                                                                blurRadius = pref.getFloat("shadow_blurRadius",4f)
                                                            )

                                                        )
                                                    )
                                                }

                                            Spacer(modifier = Modifier.height(pref.getInt("space_above_aya",30).dp))
                                            if (indexPlus1 == 0) {
                                                Text(
                                                    text =
                                                        if (chapterNumber == "0") "أعوذ بالله السميع العليم من الشيطان الرجيم \n من همزه  ونفخه  ونفثه"
                                                        else if (chapterNumber == "1") "أعوذُ بِٱللَّهِ مِنَ ٱلشَّيۡطَٰنِ ٱلرَّجِيمِ"
                                                        else
                                                            bismi,
                                                    modifier = Modifier
                                                        .absolutePadding(left =pref.getInt("left_padding",8).dp , right = pref.getInt("right_padding",8).dp)
                                                        .align(Alignment.CenterHorizontally),
                                                    fontFamily = kfgqpc_uthmanic_script_hafs_regular,
                                                    fontSize = (pref.getInt("font_size_arabic", 20)).sp,
                                                    color = Color.White,
                                                    textAlign = TextAlign.Center,
                                                    style = TextStyle(
                                                        shadow = Shadow(
                                                            color = Color.Black,
                                                            offset = Offset(2f, 2f),
                                                            blurRadius = 4f
                                                        )

                                                    )

                                                )
                                            } else {


                                                var number =
                                                    if ((ayathNumbers.getOrNull(index)
                                                            ?: 0) <= 0
                                                    ) "" else if (isEnd[index]) "\u00A0${
                                                        java.lang.String.valueOf(
                                                            nf.format(ayathNumbers.get(index))
                                                        )
                                                    }" else ""






                                                if (ayaths.get(index).contains(" _") && !isRedBG)
                                                    isRedBG = true
                                                else if (isRedBG) {
                                                    isRedBG = false
                                                }

                                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                                    Text(
                                                        text = "${
                                                            ayaths.get(index).replace(" _", "")
                                                        }$number",
                                                        modifier = Modifier
                                                            .absolutePadding(left =pref.getInt("left_padding",8).dp , right = pref.getInt("right_padding",8).dp)

                                                            .align(Alignment.CenterHorizontally),
                                                        fontFamily = kfgqpc_uthmanic_script_hafs_regular,
                                                        fontSize = (pref.getInt(
                                                            "font_size_arabic",
                                                            20
                                                        )).sp,
                                                        lineHeight = 1.425.em,
                                                        textAlign = TextAlign.Center,
                                                        color = Color.White,
                                                        style = TextStyle(
                                                            shadow = Shadow(
                                                                color = Color.Black,
                                                                offset = Offset(2f, 2f),
                                                                blurRadius = 4f
                                                            )

                                                        )


                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(pref.getInt("space_above_aya",pref.getInt("space_above_malayalam",0)).dp))
                                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                                    Text(
                                                        ayathsTrans.get(index),
                                                        modifier = Modifier
                                                            .absolutePadding(left =pref.getInt("left_padding",8).dp , right = pref.getInt("right_padding",8).dp)
                                                            .align(Alignment.CenterHorizontally),
                                                        color = Color.White,
                                                        lineHeight = 1.4.em,
                                                        textAlign = TextAlign.Center,
                                                        fontSize = pref.getInt(
                                                            "font_size_malayalam",
                                                            16
                                                        ).sp,
                                                        style = TextStyle(
                                                            shadow = Shadow(
                                                                color = Color.Black,
                                                                offset = Offset(2f, 2f),
                                                                blurRadius = 4f
                                                            )

                                                        )
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


    }


}


private fun adjustVideoScaling(textureView: TextureView, videoWidth: Int, videoHeight: Int) {
    val viewWidth = textureView.width
    val viewHeight = textureView.height

    val scaleX = viewWidth.toFloat() / videoWidth
    val scaleY = viewHeight.toFloat() / videoHeight
    val scale = maxOf(scaleX, scaleY)

    val scaledWidth = scale * videoWidth
    val scaledHeight = scale * videoHeight

    val pivotX = viewWidth / 2f
    val pivotY = viewHeight / 2f

    val matrix = Matrix()
    matrix.setScale(
        scaledWidth / viewWidth,
        scaledHeight / viewHeight,
        pivotX,
        pivotY
    )

    textureView.setTransform(matrix)
}




