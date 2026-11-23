package com.example.inventarioapp.feature.product.data.repository

import android.content.Context
import com.example.inventarioapp.feature.product.data.local.ProductDao
import com.example.inventarioapp.feature.product.data.local.SneakerSeedData
import com.example.inventarioapp.feature.product.data.mapper.toDomain
import com.example.inventarioapp.feature.product.data.mapper.toDto
import com.example.inventarioapp.feature.product.data.mapper.toEntity
import com.example.inventarioapp.feature.product.data.remote.FirestoreProductDataSource
import com.example.inventarioapp.feature.product.domain.model.Product
import com.example.inventarioapp.feature.product.domain.repository.ProductRepository
import com.example.inventarioapp.feature.product.domain.repository.SyncReport
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val dao: ProductDao,
    private val remote: FirestoreProductDataSource,
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) : ProductRepository {
    private val syncMutex = Mutex()
    private val seedMutex = Mutex()

    private fun requireUserId(): String {
        val uid = auth.currentUser?.uid
        check(!uid.isNullOrBlank()) { "Debes iniciar sesion con Google para sincronizar." }
        return uid
    }

    override fun observeProducts(): Flow<List<Product>> =
        dao.observeVisible().map { rows -> rows.map { it.toDomain() } }

    override suspend fun seedInitialInventoryIfNeeded() = seedMutex.withLock {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_SEEDED, false)) return@withLock
        if (dao.countAll() == 0) {
            dao.upsertAll(SneakerSeedData.initialProducts())
        }
        prefs.edit().putBoolean(KEY_SEEDED, true).apply()
    }

    override suspend fun findById(id: String): Product? =
        dao.find(id)?.takeUnless { it.isDeleted }?.toDomain()

    override suspend fun save(product: Product) {
        // Guardamos primero en Room: se mantiene el modo offline.
        dao.upsert(
            product.copy(updatedAt = System.currentTimeMillis()).toEntity()
        )
    }

    override suspend fun delete(id: String) {
        val current = dao.find(id) ?: return
        // Tombstone: no borrar fisicamente antes de sincronizar.
        dao.upsert(
            current.copy(
                isDeleted = true,
                pending = true,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun syncNow(): SyncReport = syncMutex.withLock {
        val uid = requireUserId()

        var uploaded = 0
        for (local in dao.getPending()) {
            remote.push(uid, local.toDto())
            dao.markSynced(local.id, local.updatedAt)
            uploaded++
        }

        val cloudItems = remote.fetchFromServer(uid)
        for (dto in cloudItems) {
            val cloud = dto.toEntity()
            val local = dao.find(cloud.id)
            // Nunca sobrescribir una edicion que sigue pendiente.
            if (local?.pending == true) continue
            if (local == null || cloud.updatedAt > local.updatedAt) {
                dao.upsert(cloud)
            }
        }
        SyncReport(uploaded = uploaded, downloaded = cloudItems.size)
    }

    private companion object {
        const val PREFS_NAME = "inventario_seed_prefs"
        const val KEY_SEEDED = "sneakers_seeded_v1"
    }
}
