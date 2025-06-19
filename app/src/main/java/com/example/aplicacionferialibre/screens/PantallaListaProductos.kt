package com.example.aplicacionferialibre.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaListaProductos(
    puestoId: String,
    onEditarProducto: (productoId: String) -> Unit,
    onVolver: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var productos by remember { mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList()) }

    LaunchedEffect(puestoId) {
        val snapshot = db.collection("puestos").document(puestoId).collection("productos").get().await()
        productos = snapshot.documents.map { it.id to it.data.orEmpty() }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Tus Productos") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            if (productos.isEmpty()) {
                Text("No has agregado productos aún.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(productos) { (id, producto) ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Nombre: ${producto["nombre"]}")
                                Text("Precio: \$${producto["precio"]}")
                                Text("Cantidad: ${producto["cantidad"]}")
                                Text("Descripción: ${producto["descripcion"] ?: "Sin descripción"}")
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { onEditarProducto(id) }) {
                                        Text("Editar")
                                    }

                                    OutlinedButton(onClick = {
                                        scope.launch {
                                            try {
                                                db.collection("puestos")
                                                    .document(puestoId)
                                                    .collection("productos")
                                                    .document(id)
                                                    .delete()
                                                    .await()
                                                productos = productos.filterNot { it.first == id }
                                                snackbarHostState.showSnackbar("Producto eliminado")
                                            } catch (e: Exception) {
                                                snackbarHostState.showSnackbar("Error al eliminar producto")
                                            }
                                        }
                                    }) {
                                        Text("Eliminar")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
                Text("Volver")
            }
        }
    }
}
