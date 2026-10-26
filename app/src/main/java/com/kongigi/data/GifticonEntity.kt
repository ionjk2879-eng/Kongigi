package com.kongigi.data

data class GifticonEntity(
    val id: Int = 0,
    val brand: String = "",
    val productName: String = "",
    val expiryDate: Long = 0L,
    val barcodeValue: String = "",
    val imageUri: String = "",
    val isUsed: Boolean = false
)
