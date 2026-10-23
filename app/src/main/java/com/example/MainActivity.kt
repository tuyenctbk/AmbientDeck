package com.example

import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.AmbientDeckScreen
import com.example.ui.AmbientDeckViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.VoidBlack

class MainActivity : ComponentActivity() {

    private val viewModel: AmbientDeckViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep ambient screen on continuously for Smart TV & Kiosk displays
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        enableEdgeToEdge()

        // Initialize smart engagement manager (updates, rate, share)
        viewModel.initEngagement(this.applicationContext)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = VoidBlack
                ) {
                    AmbientDeckScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_MENU -> {
                viewModel.onToggleSettings(true)
                true
            }
            KeyEvent.KEYCODE_MEDIA_NEXT, KeyEvent.KEYCODE_PAGE_DOWN -> {
                viewModel.cycleWallpaper()
                true
            }
            KeyEvent.KEYCODE_MEDIA_PREVIOUS, KeyEvent.KEYCODE_PAGE_UP -> {
                viewModel.nextHistoryItem()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
}
