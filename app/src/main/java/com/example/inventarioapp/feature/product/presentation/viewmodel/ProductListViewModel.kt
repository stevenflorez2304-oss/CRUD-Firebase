package com.example.inventarioapp.feature.product.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventarioapp.feature.product.domain.repository.ProductRepository
import com.example.inventarioapp.feature.product.presentation.state.BrandFilter
import com.example.inventarioapp.feature.product.presentation.state.ProductListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {
    private val _ui = MutableStateFlow(ProductListUiState())
    val ui: StateFlow<ProductListUiState> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialInventoryIfNeeded()
            sync()
            repository.observeProducts()
                .catch { error ->
                    if (error is CancellationException) throw error
                    _ui.update { it.copy(message = error.message ?: "Error cargando inventario") }
                }
                .collect { products ->
                    _ui.update { it.copy(products = products) }
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _ui.update { it.copy(searchQuery = query) }
    }

    fun selectBrand(brand: BrandFilter) {
        _ui.update { it.copy(selectedBrand = brand) }
    }

    fun clearMessage() {
        _ui.update { it.copy(message = null) }
    }

    fun sync() = viewModelScope.launch {
        _ui.update { it.copy(isSyncing = true, message = null) }
        try {
            val report = repository.syncNow()
            _ui.update {
                it.copy(
                    message = "Sincronizado: ${report.uploaded} enviados, ${report.downloaded} en nube"
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _ui.update {
                it.copy(message = e.message ?: "Sin conexion: datos locales disponibles")
            }
        } finally {
            _ui.update { it.copy(isSyncing = false) }
        }
    }

    fun delete(id: String) = viewModelScope.launch {
        repository.delete(id)
        sync()
    }
}
