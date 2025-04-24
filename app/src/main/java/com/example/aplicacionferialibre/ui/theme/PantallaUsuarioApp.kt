package com.example.aplicacionferialibre

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.google.maps.android.compose.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng

sealed class UsuarioScreen(val route: String, val label: String, val icon: ImageVector) {
    object Mapa : UsuarioScreen("mapa", "Mapa", Icons.Filled.Place)
    object Puestos : UsuarioScreen("puestos", "Puestos", Icons.Filled.ShoppingCart)
    object Login : UsuarioScreen("login", "Login", Icons.Filled.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaUsuarioApp() {
    val navController = rememberNavController()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ferias Cercanas") },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(UsuarioScreen.Login.route) {
                            popUpTo(UsuarioScreen.Mapa.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }) {
                        Icon(imageVector = UsuarioScreen.Login.icon, contentDescription = "Login")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = UsuarioScreen.Mapa.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(UsuarioScreen.Mapa.route) { PantallaMapa() }
            composable(UsuarioScreen.Puestos.route) { PantallaExplorarPuestos() }
            composable(UsuarioScreen.Login.route) { PantallaLogin(navController, "usuario") }
            composable("registro_usuario") { RegistroUsuario() }
            composable("panel_usuario") { PanelUsuario(navController) }
            composable("registro_feriante") { RegistroFeriante() }
            composable("panel_feriante") { PanelFeriante(navController) }
            composable("agregar_producto") {
                AgregarProductoScreen { navController.popBackStack() }
            }

        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        UsuarioScreen.Mapa,
        UsuarioScreen.Puestos
    )
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}


@Composable
fun PantallaMapa() {
    val santiago = LatLng(-33.4489, -70.6693)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(santiago, 12f)
    }


    GoogleMap(
        modifier = Modifier.padding(16.dp),
        cameraPositionState = cameraPositionState
    ) {
        Marker(
            state = MarkerState(position = santiago),
            title = "Feria Central Santiago"
        )
    }
}

@Composable
fun PantallaExplorarPuestos() {
    Surface(modifier = Modifier.padding(16.dp)) {
        Text("Aquí se mostrarán los puestos y productos 🛍️")
    }
}
