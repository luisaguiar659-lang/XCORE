package com.xcore.app.masterbanner

import android.content.Context
import android.net.Uri
import org.json.JSONArray

internal object BuiltInAgendaTemplate {
    const val URI = "builtin://master-flix-default"
    fun isBuiltIn(value: String?): Boolean = value.isNullOrBlank() || value == URI
}

internal data class BannerBackground(val id: String, val uri: String)

internal class BackgroundRepository(private val context: Context) {
    private val preferences = context.getSharedPreferences("master_banner_backgrounds", Context.MODE_PRIVATE)

    fun load(): List<BannerBackground> {
        val selected = context.getSharedPreferences("master_banner_automation", Context.MODE_PRIVATE)
            .getString("template_uri", "").orEmpty()
        if (BuiltInAgendaTemplate.isBuiltIn(selected)) return emptyList()
        val stored = loadAll()
        return stored.filter { it.uri == selected }.ifEmpty { listOf(BannerBackground(selected, selected)) }
    }

    fun loadAll(): List<BannerBackground> {
        val raw = preferences.getString(KEY_BACKGROUNDS, "[]") ?: "[]"
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val uri = array.optString(index).trim()
                    if (uri.isNotBlank()) add(BannerBackground(uri, uri))
                }
            }
        }.getOrDefault(emptyList())
    }

    fun add(uri: Uri): List<BannerBackground> {
        val current = loadAll().toMutableList()
        val value = uri.toString()
        if (current.none { it.uri == value }) {
            current.add(BannerBackground(value, value))
            save(current)
        }
        return current
    }

    fun remove(uri: String): List<BannerBackground> {
        val updated = loadAll().filterNot { it.uri == uri }
        save(updated)
        return updated
    }

    private fun save(backgrounds: List<BannerBackground>) {
        val array = JSONArray()
        backgrounds.forEach { array.put(it.uri) }
        preferences.edit().putString(KEY_BACKGROUNDS, array.toString()).apply()
    }

    private companion object { const val KEY_BACKGROUNDS = "background_uris" }
}
