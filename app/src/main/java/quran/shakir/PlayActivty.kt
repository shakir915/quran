package quran.shakir


import androidx.compose.ui.platform.LocalContext

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale

class PlayActivty : ComponentActivity() {

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
            val bismi = getAyaths("1").first.first()
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


                val lazyListState = rememberLazyListState()


                val scope = rememberCoroutineScope()


                val selected = remember { mutableStateListOf<Int>() }

                var indexPlus1 by remember { mutableStateOf(0) }


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
                    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121316)) {
                        Column {
/*                            Row(
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(color = Color.White.copy(alpha = .05f))
                            ) {


                                Image(
                                    painter = painterResource(id = R.drawable.baseline_play_circle_24),
                                    contentDescription = "Share Clipboard Content",
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .clickable {


                                        },
                                )


                            }*/




                            Column(

                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                                    .align(alignment = Alignment.CenterHorizontally)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                        onClick = {
                                            if (ayaths.getOrNull(indexPlus1)!=null)
                                            indexPlus1++
                                            else{
                                                finish()
                                            }
                                        }

                                    ),
                            ) {
                                Spacer(modifier = Modifier.weight(3f))
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
                                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                        Text(
                                            text = "${ayaths.get(index)} ${
                                                java.lang.String.valueOf(
                                                    nf.format(index + 1)
                                                )
                                            }",
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .align(Alignment.CenterHorizontally),
                                            fontFamily = kfgqpc_uthmanic_script_hafs_regular,
                                            fontSize = (pref.getInt("font_size_arabic", 20)).sp,
                                            lineHeight = 1.4.em,
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

                                Spacer(modifier = Modifier.weight(5f))
                            }


                        }


                    }
                }
            }
        }


    }


}