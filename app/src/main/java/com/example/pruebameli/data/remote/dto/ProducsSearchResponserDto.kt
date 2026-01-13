package com.example.pruebameli.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProductsSearchResponseDto(
    val keywords: String?,
    val paging: PagingDto,
    val results: List<ProductDto>,
    @Json(name = "used_attributes") val usedAttributes: List<AttributeDto>?,
    @Json(name = "query_type") val queryType: String?
)

@JsonClass(generateAdapter = true)
data class PagingDto(
    val total: Int,
    val limit: Int,
    val offset: Int
)

@JsonClass(generateAdapter = true)
data class ProductDto(
    val id: String,
    @Json(name = "date_created") val dateCreated: String?,
    @Json(name = "catalog_product_id") val catalogProductId: String?,
    val status: String?,
    @Json(name = "domain_id") val domainId: String?,
    val name: String,
    val attributes: List<AttributeDto>?,
    val pictures: List<PictureDto>?,
    @Json(name = "parent_id") val parentId: String?,
    @Json(name = "last_updated") val lastUpdated: String?,
    val type: String?,
    @Json(name = "site_id") val siteId: String?,
    val keywords: String?,
    val description: String?
)

@JsonClass(generateAdapter = true)
data class PictureDto(
    val id: String?,
    val url: String?
)

@JsonClass(generateAdapter = true)
data class AttributeDto(
    val id: String?,
    val name: String?,
    @Json(name = "value_id") val valueId: String?,
    @Json(name = "value_name") val valueName: String?
)

@JsonClass(generateAdapter = true)
data class ProductDetailDto(
    val id: String,
    val name: String,
    val pictures: List<PictureDto>?,
    val description: String?
)
