package com.example.pruebameli.domain.repository

import androidx.paging.PagingData
import com.example.pruebameli.domain.common.ResourceData
import com.example.pruebameli.domain.models.Product
import com.example.pruebameli.domain.models.ProductDetail
import com.example.pruebameli.domain.models.ProductSearchParams
import kotlinx.coroutines.flow.Flow

interface ProductsRepository {
    fun searchProductsPaged(params: ProductSearchParams): Flow<PagingData<Product>>
    suspend fun getProductDetail(id: String): ResourceData<ProductDetail>
}
