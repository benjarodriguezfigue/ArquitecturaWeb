package com.example.aplicacionferialibre.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.graphics.BitmapFactory
import android.net.Uri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEditarProducto(
    puestoId: String,
    productoId: String,
    onProductoActualizado: () -> Unit,
    onCancelar: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var imagenUrl by remember { mutableStateOf<String?>(null) }
    var imagenBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    // Cargar datos del producto
    LaunchedEffect(true) {
        try {
            val doc = db.collection("puestos").document(puestoId)
                .collection("productos").document(productoId).get().await()

            nombre = doc.getString("nombre") ?: ""
            precio = doc.getDouble("precio")?.toString() ?: ""
            cantidad = doc.getLong("cantidad")?.toString() ?: ""
            descripcion = doc.getString("descripcion") ?: ""
            imagenUrl = doc.getString("imagenUrl")

            imagenUrl?.let {
                val bytes = storage.getReferenceFromUrl(it).getBytes(1024 * 1024).await()
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                imagenBitmap = bitmap.asImageBitmap()
            }
        } catch (e: Exception) {
            snackbarHostState.showSnackbar("Error al cargar producto")
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Editar Producto") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
            OutlinedTextField(value = precio, onValueChange = { precio = it }, label = { Text("Precio") })
            OutlinedTextField(value = cantidad, onValueChange = { cantidad = it }, label = { Text("Cantidad") })
            OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") })

            imagenBitmap?.let {
                Image(bitmap = it, contentDescription = null, modifier = Modifier.size(120.dp).align(Alignment.CenterHorizontally))
            }

            Button(onClick = {
                scope.launch {
                    val precioValido = precio.toDoubleOrNull()
                    if (nombre.isBlank() || precioValido == null) {
                        snackbarHostState.showSnackbar("Ingresa un nombre válido y un precio numérico")
                        return@launch
                    }
                    try {
                        val actualizacion = mapOf(
                            "nombre" to nombre,
                            "precio" to precio.toDouble(),
                            "cantidad" to (cantidad.toIntOrNull() ?: 0),
                            "descripcion" to descripcion
                        )
                        db.collection("puestos")
                            .document(puestoId)
                            .collection("productos")
                            .document(productoId)
                            .update(actualizacion)
                            .await()

                        snackbarHostState.showSnackbar("Producto actualizado")
                        onProductoActualizado()
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Error al actualizar producto")
                    }
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Guardar cambios")
            }

            OutlinedButton(onClick = onCancelar, modifier = Modifier.fillMaxWidth()) {
                Text("Cancelar")
            }
        }
    }
}
