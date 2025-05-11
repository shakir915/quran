package quran.shakir

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FadingVerticalScrollbar(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    scrollbarColor: Color = Color.Gray,
    scrollbarWidth: Dp = 4.dp,
    fadeDelayMillis: Long = 1500,
    content: @Composable ColumnScope.() -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var visible by remember { mutableStateOf(true) }
    var fadeJob by remember { mutableStateOf<Job?>(null) }
    val alpha by animateFloatAsState(if (visible) 1f else 0f, label = "scrollbar-fade")

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(end = scrollbarWidth),
            content = content
        )

        // Scrollbar logic
        val scrollRatio = scrollState.value.toFloat() / scrollState.maxValue.coerceAtLeast(1)
        val visibleRatio = scrollState.maxValue.toFloat() / scrollState.maxValue.toFloat().coerceAtLeast(1f)
        val thumbHeight = (visibleRatio * 100.dp.value).coerceAtLeast(24f)

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(scrollbarWidth)
                .align(Alignment.CenterEnd)
                .padding(vertical = 16.dp)
                .alpha(alpha)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(thumbHeight.dp)
                    .offset(y = ((scrollRatio * (200 - thumbHeight)).dp))
                    .background(scrollbarColor)
            )
        }
    }

    // Scroll activity tracking
    LaunchedEffect(scrollState.value) {
        visible = true
        fadeJob?.cancel()
        fadeJob = coroutineScope.launch {
            delay(fadeDelayMillis)
            visible = false
        }
    }
}
