package com.example.cmfdialcontroller

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 40, 32, 32)
        }

        fun label(text: String) = TextView(this).apply {
            this.text = text
            textSize = 16f
            setPadding(0, 16, 0, 8)
        }

        root.addView(TextView(this).apply {
            text = "CMF Dial Controller — Test"
            textSize = 24f
        })

        root.addView(label("1. Enable the Accessibility Service:"))
        root.addView(Button(this).apply {
            text = "Open Accessibility Settings"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        })

        root.addView(label("2. Test controls while Brave is open:"))

        root.addView(Button(this).apply {
            text = "Test Scroll Down"
            setOnClickListener { ControllerAccessibilityService.scrollDown() }
        })
        root.addView(Button(this).apply {
            text = "Test Scroll Up"
            setOnClickListener { ControllerAccessibilityService.scrollUp() }
        })

        root.addView(label(
            "Dial: volume up/down is converted to scroll up/down.\n" +
            "Media-key presses are logged for later mapping.\n" +
            "Keep Brave in the foreground for the test."
        ))

        val status = TextView(this)
        status.text = "Accessibility service: ${if (ControllerAccessibilityService.instance != null) "ON" else "OFF"}"
        status.textSize = 15f
        root.addView(status)

        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        // Recreate screen so service status refreshes after returning from Settings.
        if (ControllerAccessibilityService.instance != null) {
            Toast.makeText(this, "Controller service is ON", Toast.LENGTH_SHORT).show()
        }
    }
}
