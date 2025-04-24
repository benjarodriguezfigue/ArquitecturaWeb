package com.example.aplicacionferialibre

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun PantallaLogin(navController: NavHostController, tipo: String) {
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
                }else{
                    auth.signInWithEmailAndPassword(correo, contrasena)
                        .addOnCompleteListener { task ->
                            scope.launch {
                                if (task.isSuccessful) {
                                    snackbarHostState.showSnackbar("Inicio de sesión exitoso")
                                    if (tipo == "usuario") navController.navigate("panel_usuario")
                                    else navController.navigate("panel_feriante")
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

            Button(onClick = {
                if (tipo == "usuario") navController.navigate("registro_usuario")
                else navController.navigate("registro_feriante")
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Registrarse")
            }
        }
    }
}
