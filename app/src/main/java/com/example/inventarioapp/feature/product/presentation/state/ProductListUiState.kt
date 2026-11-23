package com.example.inventarioapp.feature.product.presentation.state

import com.example.inventarioapp.feature.product.domain.model.Product

enum class BrandFilter(val label: String) {
    ALL("Todos"),
    NIKE("Nike"),
    NEW_BALANCE("New Balance"),
    ADIDAS("Adidas")
}

/** Datos que ProductListScreen necesita renderizar. */
data class ProductListUiState(
    val products: List<Product> = emptyList(),
    val searchQuery: String = "",
    val selectedBrand: BrandFilter = BrandFilter.ALL,
    val isSyncing: Boolean = false,
    val message: String? = null
) {
    val filteredProducts: List<Product>
        get() = products.filter { product ->
            val matchesBrand = when (selectedBrand) {
                BrandFilter.ALL -> true
                BrandFilter.NIKE -> product.name.contains("Nike", ignoreCase = true) ||
                    product.description.contains("Nike", ignoreCase = true)
                BrandFilter.NEW_BALANCE -> product.name.contains("New Balance", ignoreCase = true) ||
                    product.description.contains("New Balance", ignoreCase = true)
                BrandFilter.ADIDAS -> product.name.contains("Adidas", ignoreCase = true) ||
                    product.description.contains("Adidas", ignoreCase = true)
            }
            val query = searchQuery.trim()
            val matchesQuery = query.isEmpty() ||
                product.name.contains(query, ignoreCase = true) ||
                product.description.contains(query, ignoreCase = true)
            matchesBrand && matchesQuery
        }

    val totalUnits: Int
        get() = filteredProducts.sumOf { it.quantity }

    val totalInventoryCop: Long
        get() = filteredProducts.sumOf { it.priceCop * it.quantity }
}
