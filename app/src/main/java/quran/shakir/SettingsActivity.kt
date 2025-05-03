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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import quran.shakir.ui.theme.QuranTheme

var arabicPlusMalayalam = "Arabic+Malayalam";
var malayalamOnly = "Malayalam Only";

//https://github.com/GovarJabbar/Quran-PNG

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val kfgqpc_uthmanic_script_hafs_regular = FontFamily(
                Font(R.font.kfgqpc_uthmanic_script_hafs_regular, FontWeight.Medium),

                )

            var font_size_malayalam by remember {
                mutableStateOf(
                    pref.getInt(
                        "font_size_malayalam",
                        16
                    )
                )
            }
            var font_size_arabic by remember { mutableStateOf(pref.getInt("font_size_arabic", 20)) }
            var font_size_sura by remember { mutableStateOf(pref.getInt("font_size_sura", 10)) }
            var space_above_sura_name by remember {
                mutableStateOf(
                    pref.getInt(
                        "space_above_sura_name",
                        20
                    )
                )
            }
            var space_above_aya by remember { mutableStateOf(pref.getInt("space_above_aya", 30)) }
            var space_above_malayalam by remember {
                mutableStateOf(
                    pref.getInt(
                        "space_above_malayalam",
                        0
                    )
                )
            }
            var left_padding by remember { mutableStateOf(pref.getInt("left_padding", 24)) }
            var right_padding by remember { mutableStateOf(pref.getInt("right_padding", 24)) }
            var shadow_offset by remember { mutableStateOf(pref.getFloat("shadow_offset", 2f)) }
            var shadow_blurRadius by remember {
                mutableStateOf(
                    pref.getFloat(
                        "shadow_blurRadius",
                        4f
                    )
                )
            }

            QuranTheme {

                var selectedOption by remember {
                    mutableStateOf(
                        pref.getString(
                            "SHARE_LANGUAGES_V1",
                            arabicPlusMalayalam
                        )
                    )
                }


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
                                        if (font_size_arabic > 5)
                                            font_size_arabic--
                                        pref.edit().putInt("font_size_arabic", font_size_arabic)
                                            .commit()

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
                                        if (font_size_arabic < 50)
                                            font_size_arabic++
                                        pref.edit().putInt("font_size_arabic", font_size_arabic)
                                            .commit()

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
                                        if (font_size_malayalam > 5)
                                            font_size_malayalam--
                                        pref.edit()
                                            .putInt("font_size_malayalam", font_size_malayalam)
                                            .commit()

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
                                        if (font_size_malayalam < 50)
                                            font_size_malayalam++
                                        pref.edit()
                                            .putInt("font_size_malayalam", font_size_malayalam)
                                            .commit()
                                    },
                                //fontFamily = kfgqpc_uthmanic_script_hafs_regular,
                            )

                        }


                        Row {
                            Text(
                                "Font Size (sura name in player) $font_size_sura",
                                modifier = Modifier.padding(8.dp),
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.weight(1f))
                            Image(
                                painter = painterResource(id = R.drawable.baseline_remove_24),
                                contentDescription = "font_size_sura",
                                colorFilter = ColorFilter.tint(color = Color.White),
                                modifier = Modifier
                                    .align(alignment = Alignment.CenterVertically)
                                    .padding(12.dp)
                                    .clickable {
                                        if (font_size_sura > 0)
                                            font_size_sura--
                                        pref.edit().putInt("font_size_sura", font_size_sura).commit()

                                    },
                            );

                            Image(
                                painter = painterResource(id = R.drawable.baseline_add_24),
                                contentDescription = "font_size_sura",
                                colorFilter = ColorFilter.tint(color = Color.White),
                                modifier = Modifier
                                    .align(alignment = Alignment.CenterVertically)
                                    .padding(12.dp)
                                    .clickable {
                                        font_size_sura++
                                        pref.edit().putInt("font_size_sura", font_size_sura).commit()
                                    },
                                //fontFamily = kfgqpc_uthmanic_script_hafs_regular,
                            )

                        }


                        Row {
                            Text(
                                "space_above_sura_name $space_above_sura_name",
                                modifier = Modifier.padding(8.dp),
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.weight(1f))
                            Image(
                                painter = painterResource(id = R.drawable.baseline_remove_24),
                                contentDescription = "space_above_sura_name",
                                colorFilter = ColorFilter.tint(color = Color.White),
                                modifier = Modifier
                                    .align(alignment = Alignment.CenterVertically)
                                    .padding(12.dp)
                                    .clickable {
                                        if (space_above_sura_name >0)
                                            space_above_sura_name--
                                        pref.edit().putInt("space_above_sura_name", space_above_sura_name).commit()
                                    },
                            );

                            Image(
                                painter = painterResource(id = R.drawable.baseline_add_24),
                                contentDescription = "space_above_sura_name",
                                colorFilter = ColorFilter.tint(color = Color.White),
                                modifier = Modifier
                                    .align(alignment = Alignment.CenterVertically)
                                    .padding(12.dp)
                                    .clickable {
                                            space_above_sura_name++
                                        pref.edit().putInt("space_above_sura_name", space_above_sura_name).commit()
                                    },
                                //fontFamily = kfgqpc_uthmanic_script_hafs_regular,
                            )

                        }


                        Row {
                            Text(
                                "space_above_aya $space_above_aya",
                                modifier = Modifier.padding(8.dp),
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.weight(1f))
                            Image(
                                painter = painterResource(id = R.drawable.baseline_remove_24),
                                contentDescription = "space_above_aya",
                                colorFilter = ColorFilter.tint(color = Color.White),
                                modifier = Modifier
                                    .align(alignment = Alignment.CenterVertically)
                                    .padding(12.dp)
                                    .clickable {
                                        if (space_above_aya > 0)
                                            space_above_aya--
                                        pref.edit().putInt("space_above_aya", space_above_aya).commit()

                                    },
                                //fontFamily = kfgqpc_uthmanic_script_hafs_regular,
                            );

                            Image(
                                painter = painterResource(id = R.drawable.baseline_add_24),
                                contentDescription = "space_above_aya",
                                colorFilter = ColorFilter.tint(color = Color.White),
                                modifier = Modifier
                                    .align(alignment = Alignment.CenterVertically)
                                    .padding(12.dp)
                                    .clickable {
                                        space_above_aya++
                                        pref.edit().putInt("space_above_aya", space_above_aya).commit()
                                    },
                                //fontFamily = kfgqpc_uthmanic_script_hafs_regular,
                            )

                        }

                        Row {
                            Text(
                                "space_above_malayalam $space_above_malayalam",
                                modifier = Modifier.padding(8.dp),
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.weight(1f))
                            Image(
                                painter = painterResource(id = R.drawable.baseline_remove_24),
                                contentDescription = "space_above_malayalam",
                                colorFilter = ColorFilter.tint(color = Color.White),
                                modifier = Modifier
                                    .align(alignment = Alignment.CenterVertically)
                                    .padding(12.dp)
                                    .clickable {
                                        if (space_above_malayalam > 0)
                                        space_above_malayalam--
                                        pref.edit()
                                            .putInt("space_above_malayalam", space_above_malayalam)
                                            .commit()

                                    },
                                //fontFamily = kfgqpc_uthmanic_script_hafs_regular,
                            );

                            Image(
                                painter = painterResource(id = R.drawable.baseline_add_24),
                                contentDescription = "space_above_malayalam",
                                colorFilter = ColorFilter.tint(color = Color.White),
                                modifier = Modifier
                                    .align(alignment = Alignment.CenterVertically)
                                    .padding(12.dp)
                                    .clickable {

                                        space_above_malayalam++
                                        pref.edit()
                                            .putInt("space_above_malayalam", space_above_malayalam)
                                            .commit()
                                    },
                                //fontFamily = kfgqpc_uthmanic_script_hafs_regular,
                            )

                        }
                        Row {
                            Text(
                                "left_padding $left_padding",
                                modifier = Modifier.padding(8.dp),
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.weight(1f))
                            Image(
                                painter = painterResource(id = R.drawable.baseline_remove_24),
                                contentDescription = "left_padding",
                                colorFilter = ColorFilter.tint(color = Color.White),
                                modifier = Modifier
                                    .align(alignment = Alignment.CenterVertically)
                                    .padding(12.dp)
                                    .clickable {
                                        if (left_padding > 0)
                                            left_padding--
                                        pref.edit().putInt("left_padding", left_padding).commit()

                                    },
                                //fontFamily = kfgqpc_uthmanic_script_hafs_regular,
                            );

                            Image(
                                painter = painterResource(id = R.drawable.baseline_add_24),
                                contentDescription = "left_padding",
                                colorFilter = ColorFilter.tint(color = Color.White),
                                modifier = Modifier
                                    .align(alignment = Alignment.CenterVertically)
                                    .padding(12.dp)
                                    .clickable {

                                        left_padding++
                                        pref.edit().putInt("left_padding", left_padding).commit()
                                    },
                                //fontFamily = kfgqpc_uthmanic_script_hafs_regular,
                            )

                        }
                        Row {
                            Text(
                                "right_padding $right_padding",
                                modifier = Modifier.padding(8.dp),
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.weight(1f))
                            Image(
                                painter = painterResource(id = R.drawable.baseline_remove_24),
                                contentDescription = "right_padding",
                                colorFilter = ColorFilter.tint(color = Color.White),
                                modifier = Modifier
                                    .align(alignment = Alignment.CenterVertically)
                                    .padding(12.dp)
                                    .clickable {
                                        if (right_padding > 0)
                                            right_padding--
                                        pref.edit().putInt("right_padding", right_padding).commit()

                                    },
                                //fontFamily = kfgqpc_uthmanic_script_hafs_regular,
                            );

                            Image(
                                painter = painterResource(id = R.drawable.baseline_add_24),
                                contentDescription = "right_padding",
                                colorFilter = ColorFilter.tint(color = Color.White),
                                modifier = Modifier
                                    .align(alignment = Alignment.CenterVertically)
                                    .padding(12.dp)
                                    .clickable {

                                        right_padding++
                                        pref.edit().putInt("right_padding", right_padding).commit()
                                    },
                                //fontFamily = kfgqpc_uthmanic_script_hafs_regular,
                            )

                        }

                        Row {
                            Text(
                                "shadow_offset $shadow_offset",
                                modifier = Modifier.padding(8.dp),
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.weight(1f))
                            Image(
                                painter = painterResource(id = R.drawable.baseline_remove_24),
                                contentDescription = "shadow_offset",
                                colorFilter = ColorFilter.tint(color = Color.White),
                                modifier = Modifier
                                    .align(alignment = Alignment.CenterVertically)
                                    .padding(12.dp)
                                    .clickable {

                                        shadow_offset = shadow_offset - .1f
                                        pref.edit().putFloat("shadow_offset", shadow_offset)
                                            .commit()

                                    },
                                //fontFamily = kfgqpc_uthmanic_script_hafs_regular,
                            );

                            Image(
                                painter = painterResource(id = R.drawable.baseline_add_24),
                                contentDescription = "shadow_offset",
                                colorFilter = ColorFilter.tint(color = Color.White),
                                modifier = Modifier
                                    .align(alignment = Alignment.CenterVertically)
                                    .padding(12.dp)
                                    .clickable {
                                        shadow_offset = shadow_offset + .1f
                                        pref.edit().putFloat("shadow_offset", shadow_offset)
                                            .commit()
                                    },
                                //fontFamily = kfgqpc_uthmanic_script_hafs_regular,
                            )

                        }


                        Row {
                            Text(
                                "shadow_blurRadius $shadow_blurRadius",
                                modifier = Modifier.padding(8.dp),
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.weight(1f))
                            Image(
                                painter = painterResource(id = R.drawable.baseline_remove_24),
                                contentDescription = "shadow_blurRadius",
                                colorFilter = ColorFilter.tint(color = Color.White),
                                modifier = Modifier
                                    .align(alignment = Alignment.CenterVertically)
                                    .padding(12.dp)
                                    .clickable {

                                        shadow_blurRadius = shadow_blurRadius - .1f
                                        pref.edit().putFloat("shadow_blurRadius", shadow_blurRadius).commit()

                                    },
                                //fontFamily = kfgqpc_uthmanic_script_hafs_regular,
                            );

                            Image(
                                painter = painterResource(id = R.drawable.baseline_add_24),
                                contentDescription = "shadow_blurRadius",
                                colorFilter = ColorFilter.tint(color = Color.White),
                                modifier = Modifier
                                    .align(alignment = Alignment.CenterVertically)
                                    .padding(12.dp)
                                    .clickable {
                                        shadow_blurRadius = shadow_blurRadius + .1f
                                        pref.edit().putFloat("shadow_blurRadius", shadow_blurRadius).commit()
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



                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "Share Ayath with :",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Column(Modifier.selectableGroup()) {
                                listOf(arabicPlusMalayalam, malayalamOnly).forEach { option ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .selectable(
                                                selected = (selectedOption == option),
                                                onClick = {
                                                    selectedOption = option
                                                    pref.edit().putString(
                                                        "SHARE_LANGUAGES_V1",
                                                        selectedOption
                                                    ).commit()
                                                },
                                                role = Role.RadioButton
                                            )
                                            .padding(8.dp)
                                    ) {
                                        RadioButton(
                                            selected = (selectedOption == option),
                                            onClick = {
                                                selectedOption = option
                                                pref.edit()
                                                    .putString("SHARE_LANGUAGES_V1", selectedOption)
                                                    .commit()
                                            }
                                        )
                                        Text(option, Modifier.padding(start = 8.dp))
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