package com.example.aplicacionferialibre.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

@Composable
fun PantallaLoginSimple(
    navController: NavHostController,
    tipo: String,
    onClose: () -> Unit,
    onLoginSuccess: (String, String) -> Unit) {
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
            Text("Inicio de sesión para $tipo", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo Electrónico") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = contrasena,
                onValueChange = { contrasena = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                if (correo.isBlank() || contrasena.isBlank()) {
                    scope.launch {
                        snackbarHostState.showSnackbar("Debes completar todos los campos")
                    }
                } else {
                    auth.signInWithEmailAndPassword(correo, contrasena)
                        .addOnCompleteListener { task ->
                            scope.launch {
                                if (task.isSuccessful) {
                                    val userId = auth.currentUser?.uid
                                    if (userId != null) {
                                        Firebase.firestore.collection("usuarios").document(userId).get()
                                            .addOnSuccessListener { document ->
                                                val nombre = document.getString("nombre") ?: "Usuario"
                                                val tipoUsuario = document.getString("tipo") ?: "usuario"
                                                onLoginSuccess(nombre, tipoUsuario)
                                                onClose()
                                            }
                                            .addOnFailureListener {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("No se pudo obtener los datos del usuario")
                                                    onClose()
                                                }
                                            }
                                    } else {
                                        snackbarHostState.showSnackbar("No se pudo obtener el ID del usuario")
                                    }
                                } else {
                                    snackbarHostState.showSnackbar("Error: ${task.exception?.message}")
                                }
                            }
                        }
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Iniciar Sesión")
            }


            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
                Text("Volver sin iniciar sesión")
            }

            Button(onClick = {
                onClose() // primero cierra el login
                navController.navigate("registro") // luego navega al registro
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Registrarse")
            }

        }
    }
}
