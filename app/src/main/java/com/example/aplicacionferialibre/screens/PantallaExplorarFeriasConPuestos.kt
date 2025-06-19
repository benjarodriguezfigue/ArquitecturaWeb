package com.example.aplicacionferialibre.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun PantallaExplorarFeriasConPuestos(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var ferias by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var feriaSeleccionada by remember { mutableStateOf<String?>(null) }
    var puestos by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var puestoSeleccionado by remember { mutableStateOf<Pair<String, String>?>(null) } // Pair<puestoId, feriaId>

    LaunchedEffect(true) {
        val snapshot = db.collection("ferias").get().await()
        ferias = snapshot.documents.map { it.data.orEmpty() + ("id" to it.id) }
    }

    LaunchedEffect(feriaSeleccionada) {
        if (feriaSeleccionada != null) {
            val snapshot = db.collection("inscripciones")
                .whereEqualTo("feria", ferias.first { it["id"] == feriaSeleccionada }["nombre"])
                .whereEqualTo("estado", "aceptado")
                .get().await()
            puestos = snapshot.documents.map { it.data.orEmpty() + ("id" to it.id) }
        }
    }

    Surface(modifier = Modifier.padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Ferias Activas", style = MaterialTheme.typography.titleLarge)

            if (feriaSeleccionada == null) {
                ferias.forEach { feria ->
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .clickable { feriaSeleccionada = feria["id"] as String }) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Nombre: ${feria["nombre"]}")
                            Text("Comuna: ${feria["comuna"]}")
                            Text("Dirección: ${feria["direccion"]}")
                        }
                    }
                }
            } else if (puestoSeleccionado == null) {
                Button(onClick = { feriaSeleccionada = null }) {
                    Text("Volver a Ferias")
                }
                puestos.forEach { puesto ->
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val puestoId = puesto["id"] as String
                            puestoSeleccionado = puestoId to feriaSeleccionada!!
                        }) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Nombre del Puesto: ${puesto["nombrePuesto"] ?: "Sin nombre"}")
                            Text("Tipo de productos: ${puesto["productos"] ?: "Sin info"}")
                        }
                    }
                }
            } else {
                Button(onClick = { puestoSeleccionado = null }) {
                    Text("Volver a Puestos")
                }
                PantallaListaProductosSoloLectura(
                    puestoId = puestoSeleccionado!!.first,
                    feria = puestoSeleccionado!!.second,
                    onVolver = { puestoSeleccionado = null }
                )
            }
        }
    }
}
