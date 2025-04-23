package com.example.aplicacionferialibre

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.aplicacionferialibre.ui.theme.AplicacionFeriaLibreTheme
import com.example.aplicacionferialibre.ui.theme.PantallaLogin
import com.google.firebase.FirebaseApp


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)


        enableEdgeToEdge()
        setContent {
            AplicacionFeriaLibreTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { PantallaLogin() }
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