package com.example.aplicacionferialibre

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun PanelUsuario(navController: NavHostController) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Bienvenido al Panel de Usuario")
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Button(onClick = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate("mapa") {
                    popUpTo(0) { inclusive = true }
                }
            }) {
                Text("Cerrar Sesión")
            }
        }
    }
}