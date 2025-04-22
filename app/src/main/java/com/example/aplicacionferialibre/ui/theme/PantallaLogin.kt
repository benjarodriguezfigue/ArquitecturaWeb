package com.example.aplicacionferialibre.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore

@Composable
fun PantallaLogin() {
    val context = LocalContext.current
    val auth = Firebase.auth

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var tipoUsuario by remember { mutableStateOf("cliente") } // Valor por defecto


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Bienvenido a FeriaLibreApp", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre completo") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Soy: ")
            Spacer(modifier = Modifier.width(8.dp))
            DropdownMenuUsuario(tipoUsuario) { seleccionado ->
                tipoUsuario = seleccionado
            }
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    mensaje = if (task.isSuccessful) {
                        "Inicio de sesión exitoso"
                    } else {
                        "Error: ${task.exception?.message}"
                    }
                }
        }) {
            Text("Iniciar sesión")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        val db = Firebase.firestore

                        val datosUsuario = hashMapOf(
                            "email" to email,
                            "nombre" to nombre,
                            "tipo" to tipoUsuario
                        )

                        if (userId != null) {
                            db.collection("usuarios").document(userId).set(datosUsuario)
                                .addOnSuccessListener {
                                    mensaje = "Usuario creado y guardado correctamente"
                                }
                                .addOnFailureListener { e ->
                                    mensaje = "Error al guardar en Firestore: ${e.message}"
                                }
                        } else {
                            mensaje = "Error: No se obtuvo UID del usuario"
                        }
                    } else {
                        mensaje = "Error: ${task.exception?.message}"
                    }
                }
        }) {
            Text("Registrarse")
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (mensaje.isNotBlank()) {
            Text(text = mensaje, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun DropdownMenuUsuario(selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val opciones = listOf("cliente", "feriante")

    Box {
        Button(onClick = { expanded = true }) {
            Text("Tipo: ${selected}")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onSelected(opcion)
                        expanded = false
                    }
                )
            }
        }
    }
}

