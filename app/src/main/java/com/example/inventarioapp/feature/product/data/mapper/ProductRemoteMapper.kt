package com.example.inventarioapp.feature.product.data.mapper

import com.example.inventarioapp.feature.product.data.local.ProductEntity
import com.example.inventarioapp.feature.product.data.remote.ProductDto

// Transformamos los registros entre la BD local y la representacion remota.
fun ProductEntity.toDto() = ProductDto(
    id = id,
    name = name,
    description = description,
    priceCop = priceCop,
    quantity = quantity,
    updatedAt = updatedAt,
    isDeleted = isDeleted
)

fun ProductDto.toEntity() = ProductEntity(
    id = id,
    name = name,
    description = description,
    priceCop = priceCop,
    quantity = quantity,
    updatedAt = updatedAt,
    pending = false,
    isDeleted = isDeleted
)
