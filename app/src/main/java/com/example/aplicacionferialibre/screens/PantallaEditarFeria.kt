package com.example.aplicacionferialibre.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.net.URLEncoder
import org.json.JSONObject
import okhttp3.OkHttpClient
import okhttp3.Request
import androidx.compose.ui.Alignment
import kotlinx.coroutines.tasks.await
import com.example.aplicacionferialibre.models.FeriaCompleta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEditarFeria(
    feriaId: String,
    feriaActual: FeriaCompleta,
    onVolver: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val db = FirebaseFirestore.getInstance()

    var nombre by remember { mutableStateOf(feriaActual.nombre) }
    var comuna by remember { mutableStateOf(feriaActual.comuna) }
    var direccion by remember { mutableStateOf(feriaActual.direccion) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Feria") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
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
            OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = comuna, onValueChange = { comuna = it }, label = { Text("Comuna") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = direccion, onValueChange = { direccion = it }, label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth())

            Button(
                onClick = {
                    scope.launch {
                        try {
                            val direccionCompleta = "$direccion, $comuna, Santiago, Región Metropolitana, Chile"
                            val encoded = URLEncoder.encode(direccionCompleta, "UTF-8")
                            val apiKey = "AIzaSyAZ4jOltAVybQ7XPeKJGSYNfiwH_KsRfjs"
                            val url = "https://maps.googleapis.com/maps/api/geocode/json?address=$encoded&key=$apiKey"

                            val bodyString = withContext(Dispatchers.IO) {
                                val client = OkHttpClient()
                                val request = Request.Builder().url(url).build()
                                val response = client.newCall(request).execute()
                                response.body?.string()
                            }

                            if (bodyString.isNullOrEmpty()) {
                                snackbarHostState.showSnackbar("Respuesta vacía del servidor")
                                return@launch
                            }

                            val json = JSONObject(bodyString)
                            val results = json.getJSONArray("results")

                            if (results.length() == 0) {
                                snackbarHostState.showSnackbar("Dirección no válida.")
                                return@launch
                            }

                            val location = results.getJSONObject(0).getJSONObject("geometry").getJSONObject("location")
                            val lat = location.getDouble("lat")
                            val lng = location.getDouble("lng")

                            val datosActualizados = mapOf(
                                "nombre" to nombre,
                                "comuna" to comuna,
                                "direccion" to direccion,
                                "latitud" to lat,
                                "longitud" to lng
                            )

                            FirebaseFirestore.getInstance().collection("ferias").document(feriaId).update(datosActualizados).await()
                            snackbarHostState.showSnackbar("Feria actualizada correctamente")
                            onVolver()
                        } catch (e: Exception) {
                            Log.e("EDIT_FERIA", "Error al actualizar", e)
                            snackbarHostState.showSnackbar("Error al actualizar: ${e.message ?: "desconocido"}")
                        }
                    }
                }
,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar cambios")
            }

            OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
                Text("Volver")
            }
        }
    }
}
