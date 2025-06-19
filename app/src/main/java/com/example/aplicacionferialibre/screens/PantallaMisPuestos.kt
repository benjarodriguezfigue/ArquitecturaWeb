package com.example.aplicacionferialibre.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import com.google.firebase.firestore.QuerySnapshot


@Composable
fun PantallaMisPuestos(userId: String,
                       onInscribir: () -> Unit,
                       onModificar: (String) -> Unit,
                       onVolver: () -> Unit) {
    val db = Firebase.firestore
    val auth = FirebaseAuth.getInstance()
    val usuarioId = auth.currentUser?.uid
    val snackbarHostState = remember { SnackbarHostState() }
    var puesto by remember { mutableStateOf<Map<String, Any>?>(null) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(usuarioId) {
        if (usuarioId != null) {
            db.collection("inscripciones")
                .whereEqualTo("usuarioId", usuarioId)
                .whereEqualTo("estado", "aceptado")
                .get()
                .addOnSuccessListener { docs ->
                    if (!docs.isEmpty) {
                        val doc = docs.documents.first()
                        puesto = doc.data!! + ("id" to doc.id)
                    }
                    cargando = false
                }
                .addOnFailureListener {
                    cargando = false
                }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text("Mis Puestos", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            when {
                cargando -> {
                    CircularProgressIndicator()
                }
                puesto == null -> {
                    Text("Aún no tienes un puesto asignado.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { onInscribir() }) {
                        Text("Inscribir un puesto")
                    }
                }
                else -> {
                    Text("Feria: ${puesto!!["feria"]}")
                    Text("Productos: ${puesto!!["productos"]}")
                    Text("Fecha de solicitud: ${puesto!!["fechaSolicitud"]}")
                    Spacer(modifier = Modifier.height(16.dp))

                    val puestoId = puesto?.get("id") as? String

                    if (puestoId != null) {
                        Button(onClick = {
                            onModificar(puestoId)
                        }) {
                            Text("Modificar productos")
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
