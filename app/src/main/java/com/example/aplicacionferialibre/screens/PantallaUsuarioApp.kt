package com.example.aplicacionferialibre

// Composición y estado
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.*

// Layouts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

// Composables Material
import androidx.compose.material3.*

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.aplicacionferialibre.screens.PantallaAgregarFeria
import com.example.aplicacionferialibre.screens.PantallaGestionSolicitudes
import com.example.aplicacionferialibre.screens.PantallaInscribirPuesto
import com.example.aplicacionferialibre.screens.PantallaLoginSimple
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.example.aplicacionferialibre.screens.PantallaRegistro
import com.example.aplicacionferialibre.screens.PantallaVerSolicitudes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

sealed class UsuarioScreen(val route: String, val label: String, val icon: ImageVector) {
    object Mapa : UsuarioScreen("mapa", "Mapa", Icons.Filled.Place)
    object Puestos : UsuarioScreen("puestos", "Puestos", Icons.Filled.ShoppingCart)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaUsuarioApp() {
    val navController = rememberNavController()
    var mostrarLogin by remember { mutableStateOf(false) }
    var nombreUsuario by remember { mutableStateOf<String?>(null) }
    var tipoUsuario by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (nombreUsuario != null && tipoUsuario != null) {
                        Text("Bienvenido $tipoUsuario: $nombreUsuario")
                    } else {
                        Text("Ferias Cercanas")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        mostrarLogin = true
                    }) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "Cuenta")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = UsuarioScreen.Mapa.route
            ) {
                composable(UsuarioScreen.Mapa.route) {
                    PantallaMapa()
                }

                composable(UsuarioScreen.Puestos.route) {
                    PantallaExplorarPuestos()
                }

                composable("registro") {
                    PantallaRegistro {
                        navController.popBackStack()
                    }
                }

                composable("inscribir_puesto") {
                    PantallaInscribirPuesto(onSubmitSuccess = {
                        navController.navigate(UsuarioScreen.Mapa.route)
                    })
                }
                composable("ver_solicitudes") {
                    PantallaVerSolicitudes(
                        onBack = { navController.navigate(UsuarioScreen.Mapa.route) }
                    )
                }

                composable("gestionar_solicitudes") {
                    PantallaGestionSolicitudes(
                        onBack = { navController.navigate(UsuarioScreen.Mapa.route) }
                    )
                }

                composable("agregar_feria") {
                    PantallaAgregarFeria(onBack = { navController.navigate(UsuarioScreen.Mapa.route) })
                }
            }

            if (mostrarLogin) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
                ) {
                    if (nombreUsuario == null) {
                        PantallaLoginSimple(
                            navController = navController,
                            tipo = "usuario",
                            onClose = { mostrarLogin = false },
                            onLoginSuccess = { nombre, tipo ->
                                nombreUsuario = nombre
                                tipoUsuario = tipo
                                mostrarLogin = false
                            }
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Sesión activa como $tipoUsuario: $nombreUsuario")
                            Spacer(modifier = Modifier.height(16.dp))

                            if (tipoUsuario == "feriante") {
                                Button(onClick = {
                                    navController.navigate("inscribir_puesto")
                                    mostrarLogin = false
                                }) {
                                    Text("¡Inscribe tu puesto!")
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                Button(onClick = {
                                    navController.navigate("ver_solicitudes")
                                    mostrarLogin = false
                                }) {
                                    Text("Ver solicitudes enviadas")
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            if (FirebaseAuth.getInstance().currentUser?.email == "admin@admin.cl") {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = {
                                    navController.navigate("gestionar_solicitudes")
                                    mostrarLogin = false
                                }) {
                                    Text("Gestionar solicitudes (Admin)")
                                }

                                    Button(onClick = {
                                        navController.navigate("agregar_feria")
                                        mostrarLogin = false
                                    }) {
                                        Text("Agregar nueva feria")
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                            }

                            Button(onClick = {
                                mostrarLogin = false
                            }) {
                                Text("Volver")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(onClick = {
                                nombreUsuario = null
                                tipoUsuario = null
                                mostrarLogin = false
                            }) {
                                Text("Cerrar sesión")
                            }
                        }
                    }
                }
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
    val db = Firebase.firestore
    var feriantesAceptados by remember { mutableStateOf(listOf<Map<String, String>>()) }

    LaunchedEffect(Unit) {
        db.collection("inscripciones")
            .whereEqualTo("estado", "aceptado")
            .get()
            .addOnSuccessListener { result ->
                feriantesAceptados = result.map { doc ->
                    mapOf(
                        "feria" to (doc.getString("feria") ?: ""),
                        "productos" to (doc.getString("productos") ?: "")
                    )
                }
            }
    }

    Surface(modifier = Modifier.padding(16.dp)) {
        Column {
            Text("Feriantes Activos 🛒", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            if (feriantesAceptados.isEmpty()) {
                Text("Aún no hay feriantes aceptados.")
            } else {
                feriantesAceptados.forEach { feria ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Feria: ${feria["feria"]}")
                            Text("Productos: ${feria["productos"]}")
                        }
                    }
                }
            }
        }
    }
}