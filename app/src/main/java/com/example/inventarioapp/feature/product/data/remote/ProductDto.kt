package com.example.inventarioapp.feature.product.data.remote

/** Representacion del documento Firestore; no contiene anotaciones Room. */
data class ProductDto(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val priceCop: Long = 0L,
    val quantity: Int = 0,
    val updatedAt: Long = 0L,
    val isDeleted: Boolean = false
)
