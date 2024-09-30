package quran.shakir

//import com.arthenica.ffmpegkit.FFmpegKit
//import com.arthenica.ffmpegkit.ReturnCode
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
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
import java.util.regex.Matcher
import java.util.regex.Pattern


class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
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

            val ayaths = arrayListOf<String>()
            val ayathNumber = arrayListOf<String>()
            val suraNumber = arrayListOf<String>()
            val ayathsTrans = arrayListOf<String>()


            val showList = arrayListOf<String>()
            val showListSuperArabic = arrayListOf<String>()
            val showListSuperTrans = arrayListOf<String>()
            val chaptersList = arrayListOf<String>()
            val englishList = arrayListOf<String>()
            withContext(Dispatchers.IO) {
                val chapters = JSONObject(
                    assets.open("info.json").bufferedReader()
                        .use { it.readText() }).getJSONArray("chapters")
                for (i in 0 until chapters.length()) {
                    chaptersList.add(
                        chapters.getJSONObject(i).getString("arabicname").replace("سُوْرَةُ", "")
                            .trim()
                    )
                    englishList.add(chapters.getJSONObject(i).getString("name").trim())
                }
                showList.clear()
                showList.addAll(chaptersList)


                assets.open("ara-quranuthmani.txt").bufferedReader().use { it.readText() }.lines()
                    .forEachIndexed { index, it ->
                        ayaths.add(it.split("|")[2])
                        suraNumber.add(it.split("|")[0])
                        ayathNumber.add(it.split("|")[1])

                    }
                assets.open("malayalam_kunhi.txt").bufferedReader().use { it.readText() }.lines()
                    .forEachIndexed { index, s ->
                        ayathsTrans.add(s)
                    }


            }

            val nf: NumberFormat = NumberFormat.getInstance(Locale.forLanguageTag("ar"))
            val kfgqpc_uthmanic_script_hafs_regular = FontFamily(
                Font(R.font.kfgqpc_uthmanic_script_hafs_regular, FontWeight.Medium),

                )





            setContent {


                val focusManager = LocalFocusManager.current
                val customTextSelectionColors = TextSelectionColors(
                    handleColor = Color.Cyan,
                    backgroundColor = Color.Cyan
                )


                val scope = rememberCoroutineScope()

                var searchText by remember { mutableStateOf("") }
                var searchResultCountText by remember { mutableStateOf("Showing 114 Chapters") }
                var refresh by remember { mutableStateOf(0) }
                var superSearch by remember { mutableStateOf(false) }


                fun searchAyath() {
                    searchResultCountText = "Searching..."
                    scope.launch {
                        showList.clear()
                        showListSuperArabic.clear()
                        showListSuperTrans.clear()
                        superSearch = true
                        focusManager.clearFocus()
                        val searchText = searchText.trim()
                        refresh++
                        withContext(Dispatchers.IO) {
                            val nnn = ayaths.mapIndexed { index, s ->
                                if (removeThashkeel(s).contains(searchText, ignoreCase = true))
                                    index
                                else
                                    -1
                            }
                                .plus(
                                    ayathsTrans.mapIndexed { index, s ->
                                        if ((s).contains(searchText, ignoreCase = true))
                                            index
                                        else
                                            -1
                                    }
                                )
                                .filter { it != -1 }
                                .distinct()





                            showList.addAll(
                                nnn.map {
                                    suraNumber.get(it) + ":" + ayathNumber.get(it) + " " +
                                            chaptersList.get(suraNumber.get(it).toInt() - 1)

                                }

                            )

                            showListSuperArabic.addAll(
                                nnn.map {
                                    ayaths.get(it)
                                }
                            )
                            showListSuperTrans.addAll(
                                nnn.map {
                                    ayathsTrans.get(it)
                                }
                            )


                        }
                        searchResultCountText =
                            "${showList.size} Search results for \"$searchText\""
                        refresh++
                    }


                }

                fun searchSura() {

                    superSearch = false
                    val searchText = searchText.trim()
                    if (searchText.isBlank()) {
                        showList.clear()
                        showList.addAll(chaptersList)
                        searchResultCountText = "Showing 114 Chapters"
                    } else {
                        showList.clear()






                        showList.addAll(chaptersList.filter {
                            removeThashkeel(it)
                                .startsWith(
                                    searchText,
                                    ignoreCase = true
                                )
                        }.plus(
                            chaptersList.filter {
                                removeThashkeel(it)
                                    .contains(
                                        searchText,
                                        ignoreCase = true
                                    )
                            }
                        )
                            .plus(
                                englishList
                                    .filter {
                                        it.startsWith(
                                            searchText,
                                            ignoreCase = true
                                        )
                                    }
                                    .map {
                                        englishList.indexOf(it)
                                    }
                                    .map {
                                        chaptersList.get(it)
                                    }
                            )
                            .plus(
                                englishList
                                    .filter {
                                        it.contains(
                                            searchText,
                                            ignoreCase = true
                                        )
                                    }
                                    .distinct()
                                    .map {
                                        englishList.indexOf(it)
                                    }
                                    .map {
                                        chaptersList.get(it)
                                    }
                            )


                            .distinct())
                        searchResultCountText = "Showing Search Result : ${showList.size} Chapters"
                    }


                    refresh++
                }





                QuranTheme {
                    // A surface container using the 'background' color from the theme
                    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121316)) {
                        Column {
                            Row(modifier = Modifier.height(56.dp)) {


                                Box(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(color = Color.White.copy(alpha = .1f))
                                        .align(alignment = Alignment.CenterVertically)
                                        .weight(1f),
                                ) {
                                    Row(modifier = Modifier.align(Alignment.Center)) {

                                        Icon(

                                            Icons.Default.Search,
                                            contentDescription = "Search icon",
                                            modifier = Modifier
                                                .padding(start = 8.dp)
                                                .align(alignment = Alignment.CenterVertically)
                                        )
                                        CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors)
                                        {
                                            BasicTextField(
                                                cursorBrush = SolidColor(Color.Cyan),
                                                textStyle = TextStyle(
                                                    color = Color.White,
                                                    textAlign = TextAlign.Start,
                                                ),
                                                modifier = Modifier
                                                    .align(alignment = Alignment.CenterVertically)
                                                    .fillMaxHeight()
                                                    .fillMaxWidth()
                                                    .wrapContentHeight(align = Alignment.CenterVertically)
                                                    .padding(start = 8.dp, end = 8.dp),
                                                maxLines = 1,
                                                keyboardActions = KeyboardActions {
                                                    searchAyath()
                                                },

                                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                                value = searchText,
                                                onValueChange = { newText ->
                                                    searchText = newText
                                                    searchSura()
                                                })
                                        }
                                    }


                                }
                                Box(
                                    modifier = Modifier
                                        .clickable {
                                            searchAyath()
                                        }
                                        .align(alignment = Alignment.CenterVertically)
                                        .padding(8.dp)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(8.dp)) // Adjust the corner radius as needed
                                        .background(color = Color.White.copy(alpha = .1f))
                                ) {
                                    Text(
                                        "Search\nAya(Arabic) /\ntranslation",
                                        fontSize = 8.sp,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 10.sp,
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .padding(start = 8.dp, end = 8.dp),
                                        color = Color.White
                                    )
                                }

                                Image(
                                    painter = painterResource(id = R.drawable.baseline_share_24),
                                    contentDescription = "Share Clipboard Content",
//                                    textAlign = TextAlign.Right,
                                    modifier = Modifier
                                        .align(alignment = Alignment.CenterVertically)
                                        .padding(12.dp)
                                        .alpha(if (superSearch) 1f else .2f)
                                        .clickable {
                                            scope.launch {
                                                try {
                                                    var s =
                                                        showList
                                                            .mapIndexed { index, s ->
                                                                showList.get(index) + "\n\n" +
                                                                        showListSuperArabic.get(
                                                                            index
                                                                        ) + "\n\n" +
                                                                        showListSuperTrans.get(index) + ""
                                                            }
                                                            .joinToString("\n\n\n")

                                                    val sendIntent = Intent()
                                                    sendIntent.action = Intent.ACTION_SEND
                                                    sendIntent.putExtra(Intent.EXTRA_TEXT, s)
                                                    sendIntent.type = "text/plain"

                                                    val shareIntent =
                                                        Intent.createChooser(sendIntent, null)
                                                    startActivity(shareIntent)
                                                } catch (e: Exception) {
                                                    Toast
                                                        .makeText(
                                                            this@MainActivity,
                                                            "${e.message}",
                                                            Toast.LENGTH_SHORT
                                                        )
                                                        .show()
                                                }

                                            }

                                        },
                                    //fontFamily = kfgqpc_uthmanic_script_hafs_regular,
                                )

                                Image(
                                    painter = painterResource(id = R.drawable.baseline_settings_24),
                                    contentDescription = "settings",
//                                    textAlign = TextAlign.Right,
                                    modifier = Modifier
                                        .align(alignment = Alignment.CenterVertically)
                                        .padding(12.dp)
                                        .alpha(1f)
                                        .clickable {
                                      startActivity(Intent(this@MainActivity, SettingsActivity::class.java))

                                        },
                                    //fontFamily = kfgqpc_uthmanic_script_hafs_regular,
                                )


                            }


                            Text(
                                searchResultCountText,
                                modifier = Modifier.padding(start = 8.dp, end = 8.dp),
                                fontSize = 10.sp,
                                color = Color.White
                            )
                            refresh.let {

                                if (superSearch)
                                    LazyColumn(modifier = Modifier) {
                                        items(showList.size) { index ->
                                            Column(
                                                modifier = Modifier.clickable {


                                                    startActivity(
                                                        Intent(
                                                            this@MainActivity,
                                                            VersActivty::class.java
                                                        ).apply {


                                                            putExtra(
                                                                "chapterNumber",
                                                                showList.get(index)
                                                                    .split(":")[0].toInt()

                                                            )

                                                            putExtra(
                                                                "ScrollToAyaNumber",
                                                                showList.get(index)
                                                                    .split(":")[1].split(" ")[0].toInt()
                                                            )

                                                            putExtra(
                                                                "chapterName",
                                                                chaptersList.get(
                                                                    showList.get(index)
                                                                        .split(":")[0].toInt() - 1
                                                                )
                                                            )
                                                        })
                                                }

                                            ) {
                                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                                    Text(
                                                        showList.get(index),
                                                        modifier = Modifier.padding(8.dp),
                                                        color = Color.White
                                                    )
                                                }
                                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                                    Text(
                                                        text = showListSuperArabic.get(index),
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
                                                        showListSuperTrans.get(index),
                                                        modifier = Modifier.padding(8.dp),
                                                        color = Color.White
                                                    )
                                                }

                                                Box(
                                                    modifier = Modifier.padding(16.dp),
                                                ) {

                                                }

                                            }


                                        }
                                    }
                                else
                                    LazyColumn(modifier = Modifier) {
                                        items(showList.size) { index ->
                                            Row(modifier = Modifier
                                                .defaultMinSize(minWidth = 150.dp)
                                                .clickable {
                                                    if (!superSearch)
                                                        startActivity(
                                                            Intent(
                                                                this@MainActivity,
                                                                VersActivty::class.java
                                                            ).apply {
                                                                putExtra(
                                                                    "chapterNumber",
                                                                    chaptersList.indexOf(
                                                                        showList.get(index)
                                                                    ) + 1
                                                                )
                                                                putExtra(
                                                                    "chapterName",
                                                                    showList.get(index)
                                                                )
                                                            })


                                                }) {


                                                Text(
                                                    if (!superSearch) (chaptersList.indexOf(
                                                        showList.get(
                                                            index
                                                        )
                                                    ) + 1).toString() else " ",
                                                    modifier = Modifier.padding(8.dp),
                                                    color = if (!superSearch) Color.White else Color.Transparent
                                                )
                                                Text(
                                                    showList.get(index),
                                                    modifier = Modifier.padding(8.dp),
                                                    color = Color.White
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


/*class MainActivity2 : ComponentActivity() {

    private lateinit var surface: Surface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        surface = createSurface()
        setContent {
            val animationState = remember { androidx.compose.animation.core.Animatable(0f) }
            LaunchedEffect(Unit) {
                while (isActive) {
                    animationState.animateTo(1f, 1000) // Animates from 0 to 1f over 1 second
                    animationState.animateTo(0f, 1000) // Animates back to 0f over 1 second
                    captureFrame(animationState.value)
                    delay(33) // Capture frame every 33 milliseconds (approx 30 fps)
                }
            }
            SampleAnimation(animationState.value)
        }
    }

    private fun createSurface(): Surface {
        val surfaceView = SurfaceView(this)
        val holder = surfaceView.holder
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                surface = holder.surface
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

            override fun surfaceDestroyed(holder: SurfaceHolder) {}
        })
        return surface
    }

    private fun captureFrame(progress: Float) {
        surface.draw { canvas ->
            val view = LocalView.current
            view.draw(canvas) // Draw the Compose UI onto the surface
        }
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        surface.readPixels(
            pixels = IntArray(view.width * view.height),
            offset = 0,
            stride = view.width,
            x = 0,
            y = 0,
            width = view.width,
            height = view.height
        )
        bitmap.applyAlpha(setAlphaForTransition(progress)) // Apply alpha for fade effect
        val filename = "frame_${System.currentTimeMillis()}.png"
        val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename)
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()
        bitmap.recycle() // Release resources
    }

    private fun setAlphaForTransition(progress: Float): Float {
        return if (progress < 0.5f) progress * 2 else 1f - (progress - 0.5f) * 2
    }

    @Composable
    fun SampleAnimation(progress: Float) {
        val color = Color(red = progress, green = 1f - progress, blue = 0f)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color)
                .padding(16.dp)
        ) {
            Text(
                text = "Animating Text",
                color = Color.White,
                fontSize = 30.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }



}*/

fun removeThashkeel(s: String): String {

    val p: Pattern = Pattern.compile("[\\p{Mn}]")
    val m: Matcher = p.matcher(s)
    while (m.find()) {
        // println("found: " + m.group())
    }
    m.reset()
    return m.replaceAll("")
}









