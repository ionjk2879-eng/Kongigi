package com.kongigi.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object GifticonStore {
    private const val FILE = "gifticons.json"

    fun save(context: Context, list: List<GifticonEntity>) {
        try {
            val arr = JSONArray()
            list.forEach { g ->
                arr.put(JSONObject().apply {
                    put("id", g.id)
                    put("brand", g.brand)
                    put("productName", g.productName)
                    put("expiryDate", g.expiryDate)
                    put("barcodeValue", g.barcodeValue)
                    put("imageUri", g.imageUri)
                    put("isUsed", g.isUsed)
                })
            }
            File(context.filesDir, FILE).writeText(arr.toString())
            android.util.Log.d("GifticonStore", "저장 완료: ${list.size}개, 경로: ${context.filesDir}/$FILE")
        } catch (e: Exception) {
            android.util.Log.e("GifticonStore", "저장 실패", e)
        }
    }

    fun load(context: Context): List<GifticonEntity> {
        val file = File(context.filesDir, FILE)
        android.util.Log.d("GifticonStore", "불러오기: ${file.absolutePath}, 존재=${file.exists()}")
        if (!file.exists()) return emptyList()
        return try {
            val arr = JSONArray(file.readText())
            android.util.Log.d("GifticonStore", "불러오기 완료: ${arr.length()}개")
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                GifticonEntity(
                    id = o.getInt("id"),
                    brand = o.getString("brand"),
                    productName = o.getString("productName"),
                    expiryDate = o.getLong("expiryDate"),
                    barcodeValue = o.getString("barcodeValue"),
                    imageUri = o.getString("imageUri"),
                    isUsed = o.getBoolean("isUsed")
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("GifticonStore", "불러오기 실패", e)
            emptyList()
        }
    }
}
