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
import com.example.aplicacionferialibre.screens.PantallaRegistro
import com.google.firebase.FirebaseApp



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
        //composable("panel_feriante") { PanelFeriante(navController) }
        composable("registro"){ PantallaRegistro{navController.popBackStack()} }
        //composable("registro_usuario") { RegistroUsuario{navController.popBackStack()} }
        //composable("registro_feriante") { RegistroFeriante{navController.popBackStack()} }
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



