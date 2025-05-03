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
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.launch
import unzip
import java.io.File


class LibraryActivity : ComponentActivity() {





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuranTheme {
                val files = remember { mutableStateListOf<File>() }
                var refresh by remember { mutableStateOf(0) }
                val scope = rememberCoroutineScope()


                LaunchedEffect(refresh) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if(! Environment.isExternalStorageManager()){
                            val  intent =  Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                            startActivity(intent);
                        }
                    }
                    var file= File(File(Environment.getExternalStorageDirectory(), "Documents"),"موسوعة اسفار")
                    files.clear()
                    files.addAll(file.listFiles().sortedByDescending { it.length() })

                }

                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121316)) {
                    LazyColumn {
                        itemsIndexed(files) { index, item ->
                            Text(
                                text = item.name,
                                modifier = Modifier.background(color = if (index%2==0)  Color(0xFF121316) else Color(0xFF121316))
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .clickable {
                                        scope.launch {
                                            if(item.path.endsWith(".sqlite",true)){
                                                startActivity(Intent(this@LibraryActivity, LibraryPageActivity::class.java).apply {
                                                    putExtra("file",item.path)
                                                })
                                            }else  if(item.path.endsWith(".zip",true)){
                                                unzip(item.path,item.parent)
                                                refresh++
                                            }

                                        }
                                    }
                            )
                        }
                    }

                }
            }
        }





    }



}