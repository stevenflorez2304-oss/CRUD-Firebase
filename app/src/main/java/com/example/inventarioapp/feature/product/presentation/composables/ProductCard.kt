package com.example.inventarioapp.feature.product.presentation.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.inventarioapp.feature.product.domain.model.Product
import java.text.NumberFormat
import java.util.Locale

/** Componente visual reutilizable: recibe datos y emite eventos. */
@Composable
fun ProductCard(
    product: Product,
    onEdit: () -> Unit,
    onAskDelete: () -> Unit
) {
    val format = remember {
        NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"))
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(product.name, style = MaterialTheme.typography.titleMedium)
            Text("${format.format(product.priceCop)} | Cantidad: ${product.quantity}")
            Row {
                TextButton(onClick = onEdit) { Text("Editar") }
                TextButton(onClick = onAskDelete) { Text("Eliminar") }
            }
        }
    }
}
