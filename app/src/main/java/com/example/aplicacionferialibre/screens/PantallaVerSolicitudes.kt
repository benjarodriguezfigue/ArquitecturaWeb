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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

data class Solicitud(
    val feria: String = "",
    val productos: String = "",
    val fechaSolicitud: String = "",
    val estado: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaVerSolicitudes(onBack: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid
    val db = Firebase.firestore
    var solicitudes by remember { mutableStateOf(listOf<Solicitud>()) }

    LaunchedEffect(userId) {
        if (userId != null) {
            db.collection("inscripciones")
                .whereEqualTo("usuarioId", userId)
                .get()
                .addOnSuccessListener { result ->
                    solicitudes = result.map { doc ->
                        Solicitud(
                            feria = doc.getString("feria") ?: "",
                            productos = doc.getString("productos") ?: "",
                            fechaSolicitud = doc.getString("fechaSolicitud") ?: "",
                            estado = doc.getString("estado") ?: ""
                        )
                    }
                }
        }
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis solicitudes") },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (solicitudes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No has enviado solicitudes aún.")
                }
            } else {
                LazyColumn {
                    items(solicitudes) { solicitud ->
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
                            }
                        }
                    }
                }
            }
        }
    }
}