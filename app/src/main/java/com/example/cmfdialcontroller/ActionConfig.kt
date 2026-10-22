package com.example.cmfdialcontroller

import org.json.JSONArray
import org.json.JSONObject

/**
 * A single action that can be bound to a press (single / double / long).
 *
 * Kept as a plain sealed class with manual JSON (de)serialization so the project
 * stays dependency-free.
 */
sealed class Action {

    abstract fun toJson(): JSONObject
    abstract fun label(): String

    object None : Action() {
        override fun toJson() = JSONObject().put("type", "none")
        override fun label() = "Do nothing"
    }

    object ScrollUp : Action() {
        override fun toJson() = JSONObject().put("type", "scroll_up")
        override fun label() = "Scroll up"
    }

    object ScrollDown : Action() {
        override fun toJson() = JSONObject().put("type", "scroll_down")
        override fun label() = "Scroll down"
    }

    /** Click a node previously captured via "Record a click". */
    data class ClickRecording(val recordingId: String, val name: String) : Action() {
        override fun toJson() = JSONObject()
            .put("type", "click_recording")
            .put("recordingId", recordingId)
            .put("name", name)
        override fun label() = "Click: $name"
    }

    /** Fallback: fuzzy text search in the accessibility tree. */
    data class ClickByText(val text: String) : Action() {
        override fun toJson() = JSONObject()
            .put("type", "click_text")
            .put("text", text)
        override fun label() = "Click text: \"$text\""
    }

    enum class MediaKind { PLAY_PAUSE, NEXT, PREVIOUS }

    data class Media(val kind: MediaKind) : Action() {
        override fun toJson() = JSONObject()
            .put("type", "media")
            .put("kind", kind.name)
        override fun label() = when (kind) {
            MediaKind.PLAY_PAUSE -> "Media: play / pause"
            MediaKind.NEXT -> "Media: next track"
            MediaKind.PREVIOUS -> "Media: previous track"
        }
    }

    data class LaunchApp(val packageName: String, val displayName: String) : Action() {
        override fun toJson() = JSONObject()
            .put("type", "launch_app")
            .put("package", packageName)
            .put("name", displayName)
        override fun label() = "Launch: $displayName"
    }

    enum class SystemKind { HOME, BACK, RECENTS, NOTIFICATIONS }

    data class SystemAction(val kind: SystemKind) : Action() {
        override fun toJson() = JSONObject()
            .put("type", "system")
            .put("kind", kind.name)
        override fun label() = when (kind) {
            SystemKind.HOME -> "System: home"
            SystemKind.BACK -> "System: back"
            SystemKind.RECENTS -> "System: recent apps"
            SystemKind.NOTIFICATIONS -> "System: notifications shade"
        }
    }

    companion object {
        fun fromJson(obj: JSONObject?): Action {
            if (obj == null) return None
            return when (obj.optString("type")) {
                "scroll_up" -> ScrollUp
                "scroll_down" -> ScrollDown
                "click_recording" -> ClickRecording(
                    obj.optString("recordingId"),
                    obj.optString("name")
                )
                "click_text" -> ClickByText(obj.optString("text"))
                "media" -> Media(
                    runCatching { MediaKind.valueOf(obj.optString("kind")) }
                        .getOrDefault(MediaKind.PLAY_PAUSE)
                )
                "launch_app" -> LaunchApp(
                    obj.optString("package"),
                    obj.optString("name")
                )
                "system" -> SystemAction(
                    runCatching { SystemKind.valueOf(obj.optString("kind")) }
                        .getOrDefault(SystemKind.HOME)
                )
                else -> None
            }
        }
    }
}

/**
 * A captured click target. We store several fingerprints (resource-id, text,
 * content-description, class name) so we can locate the same node later even if
 * one attribute changes.
 */
data class RecordedClick(
    val id: String,
    val name: String,
    val packageName: String?,
    val viewId: String?,
    val text: String?,
    val contentDescription: String?,
    val className: String?
) {
    fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("name", name)
        .put("package", packageName ?: JSONObject.NULL)
        .put("viewId", viewId ?: JSONObject.NULL)
        .put("text", text ?: JSONObject.NULL)
        .put("contentDescription", contentDescription ?: JSONObject.NULL)
        .put("className", className ?: JSONObject.NULL)

    companion object {
        fun fromJson(obj: JSONObject): RecordedClick = RecordedClick(
            id = obj.optString("id"),
            name = obj.optString("name"),
            packageName = obj.optStringOrNull("package"),
            viewId = obj.optStringOrNull("viewId"),
            text = obj.optStringOrNull("text"),
            contentDescription = obj.optStringOrNull("contentDescription"),
            className = obj.optStringOrNull("className")
        )

        fun listFromJson(arr: JSONArray?): List<RecordedClick> {
            if (arr == null) return emptyList()
            val out = mutableListOf<RecordedClick>()
            for (i in 0 until arr.length()) {
                arr.optJSONObject(i)?.let { out += fromJson(it) }
            }
            return out
        }
    }
}

private fun JSONObject.optStringOrNull(key: String): String? {
    if (isNull(key)) return null
    val v = optString(key, "")
    return v.ifEmpty { null }
}
