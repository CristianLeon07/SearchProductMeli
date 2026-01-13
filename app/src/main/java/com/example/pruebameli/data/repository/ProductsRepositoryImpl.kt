package com.example.pruebameli.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.pruebameli.core.utils.ApiErrorMapper
import com.example.pruebameli.domain.common.ResourceData
import com.example.pruebameli.data.mapper.toDomain
import com.example.pruebameli.data.pagin.ProductsPagingSource
import com.example.pruebameli.data.remote.SearchProductsApi
import com.example.pruebameli.domain.models.Product
import com.example.pruebameli.domain.models.ProductDetail
import com.example.pruebameli.domain.repository.ProductsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class ProductsRepositoryImpl @Inject constructor(
    private val api: SearchProductsApi
) : ProductsRepository {

    override fun searchProductsPaged(
        params: com.example.pruebameli.domain.models.ProductSearchParams
    ): Flow<PagingData<Product>> {

        // Validamos que el pageSize esté dentro de los límites permitidos
        val pageSize = params.limit.coerceIn(
            com.example.pruebameli.domain.config.AppConfig.Search.MIN_PAGE_SIZE,
            com.example.pruebameli.domain.config.AppConfig.Search.MAX_PAGE_SIZE
        )

        return Pager(
            config = PagingConfig(
                pageSize = pageSize,

                // Primera carga trae 2 páginas para que la lista se vea llena más rápido
                initialLoadSize = pageSize * 2,

                // Prefetch: cuando falten pageSize items para terminar, pide la siguiente página
                prefetchDistance = pageSize,

                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                // PagingSource recibe tod el params, incluyendo siteId/domainId/status
                ProductsPagingSource(
                    api = api,
                    searchParams = params.copy(limit = pageSize)
                )
            }
        ).flow
    }

    override suspend fun getProductDetail(id: String): ResourceData<ProductDetail> {
        return try {
            val response = api.getProductDetail(id)

            if (!response.isSuccessful) {
                return ApiErrorMapper.fromHttp(
                    code = response.code(),
                    rawMessage = response.message()
                )
            }

            val dto = response.body()
                ?: return ResourceData.Error(
                    message = "Respuesta vacía del servidor.",
                    code = response.code()
                )

            ResourceData.Success(dto.toDomain())

        } catch (t: Throwable) {
            ApiErrorMapper.fromThrowable(t)
        }
    }

}
