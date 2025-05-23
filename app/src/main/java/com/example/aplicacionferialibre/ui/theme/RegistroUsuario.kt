package com.example.aplicacionferialibre

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore

@Composable
fun RegistroUsuario(onCancel: () -> Unit = {}) {
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Registro de Usuario", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(value = correo, onValueChange = { correo = it }, label = { Text("Correo Electrónico") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(value = contrasena, onValueChange = { contrasena = it }, label = { Text("Contraseña") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                auth.createUserWithEmailAndPassword(correo, contrasena)
                    .addOnCompleteListener { task ->
                        scope.launch {
                            if (task.isSuccessful) {
                                val userId = auth.currentUser?.uid
                                val db = Firebase.firestore
                                val tipo = "usuario"

                                val datos = hashMapOf(
                                    "nombre" to nombre,
                                    "correo" to correo,
                                    "tipo" to tipo
                                )

                                if (userId != null) {
                                    db.collection("usuarios").document(userId).set(datos)
                                    snackbarHostState.showSnackbar("Usuario registrado correctamente")
                                } else {
                                    snackbarHostState.showSnackbar("No se pudo obtener el ID del usuario")
                                }
                            } else {
                                snackbarHostState.showSnackbar("Error: ${task.exception?.message}")
                            }
                        }
                    }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Registrarse")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                Text("Cancelar")
            }
        }
    }
}
