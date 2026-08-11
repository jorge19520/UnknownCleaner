package com.example.unknowncleaner

import android.os.Bundle
import androidx.activity.ComponentActivity

class ComposeSmsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finish() // Just finish immediately, we are not building a real SMS app
    }
}
