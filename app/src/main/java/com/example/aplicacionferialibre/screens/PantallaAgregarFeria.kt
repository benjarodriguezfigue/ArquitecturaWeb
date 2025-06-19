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
import android.util.Log
import kotlinx.coroutines.withContext
import com.example.aplicacionferialibre.models.FeriaCompleta

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAgregarFeria(onBack: () -> Unit) {
    val db = Firebase.firestore
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var nombreFeria by remember { mutableStateOf("") }
    var comunaInput by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val comunasRM = listOf(
        "Alhué", "Buin", "Calera de Tango", "Cerrillos", "Cerro Navia", "Colina", "Conchalí",
        "Curacaví", "El Bosque", "El Monte", "Estación Central", "Huechuraba", "Independencia",
        "Isla de Maipo", "La Cisterna", "La Florida", "La Granja", "La Pintana", "La Reina",
        "Lampa", "Las Condes", "Lo Barnechea", "Lo Espejo", "Lo Prado", "Macul", "Maipú",
        "María Pinto", "Melipilla", "Ñuñoa", "Padre Hurtado", "Paine", "Pedro Aguirre Cerda",
        "Peñaflor", "Peñalolén", "Pirque", "Providencia", "Puente Alto", "Pudahuel", "Quilicura",
        "Quinta Normal", "Recoleta", "Renca", "San Bernardo", "San Joaquín", "San José de Maipo",
        "San Miguel", "San Pedro", "San Ramón", "Santiago", "Talagante", "Til Til", "Vitacura"
    )

    val comunasFiltradas = remember(comunaInput) {
        if (comunaInput.isBlank()) comunasRM
        else comunasRM.filter { it.contains(comunaInput, ignoreCase = true) }
    }

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = nombreFeria,
                onValueChange = { nombreFeria = it },
                label = { Text("Nombre de la feria") },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = expanded && comunasFiltradas.isNotEmpty(),
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = comunaInput,
                    onValueChange = {
                        comunaInput = it
                        expanded = true
                    },
                    label = { Text("Comuna") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    comunasFiltradas.forEach { comuna ->
                        DropdownMenuItem(
                            text = { Text(comuna) },
                            onClick = {
                                comunaInput = comuna
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text("Dirección (Ej: Av. Pajaritos 1234)") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (nombreFeria.isNotBlank() && direccion.isNotBlank() && comunaInput.isNotBlank()) {
                        val direccionCompleta = "$direccion, $comunaInput, Santiago, Chile"
                        val apiKey = "AIzaSyAZ4jOltAVybQ7XPeKJGSYNfiwH_KsRfjs"
                        val encodedDireccion =
                            java.net.URLEncoder.encode(direccionCompleta, "UTF-8")
                        val url =
                            "https://maps.googleapis.com/maps/api/geocode/json?address=$encodedDireccion&key=$apiKey"

                        scope.launch {
                            try {
                                val jsonString = withContext(kotlinx.coroutines.Dispatchers.IO) {
                                    val client = okhttp3.OkHttpClient()
                                    val request = okhttp3.Request.Builder().url(url).build()
                                    val response = client.newCall(request).execute()
                                    response.body?.string()
                                }

                                if (jsonString.isNullOrEmpty()) {
                                    Log.e("GEO_ERROR", "Respuesta vacía o null")
                                    snackbarHostState.showSnackbar("Error: respuesta vacía")
                                    return@launch
                                }

                                Log.d("GEO_JSON", jsonString)

                                val json = org.json.JSONObject(jsonString)
                                val results = json.getJSONArray("results")

                                if (results.length() == 0) {
                                    snackbarHostState.showSnackbar("No se encontró ubicación")
                                    return@launch
                                }

                                val location = results.getJSONObject(0)
                                    .getJSONObject("geometry")
                                    .getJSONObject("location")

                                val lat = location.getDouble("lat")
                                val lng = location.getDouble("lng")

                                val nuevaFeria = mapOf(
                                    "nombre" to nombreFeria,
                                    "comuna" to comunaInput,
                                    "direccion" to direccion,
                                    "latitud" to lat,
                                    "longitud" to lng
                                )

                                Firebase.firestore.collection("ferias").add(nuevaFeria)
                                    .addOnSuccessListener {
                                        nombreFeria = ""
                                        direccion = ""
                                        comunaInput = ""
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Feria agregada correctamente con ubicación")
                                        }
                                    }
                            } catch (e: Exception) {
                                Log.e("GEO_ERROR", "Excepción: ${e.message}", e)
                                snackbarHostState.showSnackbar("Error al obtener ubicación: ${e.message}")
                            }
                        }
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("Completa todos los campos")
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