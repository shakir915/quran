package quran.shakir

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import quran.shakir.ui.theme.QuranTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val kfgqpc_uthmanic_script_hafs_regular = FontFamily(
                Font(R.font.kfgqpc_uthmanic_script_hafs_regular, FontWeight.Medium),

                )

            var font_size_malayalam by remember { mutableStateOf(pref.getInt("font_size_malayalam",16)) }
            var font_size_arabic by remember { mutableStateOf(pref.getInt("font_size_arabic",20)) }

            QuranTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121316)) {


                        Column(modifier = Modifier.padding(16.dp)) {

                            Row {
                                Text(
                                    "Font Size (Quran) $font_size_arabic",
                                    modifier = Modifier.padding(8.dp),
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.weight(1f))
                                Image(
                                    painter = painterResource(id = R.drawable.baseline_remove_24),
                                    contentDescription = "font minus size  arabic",
                                    colorFilter = ColorFilter.tint(color = Color.White),
                                    modifier = Modifier
                                        .align(alignment = Alignment.CenterVertically)
                                        .padding(12.dp)
                                        .clickable {
                                            if (font_size_arabic>5)
                                                font_size_arabic--
                                            pref.edit().putInt("font_size_arabic",font_size_arabic).commit()

                                        },
                                    //fontFamily = kfgqpc_uthmanic_script_hafs_regular,
                                );

                                Image(
                                    painter = painterResource(id = R.drawable.baseline_add_24),
                                    contentDescription = "font plus size arabic ",
                                    colorFilter = ColorFilter.tint(color = Color.White),
                                    modifier = Modifier
                                        .align(alignment = Alignment.CenterVertically)
                                        .padding(12.dp)
                                        .clickable {
                                            if (font_size_arabic<50)
                                                font_size_arabic++
                                            pref.edit().putInt("font_size_arabic",font_size_arabic).commit()

                                        },
                                    //fontFamily = kfgqpc_uthmanic_script_hafs_regular,
                                )

                            }



                            Row {
                                Text(
                                    "Font Size (Translation) $font_size_malayalam",
                                    modifier = Modifier.padding(8.dp),
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.weight(1f))
                                Image(
                                    painter = painterResource(id = R.drawable.baseline_remove_24),
                                    contentDescription = "font minus size  malayalam",
                                    colorFilter = ColorFilter.tint(color = Color.White),
                                    modifier = Modifier
                                        .align(alignment = Alignment.CenterVertically)
                                        .padding(12.dp)
                                        .clickable {
                                            if (font_size_malayalam>5)
                                                font_size_malayalam--
                                            pref.edit().putInt("font_size_malayalam",font_size_malayalam).commit()

                                        },
                                    //fontFamily = kfgqpc_uthmanic_script_hafs_regular,
                                );

                                Image(
                                    painter = painterResource(id = R.drawable.baseline_add_24),
                                    contentDescription = "font plus size malayalam",
                                    colorFilter = ColorFilter.tint(color = Color.White),
                                    modifier = Modifier
                                        .align(alignment = Alignment.CenterVertically)
                                        .padding(12.dp)
                                        .clickable {
                                            if (font_size_malayalam<50)
                                                font_size_malayalam++
                                            pref.edit().putInt("font_size_malayalam",font_size_malayalam).commit()
                                        },
                                    //fontFamily = kfgqpc_uthmanic_script_hafs_regular,
                                )

                            }

                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                Text(
                                    text = "بِسۡمِ ٱللَّهِ ٱلرَّحۡمَٰنِ ٱلرَّحِيم",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                        .align(Alignment.End),
                                    fontFamily = kfgqpc_uthmanic_script_hafs_regular,
                                    fontSize = font_size_arabic.sp,
                                    lineHeight = 1.4.em,
                                    color = Color.White


                                )
                            }


                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Text(
                                    "പരമകാരുണികനും കരുണാനിധിയുമായ അല്ലാഹുവിന്റെ നാമത്തില്\u200D",
                                    modifier = Modifier.padding(8.dp),
                                    color = Color.White,

                                    lineHeight = 1.4.em,
                                            fontSize = font_size_malayalam.sp
                                )
                            }

                        }



                }
            }
        }
    }
}