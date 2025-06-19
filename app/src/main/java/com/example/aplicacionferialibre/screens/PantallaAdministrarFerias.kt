package com.example.aplicacionferialibre.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAdministrarFerias(
    onBack: () -> Unit,
    onEditarFeria: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()
    var ferias by remember { mutableStateOf(listOf<Pair<String, Map<String, Any>>>()) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        val snapshot = db.collection("ferias").get().await()
        ferias = snapshot.documents.map { it.id to (it.data ?: emptyMap()) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Administrar Ferias") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            items(ferias) { (id, feria) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("${feria["nombre"]} - ${feria["comuna"]}")
                        Text("Dirección: ${feria["direccion"]}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(onClick = { onEditarFeria(id) }) {
                                Text("Editar")
                            }
                            Button(
                                onClick = {
                                    scope.launch {
                                        try {
                                            db.collection("ferias").document(id).delete().await()
                                            ferias = ferias.filterNot { it.first == id }
                                            snackbarHostState.showSnackbar("Feria eliminada correctamente")
                                        } catch (e: Exception) {
                                            snackbarHostState.showSnackbar("Error al eliminar: ${e.message}")
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Eliminar")
                            }
                        }
                    }
                }
            }
        }
    }
}
