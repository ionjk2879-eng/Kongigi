package com.kongigi

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.kongigi.data.GifticonEntity
import com.kongigi.data.GifticonStore

class GifticonViewModel(app: Application) : AndroidViewModel(app) {
    private val ctx = app.applicationContext
    private val _all = mutableStateListOf<GifticonEntity>()
    val all: List<GifticonEntity> = _all

    var selected by mutableStateOf<GifticonEntity?>(null)

    init {
        _all.addAll(GifticonStore.load(ctx))
    }

    private fun persist() = GifticonStore.save(ctx, _all)

    fun insert(g: GifticonEntity) {
        val nextId = (_all.maxOfOrNull { it.id } ?: -1) + 1
        _all.add(g.copy(id = nextId))
        persist()
    }

    fun update(g: GifticonEntity) {
        val idx = _all.indexOfFirst { it.id == g.id }
        if (idx >= 0) { _all[idx] = g; persist() }
    }

    fun markUsed(g: GifticonEntity) {
        val idx = _all.indexOfFirst { it.id == g.id }
        if (idx >= 0) { _all[idx] = g.copy(isUsed = true); persist() }
    }

    fun delete(g: GifticonEntity) {
        _all.removeIf { it.id == g.id }
        persist()
    }
}
