package com.example.inventarioapp.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.inventarioapp.feature.auth.presentation.screen.AuthScreen
import com.example.inventarioapp.feature.auth.presentation.screen.signOut
import com.example.inventarioapp.feature.product.presentation.screen.ProductFormScreen
import com.example.inventarioapp.feature.product.presentation.screen.ProductListScreen
import com.example.inventarioapp.feature.product.presentation.viewmodel.ProductFormViewModel
import com.example.inventarioapp.feature.product.presentation.viewmodel.ProductListViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.Serializable

@Serializable
data object ProductListRoute : NavKey

@Serializable
data class ProductFormRoute(val id: String? = null) : NavKey

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        trySend(firebaseAuth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

@Composable
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        AuthScreen(onAuthSuccess = { /* authStateFlow actualiza la navegacion automaticamente */ })
    } else {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val credentialManager = remember(context) { CredentialManager.create(context) }

        key(user.uid) {
            val backStack = rememberNavBackStack(ProductListRoute)
            NavDisplay(
                backStack = backStack,
                onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                entryProvider = entryProvider {
                    entry<ProductListRoute> {
                        val vm: ProductListViewModel = hiltViewModel()
                        ProductListScreen(
                            vm = vm,
                            onCreate = { backStack.add(ProductFormRoute()) },
                            onEdit = { id -> backStack.add(ProductFormRoute(id)) },
                            onSignOut = { signOut(credentialManager, scope) }
                        )
                    }
                    entry<ProductFormRoute> { route ->
                        val vm: ProductFormViewModel = hiltViewModel()
                        ProductFormScreen(
                            vm = vm,
                            productId = route.id,
                            onDone = { backStack.removeLastOrNull() }
                        )
                    }
                }
            )
        }
    }
}
