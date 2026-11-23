package com.example.inventarioapp.feature.product.data.remote

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/** Unica clase que conoce las rutas de la coleccion de Firestore. */
class FirestoreProductDataSource @Inject constructor(
    private val db: FirebaseFirestore
) {
    private fun products(uid: String) = db.collection("users")
        .document(uid).collection("products")

    suspend fun push(uid: String, dto: ProductDto) {
        val docRef = products(uid).document(dto.id)
        try {
            // El ID del documento es el ID estable del producto local.
            val data = mapOf(
                "id" to dto.id,
                "name" to dto.name,
                "description" to dto.description,
                "priceCop" to dto.priceCop,
                "quantity" to dto.quantity,
                "updatedAt" to dto.updatedAt,
                "serverUpdatedAt" to FieldValue.serverTimestamp(),
                "isDeleted" to dto.isDeleted
            )
            docRef.set(data).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun fetchFromServer(uid: String): List<ProductDto> {
        val colRef = products(uid)
        return try {
            colRef.get(Source.SERVER).await().documents.mapNotNull { doc ->
                val id = doc.getString("id") ?: return@mapNotNull null
                ProductDto(
                    id = id,
                    name = doc.getString("name").orEmpty(),
                    description = doc.getString("description").orEmpty(),
                    priceCop = doc.getLong("priceCop") ?: 0L,
                    quantity = (doc.getLong("quantity") ?: 0L).toInt(),
                    updatedAt = doc.getLong("updatedAt") ?: 0L,
                    isDeleted = doc.getBoolean("isDeleted") ?: false
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, colRef.path)
            throw e
        }
    }
}
