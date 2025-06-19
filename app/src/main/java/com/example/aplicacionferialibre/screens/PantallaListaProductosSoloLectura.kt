package com.example.aplicacionferialibre.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaListaProductosSoloLectura(
    puestoId: String,
    feria: String,
    onVolver: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    var productos by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    LaunchedEffect(puestoId) {
        val snapshot = db.collection("puestos").document(puestoId).collection("productos").get().await()
        productos = snapshot.documents.mapNotNull { it.data }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Productos del Puesto") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (productos.isEmpty()) {
                Text("Este puesto aún no tiene productos disponibles.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(productos) { producto ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(12.dp)) {
                                // Imagen pequeña (si existe)
                                val imagenUrl = producto["imagenUrl"] as? String
                                if (!imagenUrl.isNullOrEmpty()) {
                                    Image(
                                        painter = rememberAsyncImagePainter(imagenUrl),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(64.dp)
                                            .padding(end = 12.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                // Detalle textual
                                Column {
                                    Text("Nombre: ${producto["nombre"]}")
                                    Text("Precio: \$${producto["precio"]}")
                                    Text("Cantidad: ${producto["cantidad"]}")
                                    Text("Descripción: ${producto["descripcion"] ?: "Sin descripción"}")
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
