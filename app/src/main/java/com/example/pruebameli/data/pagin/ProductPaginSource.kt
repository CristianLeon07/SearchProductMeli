package com.example.pruebameli.data.pagin

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.pruebameli.data.mapper.toDomain
import com.example.pruebameli.data.remote.SearchProductsApi
import com.example.pruebameli.domain.models.Product
import com.example.pruebameli.domain.models.ProductSearchParams
import kotlinx.coroutines.CancellationException


class ProductsPagingSource(
    private val api: SearchProductsApi,
    private val searchParams: ProductSearchParams
) : PagingSource<Int, Product>() {

    override fun getRefreshKey(state: PagingState<Int, Product>): Int? {
        // anchorPosition = ítem más cercano a la posición actual del scroll.
        val anchorPosition = state.anchorPosition ?: return null

        //Busca la página cargada más cercana a ese ítem.
        val page = state.closestPageToPosition(anchorPosition) ?: return null

        // Usa el pageSize configurado en PagingConfig.
        val pageSize = state.config.pageSize

        // Devuelve un offset aproximado para recargar cerca del scroll actual.
        return page.prevKey?.plus(pageSize)
            ?: page.nextKey?.minus(pageSize)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Product> {
        return try {
            // offset actual. Si es primera carga, iniciamos en el offset base.
            val offset = params.key ?: searchParams.offset

            // Con Paging puedemos pedir más items en la primera carga.
            // Por eso usamos params.loadSize como limit real de esta llamada.
            val limit = params.loadSize

            val response = api.searchProducts(
                query = searchParams.query,
                domainId = searchParams.domainId,
                siteId = searchParams.siteId,
                status = searchParams.status,
                offset = offset,
                limit = limit
            )

            if (!response.isSuccessful) {
                // HttpException permite a Paging reportar el error y que la UI muestre Retry.
                return LoadResult.Error(retrofit2.HttpException(response))
            }

            val body = response.body()
                ?: return LoadResult.Error(IllegalStateException("Body nulo en respuesta exitosa"))


            val products = body.results.map { it.toDomain() }

            // Total reportado por la API
            val total = body.paging.total

            // Cálculo de offsets para pagina anterior/siguiente
            val prevKey = if (offset == 0) null else maxOf(0, offset - limit)

            // nextOffset: avanzamos por la cantidad realmente recibida
            val nextOffset = offset + products.size

            // Si ya llegamos al total o no llegaron items, no hay más páginas
            val nextKey = if (products.isEmpty() || nextOffset >= total) null else nextOffset

            LoadResult.Page(
                data = products,
                prevKey = prevKey,
                nextKey = nextKey
            )

        } catch (e: CancellationException) {
            // Re-lanzar cancelaciones para que no se conviertan en errores
            throw e
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
