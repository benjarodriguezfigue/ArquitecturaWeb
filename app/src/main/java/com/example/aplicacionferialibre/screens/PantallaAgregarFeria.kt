package com.example.aplicacionferialibre.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAgregarFeria(onBack: () -> Unit) {
    var nombreFeria by remember { mutableStateOf("") }
    val db = Firebase.firestore
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar Nueva Feria") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = nombreFeria,
                onValueChange = { nombreFeria = it },
                label = { Text("Nombre de la feria") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (nombreFeria.isNotBlank()) {
                        db.collection("ferias").add(mapOf("nombre" to nombreFeria.trim()))
                            .addOnSuccessListener {
                                nombreFeria = ""
                                scope.launch {
                                    snackbarHostState.showSnackbar("Feria agregada correctamente")
                                }
                            }
                            .addOnFailureListener {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Error al agregar la feria: ${it.message}")
                                }
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Agregar Feria")
            }
        }
    }
}
