package com.example.inventarioapp.feature.product.presentation.composables

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

/** Modal solo visual. La operacion real se decide en la Screen. */
@Composable
fun ProductDeleteDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar producto") },
        text = { Text("Se ocultara localmente y se eliminara al sincronizar.") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Eliminar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
