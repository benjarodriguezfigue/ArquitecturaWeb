package com.example.aplicacionferialibre


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import com.example.aplicacionferialibre.ui.theme.AplicacionFeriaLibreTheme
import androidx.navigation.navArgument
import androidx.navigation.NavHostController
import com.example.aplicacionferialibre.screens.PantallaAdministrarFerias
import com.example.aplicacionferialibre.screens.PantallaEditarFeria
import com.example.aplicacionferialibre.screens.PantallaRegistro
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.tasks.await
import com.example.aplicacionferialibre.models.FeriaCompleta
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.util.Log


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContent {
            AplicacionFeriaLibreTheme {
                PantallaUsuarioApp()
            }
        }
    }
}


@Composable
fun AppNavigator() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "panel_usuario") {
        composable("menu_inicial") { MenuInicial(navController) }
        composable(
            "login/{tipo}",
            arguments = listOf(navArgument("tipo") { type = NavType.StringType })
        ) { backStackEntry ->
            PantallaLogin(navController, backStackEntry.arguments?.getString("tipo") ?: "usuario")
        }
        composable("panel_usuario") { PantallaUsuarioApp() }

        composable("registro"){ PantallaRegistro{navController.popBackStack()} }

        composable("administrarFerias") {
            PantallaAdministrarFerias(
                onBack = { navController.popBackStack() },
                onEditarFeria = { feriaId -> navController.navigate("editarFeria/$feriaId") }
            )
        }

        composable(
            "editarFeria/{feriaId}",
            arguments = listOf(navArgument("feriaId") { type = NavType.StringType })
        ) { backStackEntry ->
            val feriaId = backStackEntry.arguments?.getString("feriaId") ?: return@composable
            PantallaEditarFeriaWrapper(feriaId = feriaId, onVolver = { navController.popBackStack() })
        }

    }
}

@Composable
fun PantallaEditarFeriaWrapper(
    feriaId: String,
    onVolver: () -> Unit
) {
    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
    var feriaActual by remember { mutableStateOf<FeriaCompleta?>(null) }

    LaunchedEffect(feriaId) {
        try {
            val snapshot = db.collection("ferias").document(feriaId).get().await()
            val data = snapshot.data
            if (data != null) {
                feriaActual = FeriaCompleta(
                    nombre = data["nombre"] as? String ?: "",
                    comuna = data["comuna"] as? String ?: "",
                    direccion = data["direccion"] as? String ?: "",
                    latitud = data["latitud"] as? Double ?: 0.0,
                    longitud = data["longitud"] as? Double ?: 0.0
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("EDITFERIA", "Error al obtener feria: ${e.message}")
        }
    }

    if (feriaActual != null) {
        PantallaEditarFeria(
            feriaId = feriaId,
            feriaActual = feriaActual!!,
            onVolver = onVolver
        )
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}


@Composable
fun MenuInicial(navController: NavHostController) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { navController.navigate("login/usuario") }, modifier = Modifier.padding(8.dp)) {
                Text("Ingresar como Usuario")
            }
            Button(onClick = { navController.navigate("login/feriante") }, modifier = Modifier.padding(8.dp)) {
                Text("Ingresar como Feriante")
            }
        }
    }
}



