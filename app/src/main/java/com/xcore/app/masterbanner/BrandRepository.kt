package com.xcore.app.masterbanner

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class BrandProfile(
    val id: Long,
    val name: String,
    val instagram: String,
    val whatsapp: String,
    val primaryColor: String,
    val secondaryColor: String,
    val adaptColorsToTemplate: Boolean = false,
    val logoUri: String,
    val watermarkText: String,
    val watermarkEnabled: Boolean,
)

class BrandRepository(context: Context) {
    private val preferences = context.getSharedPreferences("master_banner_brands", Context.MODE_PRIVATE)

    fun loadBrands(): List<BrandProfile> {
        val raw = preferences.getString(KEY_BRANDS, "[]") ?: "[]"
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) add(array.getJSONObject(index).toBrandProfile())
            }
        }.getOrDefault(emptyList())
    }

    fun saveBrands(brands: List<BrandProfile>) {
        val array = JSONArray()
        brands.forEach { brand ->
            array.put(
                JSONObject()
                    .put("id", brand.id)
                    .put("name", brand.name)
                    .put("instagram", brand.instagram)
                    .put("whatsapp", brand.whatsapp)
                    .put("primaryColor", brand.primaryColor)
                    .put("secondaryColor", brand.secondaryColor)
                    .put("adaptColorsToTemplate", brand.adaptColorsToTemplate)
                    .put("logoUri", brand.logoUri)
                    .put("watermarkText", brand.watermarkText)
                    .put("watermarkEnabled", brand.watermarkEnabled),
            )
        }
        preferences.edit().putString(KEY_BRANDS, array.toString()).apply()
    }

    private fun JSONObject.toBrandProfile() = BrandProfile(
        id = optLong("id", System.currentTimeMillis()),
        name = optString("name"),
        instagram = optString("instagram"),
        whatsapp = optString("whatsapp"),
        primaryColor = optString("primaryColor", "#9BFF38"),
        secondaryColor = optString("secondaryColor", "#53C7FF"),
        adaptColorsToTemplate = optBoolean("adaptColorsToTemplate", false),
        logoUri = optString("logoUri"),
        watermarkText = optString("watermarkText"),
        watermarkEnabled = optBoolean("watermarkEnabled", true),
    )

    private companion object { const val KEY_BRANDS = "brands_json" }
}
