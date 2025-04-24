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
import com.google.firebase.FirebaseApp



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContent {
            AplicacionFeriaLibreTheme {
                PantallaUsuarioApp() // ← aquí llamas directamente a la nueva interfaz de usuario
            }
        }
    }
}


@Composable
fun AppNavigator() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "menu_inicial") {
        composable("menu_inicial") { MenuInicial(navController) }
        composable(
            "login/{tipo}",
            arguments = listOf(navArgument("tipo") { type = NavType.StringType })
        ) { backStackEntry ->
            PantallaLogin(navController, backStackEntry.arguments?.getString("tipo") ?: "usuario")
        }
        composable("panel_usuario") { PanelUsuario(navController) }
        composable("panel_feriante") { PanelFeriante(navController) }
        composable("registro_usuario") { RegistroUsuario() }
        composable("registro_feriante") { RegistroFeriante() }
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



