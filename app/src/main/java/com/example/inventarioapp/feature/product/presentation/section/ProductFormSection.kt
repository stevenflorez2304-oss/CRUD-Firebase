package com.example.inventarioapp.feature.product.presentation.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.inventarioapp.feature.product.presentation.composables.ProductFormField
import com.example.inventarioapp.feature.product.presentation.state.ProductFormUiState

/** Seccion de formulario: solo recibe UiState y callbacks. */
@Composable
fun ProductFormSection(
    ui: ProductFormUiState,
    onName: (String) -> Unit,
    onDescription: (String) -> Unit,
    onPrice: (String) -> Unit,
    onQuantity: (String) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ProductFormField(
            label = "Nombre (ej. Nike Air Force 1)",
            value = ui.name,
            onValueChange = onName,
            testTagName = "product_name_input"
        )
        ProductFormField(
            label = "Descripcion (marca, talla, color)",
            value = ui.description,
            onValueChange = onDescription,
            singleLine = false,
            testTagName = "product_description_input"
        )
        ProductFormField(
            label = "Precio COP",
            value = ui.price,
            onValueChange = onPrice,
            keyboardType = KeyboardType.Number,
            testTagName = "product_price_input"
        )
        ProductFormField(
            label = "Cantidad",
            value = ui.quantity,
            onValueChange = onQuantity,
            keyboardType = KeyboardType.Number,
            testTagName = "product_quantity_input"
        )

        ui.error?.let { errorText ->
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = onSave,
            enabled = !ui.saving,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("save_product_button")
        ) {
            Text(if (ui.saving) "Guardando..." else "Guardar producto")
        }
    }
}
