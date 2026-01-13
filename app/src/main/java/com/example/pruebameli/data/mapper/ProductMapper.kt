package com.example.pruebameli.data.mapper

import com.example.pruebameli.data.remote.dto.ProductDto
import com.example.pruebameli.domain.models.Product

private fun ProductDto.attrValue(attrId: String): String? =
    attributes?.firstOrNull { it.id == attrId }?.valueName

fun ProductDto.toDomain(): Product {
    val image = pictures?.firstOrNull()?.url

    return Product(
        id = id,
        name = name,
        imageUrl = image,
        brand = attrValue("BRAND"),
        model = attrValue("MODEL")
    )
}