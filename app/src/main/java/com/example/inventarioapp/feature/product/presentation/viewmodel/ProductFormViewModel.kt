package com.example.inventarioapp.feature.product.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventarioapp.feature.product.domain.model.Product
import com.example.inventarioapp.feature.product.domain.repository.ProductRepository
import com.example.inventarioapp.feature.product.domain.usecase.SaveProductUseCase
import com.example.inventarioapp.feature.product.presentation.state.ProductFormUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ProductFormViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val saveProduct: SaveProductUseCase
) : ViewModel() {
    private val _ui = MutableStateFlow(ProductFormUiState())
    val ui: StateFlow<ProductFormUiState> = _ui
    private var editingId: String? = null
    private var loaded = false

    fun load(id: String?) {
        if (loaded) return
        loaded = true
        editingId = id
        if (id == null) return
        viewModelScope.launch {
            val product = repository.findById(id) ?: return@launch
            _ui.value = ProductFormUiState(
                product.name, product.description,
                product.priceCop.toString(), product.quantity.toString()
            )
        }
    }

    fun updateName(v: String) { _ui.value = _ui.value.copy(name = v) }
    fun updateDescription(v: String) {
        _ui.value = _ui.value.copy(description = v)
    }
    fun updatePrice(v: String) { _ui.value = _ui.value.copy(price = v) }
    fun updateQuantity(v: String) {
        _ui.value = _ui.value.copy(quantity = v)
    }

    fun save() = viewModelScope.launch {
        val current = _ui.value
        val price = current.price.toLongOrNull()
        val quantity = current.quantity.toIntOrNull()
        if (price == null || quantity == null) {
            _ui.value = current.copy(error = "Precio y cantidad numericos")
            return@launch
        }
        _ui.value = current.copy(saving = true, error = null)
        try {
            saveProduct(
                Product(
                    editingId ?: UUID.randomUUID().toString(),
                    current.name.trim(), current.description.trim(),
                    price, quantity
                )
            )
            // Guardado local exitoso incluso si no hay Internet.
            _ui.value = _ui.value.copy(saved = true, saving = false)
            // El usuario usa Sincronizar en la lista cuando necesite subir cambios.
            // Para automatizar reintentos en produccion, usar WorkManager.
        } catch (e: Exception) {
            _ui.value = _ui.value.copy(
                saving = false, error = e.message ?: "Error de guardado"
            )
        }
    }
}
