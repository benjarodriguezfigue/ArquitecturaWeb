package com.example.aplicacionferialibre.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

data class SolicitudEditable(
    val id: String,
    val feria: String,
    val productos: String,
    val fechaSolicitud: String,
    val nombrePuesto: String,
    var estado: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaGestionSolicitudes(onBack: () -> Unit) {
    val db = Firebase.firestore
    var solicitudesPendientes by remember { mutableStateOf(listOf<SolicitudEditable>()) }
    var solicitudesAceptadas by remember { mutableStateOf(listOf<SolicitudEditable>()) }
    var solapaSeleccionada by remember { mutableStateOf("pendientes") }
    var solicitudAEliminar by remember { mutableStateOf<SolicitudEditable?>(null) }

    LaunchedEffect(true) {
        db.collection("inscripciones")
            .get()
            .addOnSuccessListener { result ->
                val todas = result.map { doc ->
                    SolicitudEditable(
                        id = doc.id,
                        feria = doc.getString("feria") ?: "",
                        productos = doc.getString("productos") ?: "",
                        fechaSolicitud = doc.getString("fechaSolicitud") ?: "",
                        nombrePuesto = doc.getString("nombrePuesto") ?: "",
                        estado = doc.getString("estado") ?: ""
                    )
                }
                solicitudesPendientes = todas.filter { it.estado == "pendiente" }
                solicitudesAceptadas = todas.filter { it.estado == "aceptado" }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Solicitudes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { solapaSeleccionada = "pendientes" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (solapaSeleccionada == "pendientes") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Pendientes")
                }
                Button(
                    onClick = { solapaSeleccionada = "aceptadas" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (solapaSeleccionada == "aceptadas") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Aceptadas")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val listaActual = if (solapaSeleccionada == "pendientes") solicitudesPendientes else solicitudesAceptadas

            if (listaActual.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay solicitudes ${solapaSeleccionada}.")
                }
            } else {
                LazyColumn {
                    items(listaActual) { solicitud ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Feria: ${solicitud.feria}")
                                Text("Productos: ${solicitud.productos}")
                                Text("Fecha: ${solicitud.fechaSolicitud}")
                                Text("Estado: ${solicitud.estado}")
                                Text("Puesto: ${solicitud.nombrePuesto.ifBlank { "Sin nombre" }}")

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(onClick = {
                                        solicitudAEliminar = solicitud
                                    }) {
                                        Text("Eliminar")
                                    }

                                    if (solapaSeleccionada == "pendientes") {
                                        Button(onClick = {
                                            db.collection("inscripciones").document(solicitud.id)
                                                .update("estado", "aceptado")
                                                .addOnSuccessListener {
                                                    solicitudesPendientes = solicitudesPendientes.filterNot { it.id == solicitud.id }
                                                    solicitudesAceptadas += solicitud.copy(estado = "aceptado")
                                                }
                                        }) {
                                            Text("Aceptar")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Diálogo de confirmación
            solicitudAEliminar?.let { solicitud ->
                AlertDialog(
                    onDismissRequest = { solicitudAEliminar = null },
                    title = { Text("Confirmar eliminación") },
                    text = { Text("¿Estás seguro de eliminar esta solicitud?") },
                    confirmButton = {
                        TextButton(onClick = {
                            db.collection("inscripciones").document(solicitud.id)
                                .delete()
                                .addOnSuccessListener {
                                    solicitudesPendientes = solicitudesPendientes.filterNot { it.id == solicitud.id }
                                    solicitudesAceptadas = solicitudesAceptadas.filterNot { it.id == solicitud.id }
                                    solicitudAEliminar = null
                                }
                        }) {
                            Text("Eliminar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { solicitudAEliminar = null }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}