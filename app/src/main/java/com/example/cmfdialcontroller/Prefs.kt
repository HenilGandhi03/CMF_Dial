package com.example.cmfdialcontroller

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Thin SharedPreferences wrapper. Stores:
 *   - action for each press slot (single / double / long)
 *   - recorded click fingerprints
 *   - scroll tuning knobs
 */
class Prefs(context: Context) {

    enum class Slot(val key: String) {
        SINGLE_PRESS("slot_single"),
        DOUBLE_PRESS("slot_double"),
        LONG_PRESS("slot_long"),
    }

    private val sp: SharedPreferences =
        context.applicationContext.getSharedPreferences("cmf_dial_prefs", Context.MODE_PRIVATE)

    // ---------- Bindings ----------

    fun getAction(slot: Slot): Action {
        val raw = sp.getString(slot.key, null) ?: return defaultFor(slot)
        return runCatching { Action.fromJson(JSONObject(raw)) }.getOrDefault(Action.None)
    }

    fun setAction(slot: Slot, action: Action) {
        sp.edit().putString(slot.key, action.toJson().toString()).apply()
    }

    private fun defaultFor(slot: Slot): Action = when (slot) {
        Slot.SINGLE_PRESS -> Action.Media(Action.MediaKind.PLAY_PAUSE)
        Slot.DOUBLE_PRESS -> Action.Media(Action.MediaKind.NEXT)
        Slot.LONG_PRESS -> Action.Media(Action.MediaKind.PREVIOUS)
    }

    // ---------- Recordings ----------

    fun recordings(): List<RecordedClick> =
        RecordedClick.listFromJson(
            runCatching { JSONArray(sp.getString(KEY_RECORDINGS, "[]")) }.getOrNull()
        )

    fun addRecording(rec: RecordedClick) {
        val list = recordings().toMutableList()
        list.removeAll { it.id == rec.id }
        list += rec
        saveRecordings(list)
    }

    fun removeRecording(id: String) {
        val list = recordings().filter { it.id != id }
        saveRecordings(list)

        // Clear any slot pointing at this recording.
        Slot.values().forEach { slot ->
            val a = getAction(slot)
            if (a is Action.ClickRecording && a.recordingId == id) {
                setAction(slot, Action.None)
            }
        }
    }

    fun recordingById(id: String): RecordedClick? =
        recordings().firstOrNull { it.id == id }

    private fun saveRecordings(list: List<RecordedClick>) {
        val arr = JSONArray()
        list.forEach { arr.put(it.toJson()) }
        sp.edit().putString(KEY_RECORDINGS, arr.toString()).apply()
    }

    // ---------- Scroll tuning ----------

    /** Enable velocity-based scroll wheel feel. */
    var scrollWheelEnabled: Boolean
        get() = sp.getBoolean(KEY_SCROLL_WHEEL, true)
        set(v) = sp.edit().putBoolean(KEY_SCROLL_WHEEL, v).apply()

    companion object {
        private const val KEY_RECORDINGS = "recordings"
        private const val KEY_SCROLL_WHEEL = "scroll_wheel_enabled"

        fun newRecordingId(): String = UUID.randomUUID().toString()
    }
}
