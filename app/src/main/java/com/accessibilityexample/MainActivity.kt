package com.accessibilityexample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.accessibilityexample.ui.AccessibilityApp
import com.accessibilityexample.ui.theme.AccessibilityExampleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AccessibilityExampleTheme {
                AccessibilityApp()
            }
        }
    }
}
