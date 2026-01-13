package com.example.pruebameli.data.mapper

import com.example.pruebameli.data.remote.dto.ProductDetailDto
import com.example.pruebameli.domain.models.ProductDetail

fun ProductDetailDto.toDomain(): ProductDetail =
    ProductDetail(
        id = id,
        name = name,
        imageUrls = pictures?.mapNotNull { it.url } ?: emptyList(),
        description = description
    )
