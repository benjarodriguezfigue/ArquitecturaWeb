package com.example.aplicacionferialibre

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarProductoScreen(onProductoAgregado: () -> Unit) {
    val nombre = remember { mutableStateOf("") }
    val precio = remember { mutableStateOf("") }
    val descripcion = remember { mutableStateOf("") }
    val stock = remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "anon"

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Agregar Producto", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = nombre.value,
                onValueChange = { nombre.value = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = precio.value,
                onValueChange = { precio.value = it },
                label = { Text("Precio") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = descripcion.value,
                onValueChange = { descripcion.value = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = stock.value,
                onValueChange = { stock.value = it },
                label = { Text("Stock disponible") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                val data = hashMapOf(
                    "nombre" to nombre.value,
                    "precio" to precio.value,
                    "descripcion" to descripcion.value,
                    "stock" to stock.value,
                    "feriante_uid" to uid
                )

                firestore.collection("productos")
                    .add(data)
                    .addOnSuccessListener {
                        scope.launch {
                            snackbarHostState.showSnackbar("Producto agregado correctamente")
                            onProductoAgregado()
                        }
                    }
                    .addOnFailureListener {
                        scope.launch {
                            snackbarHostState.showSnackbar("Error al guardar: ${it.message}")
                        }
                    }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Guardar Producto")
            }
        }
    }
}
