import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.koin.core.context.startKoin
import ui.screen.App

fun main() = application {
    startKoin {
        modules(appModule)
    }
    
    Window(
        onCloseRequest = ::exitApplication,
        title = "层次分析计算",
        state = rememberWindowState(
            width = 640.dp,
            height = 480.dp,
            position = WindowPosition.Aligned(Alignment.Center)
        ),
        resizable = false,
    ) {
        MaterialTheme {
            App()
        }
    }
}