package com.example.aplicacionferialibre.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaProductosFeriante(
    puestoId: String,
    onVolver: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var productos by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(puestoId) {
        try {
            val snapshot = db.collection("puestos")
                .document(puestoId)
                .collection("productos")
                .get()
                .await()

            productos = snapshot.documents.map { it.data!! + ("id" to it.id) }
        } catch (e: Exception) {
            snackbarHostState.showSnackbar("Error al cargar productos")
        } finally {
            cargando = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mis Productos") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            if (cargando) {
                CircularProgressIndicator()
            } else if (productos.isEmpty()) {
                Text(
                    "No has agregado productos aún.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                LazyColumn {
                    items(productos) { producto ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Nombre: ${producto["nombre"]}")
                                Text("Precio: $${producto["precio"]}")
                                if ((producto["cantidad"] as Long) > 0) {
                                    Text("Cantidad: ${producto["cantidad"]}")
                                }
                                if ((producto["descripcion"] as? String)?.isNotBlank() == true) {
                                    Text("Descripción: ${producto["descripcion"]}")
                                }
                                if (producto["imagenUrl"] != null) {
                                    Image(
                                        painter = rememberAsyncImagePainter(producto["imagenUrl"]),
                                        contentDescription = "Imagen del producto",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(160.dp)
                                            .padding(top = 8.dp)
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                ) {
                                    IconButton(onClick = {
                                        // Futuro: abrir pantalla de edición
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Función editar no implementada aún")
                                        }
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                                    }

                                    IconButton(onClick = {
                                        scope.launch {
                                            try {
                                                db.collection("puestos")
                                                    .document(puestoId)
                                                    .collection("productos")
                                                    .document(producto["id"].toString())
                                                    .delete()
                                                    .await()
                                                productos = productos.filter { it["id"] != producto["id"] }
                                                snackbarHostState.showSnackbar("Producto eliminado")
                                            } catch (e: Exception) {
                                                snackbarHostState.showSnackbar("Error al eliminar")
                                            }
                                        }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onVolver,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver")
            }
        }
    }
}
