package com.example.pruebameli.domain.models

data class Product(
    val id: String,
    val name: String,
    val imageUrl: String?,
    val brand: String?,
    val model: String?
)


data class ProductDetail(
    val id: String,
    val name: String,
    val imageUrls: List<String>,
    val description: String?
)