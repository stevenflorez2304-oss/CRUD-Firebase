package com.example.inventarioapp.feature.product.presentation.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.inventarioapp.feature.product.presentation.section.ProductFormSection
import com.example.inventarioapp.feature.product.presentation.viewmodel.ProductFormViewModel

/** La Screen orquesta estado, estructura Scaffold y salida del formulario. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    vm: ProductFormViewModel,
    productId: String?,
    onDone: () -> Unit
) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    LaunchedEffect(productId) { vm.load(productId) }
    LaunchedEffect(ui.saved) { if (ui.saved) onDone() }
    Scaffold(topBar = {
        TopAppBar(title = {
            Text(if (productId == null) "Nuevo producto" else "Editar producto")
        })
    }) { padding ->
        androidx.compose.foundation.layout.Box(Modifier.padding(padding)) {
            ProductFormSection(
                ui = ui,
                onName = vm::updateName,
                onDescription = vm::updateDescription,
                onPrice = vm::updatePrice,
                onQuantity = vm::updateQuantity,
                onSave = vm::save
            )
        }
    }
}
