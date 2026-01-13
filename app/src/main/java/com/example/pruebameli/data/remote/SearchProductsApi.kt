package com.example.pruebameli.data.remote

import com.example.pruebameli.data.remote.dto.ProductDetailDto
import com.example.pruebameli.data.remote.dto.ProductsSearchResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SearchProductsApi {

    @GET("products/search")
    suspend fun searchProducts(
        @Query("q") query: String,
        @Query("domain_id") domainId: String?,
        @Query("site_id") siteId: String,
        @Query("status") status: String?,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): Response<ProductsSearchResponseDto>

    @GET("products/{id}")
    suspend fun getProductDetail(
        @Path("id") id: String
    ): Response<ProductDetailDto>
}
