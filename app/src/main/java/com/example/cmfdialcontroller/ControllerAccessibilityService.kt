package com.example.cmfdialcontroller

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.concurrent.atomic.AtomicBoolean

class ControllerAccessibilityService : AccessibilityService() {

    companion object {
        var instance: ControllerAccessibilityService? = null
            private set

        private var lastVolume = -1
        private val handler = Handler(Looper.getMainLooper())
        private var restoringVolume = AtomicBoolean(false)

        fun scrollDown() = instance?.performScroll(down = true)
        fun scrollUp() = instance?.performScroll(down = false)

        // Change these labels to match the site's accessible button/link names.
        fun clickByText(text: String): Boolean {
            val root = instance?.rootInActiveWindow ?: return false
            return clickTextRecursive(root, text)
        }

        private fun clickTextRecursive(node: AccessibilityNodeInfo?, text: String): Boolean {
            if (node == null) return false
            val nodeText = node.text?.toString()
            val desc = node.contentDescription?.toString()
            if ((nodeText != null && nodeText.contains(text, ignoreCase = true)) ||
                (desc != null && desc.contains(text, ignoreCase = true))) {
                if (node.isClickable && node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    return true
                }
                var p = node.parent
                repeat(5) {
                    if (p != null && p.isClickable &&
                        p.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true
                    p = p?.parent
                }
            }
            for (i in 0 until node.childCount) {
                if (clickTextRecursive(node.getChild(i), text)) return true
            }
            return false
        }
    }

    private lateinit var audio: AudioManager
    private val volumePoller = object : Runnable {
        override fun run() {
            checkVolume()
            handler.postDelayed(this, 120)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        audio = getSystemService(AUDIO_SERVICE) as AudioManager
        lastVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
        handler.post(volumePoller)
    }

    override fun onDestroy() {
        handler.removeCallbacks(volumePoller)
        instance = null
        super.onDestroy()
    }

    private fun checkVolume() {
        if (restoringVolume.get()) return
        val now = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
        if (lastVolume >= 0 && now != lastVolume) {
            val delta = now - lastVolume
            lastVolume = now

            // CMF Smart Dial configured as volume:
            // volume up => clockwise => scroll down
            // volume down => counter-clockwise => scroll up
            if (delta > 0) scrollDown() else scrollUp()

            // Restore the old level so the dial acts as a controller, not volume control.
            restoringVolume.set(true)
            try {
                audio.setStreamVolume(AudioManager.STREAM_MUSIC, lastVolume, 0)
            } finally {
                restoringVolume.set(false)
            }
        } else {
            lastVolume = now
        }
    }

    private fun performScroll(down: Boolean) {
        val display = resources.displayMetrics
        val x = display.widthPixels * 0.5f
        val startY = if (down) display.heightPixels * 0.72f else display.heightPixels * 0.30f
        val endY = if (down) display.heightPixels * 0.30f else display.heightPixels * 0.72f

        val path = Path().apply {
            moveTo(x, startY)
            lineTo(x, endY)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 350))
            .build()
        dispatchGesture(gesture, null, null)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Intentionally minimal for the first test.
    }

    override fun onInterrupt() {}

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN || event.repeatCount != 0) {
            return super.onKeyEvent(event)
        }

        when (event.keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                // Single press candidate: test action.
                // Replace with clickByText("YOUR BUTTON TEXT") after identifying the button.
                return true
            }
            KeyEvent.KEYCODE_MEDIA_NEXT -> {
                // Double press / next candidate depending on CMF mapping.
                // Replace with clickByText("Next Chapter") if exposed by the site.
                clickByText("Next Chapter")
                return true
            }
            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                clickByText("Previous Chapter")
                return true
            }
        }
        return super.onKeyEvent(event)
    }
}
