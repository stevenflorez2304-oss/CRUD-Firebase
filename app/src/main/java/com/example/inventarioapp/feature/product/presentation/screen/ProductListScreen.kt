package com.example.inventarioapp.feature.product.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.inventarioapp.feature.product.presentation.composables.ProductDeleteDialog
import com.example.inventarioapp.feature.product.presentation.section.ProductListSection
import com.example.inventarioapp.feature.product.presentation.viewmodel.ProductListViewModel

/** Pantalla contenedora: conecta ViewModel, secciones y navegacion. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    vm: ProductListViewModel,
    onCreate: () -> Unit,
    onEdit: (String) -> Unit
) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventario") },
                actions = { TextButton(onClick = { vm.sync() }) { Text("Sincronizar") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreate) { Text("+") }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            if (ui.isSyncing) LinearProgressIndicator(Modifier.fillMaxWidth())
            ui.message?.let { Text(it, Modifier.padding(12.dp)) }
            ProductListSection(
                products = ui.products,
                onEdit = onEdit,
                onAskDelete = { pendingDeleteId = it }
            )
        }
    }
    if (pendingDeleteId != null) ProductDeleteDialog(
        onConfirm = {
            pendingDeleteId?.let(vm::delete)
            pendingDeleteId = null
        },
        onDismiss = { pendingDeleteId = null }
    )
}
