package com.example.aplicacionferialibre.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PantallaInscribirPuesto(onSubmitSuccess: () -> Unit = {}) {
    val db = Firebase.firestore
    val feriasDisponibles = remember { mutableStateListOf<String>() }
    var feriaSeleccionada by remember { mutableStateOf("") }
    var productos by remember { mutableStateOf("") }
    var fechaSolicitud by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val calendario = Calendar.getInstance()
    val fechaFormateada = remember { mutableStateOf("") }
    val context = LocalContext.current

    // Cargar ferias desde Firebase
    LaunchedEffect(Unit) {
        db.collection("ferias").get().addOnSuccessListener { result ->
            feriasDisponibles.clear()
            for (document in result) {
                val nombre = document.getString("nombre")
                if (nombre != null) {
                    feriasDisponibles.add(nombre)
                }
            }
            if (feriasDisponibles.isNotEmpty()) {
                feriaSeleccionada = feriasDisponibles.first()
            }
        }
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            fechaFormateada.value = formato.format(selectedCalendar.time)
        },
        calendario.get(Calendar.YEAR),
        calendario.get(Calendar.MONTH),
        calendario.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text("Inscripción de Puesto", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            Text("Selecciona la feria")
            DropdownMenuBox(
                items = feriasDisponibles,
                selected = feriaSeleccionada,
                onSelected = { feriaSeleccionada = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = productos,
                onValueChange = { productos = it },
                label = { Text("¿Qué productos vas a vender?") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Fecha de solicitud")
            OutlinedTextField(
                value = fechaFormateada.value,
                onValueChange = {},
                label = { Text("Selecciona una fecha") },
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { datePickerDialog.show() }) {
                Text("Seleccionar Fecha")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val userId = auth.currentUser?.uid
                    if (feriaSeleccionada.isBlank() || productos.isBlank() || fechaFormateada.value.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Todos los campos son obligatorios.")
                        }
                        return@Button
                    }

                    if (userId != null) {
                        val datos = mapOf(
                            "usuarioId" to userId,
                            "feria" to feriaSeleccionada,
                            "productos" to productos,
                            "fechaSolicitud" to fechaFormateada.value,
                            "estado" to "pendiente"
                        )
                        db.collection("inscripciones")
                            .add(datos)
                            .addOnSuccessListener {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Solicitud enviada")
                                    onSubmitSuccess()
                                }
                            }
                            .addOnFailureListener {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Error al registrar: ${it.message}")
                                }
                            }
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("Usuario no autenticado")
                        }
                    }
                },

                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enviar inscripción")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onSubmitSuccess() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Volver")
            }
        }
    }
}

@Composable
fun DropdownMenuBox(items: List<String>, selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            label = { Text("Feria") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { feria ->
                DropdownMenuItem(
                    text = { Text(feria) },
                    onClick = {
                        onSelected(feria)
                        expanded = false
                    }
                )
            }
        }
    }
}
