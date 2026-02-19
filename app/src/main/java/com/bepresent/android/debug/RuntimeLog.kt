package com.bepresent.android.debug

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object RuntimeLog {
    private const val MAX_ENTRIES = 200
    private val formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
    private val _entries = MutableStateFlow<List<String>>(emptyList())
    val entries: StateFlow<List<String>> = _entries.asStateFlow()

    fun d(tag: String, message: String) = log(Log.DEBUG, tag, message, null)
    fun i(tag: String, message: String) = log(Log.INFO, tag, message, null)
    fun w(tag: String, message: String) = log(Log.WARN, tag, message, null)
    fun e(tag: String, message: String, throwable: Throwable? = null) =
        log(Log.ERROR, tag, message, throwable)

    fun clear() {
        _entries.value = emptyList()
    }

    private fun log(priority: Int, tag: String, message: String, throwable: Throwable?) {
        when (priority) {
            Log.DEBUG -> Log.d(tag, message)
            Log.INFO -> Log.i(tag, message)
            Log.WARN -> Log.w(tag, message)
            else -> Log.e(tag, message, throwable)
        }

        val level = when (priority) {
            Log.DEBUG -> "D"
            Log.INFO -> "I"
            Log.WARN -> "W"
            else -> "E"
        }
        val error = throwable?.let { " | ${it.javaClass.simpleName}: ${it.message}" } ?: ""
        val line = "${LocalTime.now().format(formatter)} $level/$tag: $message$error"
        _entries.value = (_entries.value + line).takeLast(MAX_ENTRIES)
    }
}
