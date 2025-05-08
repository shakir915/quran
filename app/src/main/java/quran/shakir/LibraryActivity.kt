package quran.shakir

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import quran.shakir.ui.theme.QuranTheme
import unzip
import java.io.File


class LibraryActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuranTheme {
                val files = remember { mutableStateListOf<File>() }
                var isLoading by remember { mutableStateOf(false) }
                val scope = rememberCoroutineScope()


                fun refresh() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (!Environment.isExternalStorageManager()) {
                            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                            startActivity(intent);
                        }
                    }
                    var file = File(
                        File(Environment.getExternalStorageDirectory(), "Documents"),
                        "موسوعة اسفار"
                    )
                    files.clear()
                    files.addAll(file.listFiles().sortedByDescending { it.length() })
                    pref.edit().putString("libFileList", files.map { it.path }.joinToString("|||"))
                        .commit()
                }


                LaunchedEffect("") {
                    isLoading = true;
                    if (pref.getString("libFileList", null)?.isNotBlank() == true) {
                        files.clear()
                        files.addAll(
                            pref.getString("libFileList", null)!!.split("|||").map { File(it) })
                    } else {
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                refresh()
                            }
                        }
                    }
                    isLoading = false;
                }

                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121316)) {
                    Column {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF121316))
                                .padding(16.dp),
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.baseline_all_inbox_24),
                                contentDescription = "refresh",
                                colorFilter = ColorFilter.tint(Color.White),
                                modifier = Modifier
                                    .padding(8.dp)
                                    .clickable {
                                        isLoading = true;
                                        refresh()
                                        isLoading = false;
                                    }
                            )
                            if (isLoading) {
                                CircularProgressIndicator()

                            }



                        }

                        Row(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(modifier = Modifier.fillMaxHeight().weight(1f)) {
                                itemsIndexed(files.filter { it.name.endsWith(".zip",true) }) { index, item ->
                                    Text(
                                        text = item.name,
                                        modifier = Modifier
                                            .background(
                                                color = if (index % 2 == 0) Color(
                                                    0xFF121316
                                                ) else Color(0xFF121316)
                                            )
                                            .fillMaxWidth()
                                            .padding(8.dp)
                                            .clickable {
                                                scope.launch {
                                                    isLoading = true;
                                                    withContext(Dispatchers.IO) {
                                                        if (item.path.endsWith(".sqlite", true)) {
                                                            startActivity(
                                                                Intent(
                                                                    this@LibraryActivity,
                                                                    LibraryPageActivity::class.java
                                                                ).apply {
                                                                    putExtra("file", item.path)
                                                                })
                                                        } else if (item.path.endsWith(
                                                                ".zip",
                                                                true
                                                            )
                                                        ) {
                                                            unzip(item.path, item.parent)
                                                            refresh()
                                                        }
                                                    }
                                                    isLoading = false;


                                                }
                                            }
                                    )
                                }
                            }
                            LazyColumn(modifier = Modifier.fillMaxHeight().weight(1f)) {
                                itemsIndexed(files.filter { it.name.endsWith(".sqlite",true) }) { index, item ->
                                    Text(
                                        text = item.name,
                                        modifier = Modifier
                                            .background(
                                                color = if (index % 2 == 0) Color(
                                                    0xFF121316
                                                ) else Color(0xFF121316)
                                            )
                                            .fillMaxWidth()
                                            .padding(8.dp)
                                            .clickable {
                                                scope.launch {
                                                    isLoading = true;
                                                    withContext(Dispatchers.IO) {
                                                        if (item.path.endsWith(".sqlite", true)) {
                                                            startActivity(
                                                                Intent(
                                                                    this@LibraryActivity,
                                                                    LibraryPageActivity::class.java
                                                                ).apply {
                                                                    putExtra("file", item.path)
                                                                })
                                                        } else if (item.path.endsWith(
                                                                ".zip",
                                                                true
                                                            )
                                                        ) {
                                                            unzip(item.path, item.parent)
                                                            refresh()
                                                        }
                                                    }
                                                    isLoading = false;


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


    }
}