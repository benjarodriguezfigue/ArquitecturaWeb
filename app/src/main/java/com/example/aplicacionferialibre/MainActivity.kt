package com.example.aplicacionferialibre

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import com.example.aplicacionferialibre.ui.theme.SplashScreen
import androidx.compose.runtime.*
import androidx.compose.material3.*
import com.example.aplicacionferialibre.ui.theme.AplicacionFeriaLibreTheme
import com.example.aplicacionferialibre.ui.theme.PantallaLogin
import com.google.firebase.FirebaseApp


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        enableEdgeToEdge()
        setContent {
            var mostrarSplash by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                delay(2000)
                mostrarSplash = false
            }

            AplicacionFeriaLibreTheme {
                if (mostrarSplash) {
                    SplashScreen(onTimeout = { mostrarSplash = false })
                } else {
                    Scaffold(modifier = Modifier.fillMaxSize()) {
                        PantallaLogin()
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AplicacionFeriaLibreTheme {
        Greeting("FeriaLibreApp")
    }
}