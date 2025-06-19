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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.util.copy
import com.example.aplicacionferialibre.screens.PantallaAgregarFeria
import com.example.aplicacionferialibre.screens.PantallaGestionSolicitudes
import com.example.aplicacionferialibre.screens.PantallaInscribirPuesto
import com.example.aplicacionferialibre.screens.PantallaLoginSimple
import com.example.aplicacionferialibre.screens.SolicitarPermisosUbicacion
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.example.aplicacionferialibre.screens.PantallaRegistro
import com.example.aplicacionferialibre.screens.PantallaVerSolicitudes
import com.example.aplicacionferialibre.models.FeriaCompleta
import com.example.aplicacionferialibre.screens.PantallaAdministrarFerias
import com.example.aplicacionferialibre.screens.PantallaAgregarProducto
import com.example.aplicacionferialibre.screens.PantallaMisPuestos
import com.example.aplicacionferialibre.screens.PantallaListaProductos
import com.example.aplicacionferialibre.screens.PantallaEditarProducto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.firebase.auth.ktx.auth


sealed class UsuarioScreen(val route: String, val label: String, val icon: ImageVector) {
    object Mapa : UsuarioScreen("mapa", "Mapa", Icons.Filled.Place)
    object Puestos : UsuarioScreen("puestos", "Puestos", Icons.Filled.ShoppingCart)

    data class Custom(val customRoute: String, val customLabel: String, val customIcon: ImageVector)
        : UsuarioScreen(customRoute, customLabel, customIcon)
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
            BottomNavigationBar(navController, tipoUsuario)
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

                composable("mis_puestos") {
                    PantallaMisPuestos(
                        userId = Firebase.auth.currentUser?.uid ?: "",
                        onModificar = { puestoId ->
                            navController.navigate("agregar_producto/$puestoId") },
                        onInscribir = {
                            navController.navigate("inscribir_puesto")
                        },
                        onVerProductos = { puestoId ->
                            navController.navigate("ver_productos/$puestoId")
                        },
                        onVolver = { navController.navigate("mapa") }
                    )
                }

                composable("agregar_producto/{puestoId}") { backStackEntry ->
                    val puestoId = backStackEntry.arguments?.getString("puestoId") ?: ""
                    PantallaAgregarProducto(
                        puestoId = puestoId,
                        onProductoAgregado = { navController.popBackStack() },
                        onCancelar = { navController.popBackStack() }
                    )
                }

                composable("ver_productos/{puestoId}") { backStackEntry ->
                    val puestoId = backStackEntry.arguments?.getString("puestoId") ?: ""
                    PantallaListaProductos(
                        puestoId = puestoId,
                        onEditarProducto = { productoId ->
                            navController.navigate("editar_producto/$puestoId/$productoId")
                        },
                        onVolver = { navController.popBackStack() }
                    )
                }

                composable(
                    route = "editar_producto/{puestoId}/{productoId}",
                    arguments = listOf(
                        navArgument("puestoId") { type = NavType.StringType },
                        navArgument("productoId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val puestoId = backStackEntry.arguments?.getString("puestoId") ?: return@composable
                    val productoId = backStackEntry.arguments?.getString("productoId") ?: return@composable

                    PantallaEditarProducto(
                        puestoId = puestoId,
                        productoId = productoId,
                        onProductoActualizado = { backStackEntry.savedStateHandle["refresh"] = true },
                        onCancelar = { backStackEntry.savedStateHandle["refresh"] = true }
                    )
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
                                    mostrarLogin = false
                                    navController.navigate("gestionar_solicitudes")
                                }) {
                                    Text("Gestionar solicitudes (Admin)")
                                }

                                    Button(onClick = {
                                        mostrarLogin = false
                                        navController.navigate("agregar_feria")
                                    }) {
                                        Text("Agregar nueva feria")
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        mostrarLogin = false
                                        navController.navigate("administrarFerias") },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Administrar Ferias")
                                }
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
fun BottomNavigationBar(navController: NavHostController, tipoUsuario: String?) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val mapaItem = UsuarioScreen.Mapa
    val puestosLabel = if (tipoUsuario == "feriante") "Tus Puestos" else "Puestos"
    val puestosItem = UsuarioScreen.Custom("puestos", puestosLabel, Icons.Filled.ShoppingCart)

    val items = listOf(mapaItem, puestosItem)

    NavigationBar {
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = currentRoute == screen.route,
                onClick = {
                    when (screen.route) {
                        "puestos" -> {
                            if (tipoUsuario == "feriante") {
                                navController.navigate("mis_puestos")
                            } else {
                                navController.navigate("puestos")
                            }
                        }
                        "mapa" -> {
                            navController.navigate("mapa")
                        }
                        else -> {
                            navController.navigate("mapa")
                        }
                    }
                }
            )
        }
    }
}



@Composable
fun PantallaMapa() {
    val mostrar = remember { mutableStateOf(false) }

    SolicitarPermisosUbicacion {
        mostrar.value = true
    }

    if (mostrar.value) {
        MostrarMapaConUbicacion()
    }
}

@Composable
fun MostrarMapaConUbicacion() {
    val db = Firebase.firestore
    val santiago = LatLng(-33.4489, -70.6693)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(santiago, 12f)
    }

    var ferias by remember { mutableStateOf(listOf<FeriaCompleta>()) }

    // Obtener ferias desde Firebase
    LaunchedEffect(true) {
        db.collection("ferias").get().addOnSuccessListener { result ->
            ferias = result.map { doc ->
                FeriaCompleta(
                    nombre = doc.getString("nombre") ?: "",
                    comuna = doc.getString("comuna") ?: "",
                    direccion = doc.getString("direccion") ?: "",
                    latitud = doc.getDouble("latitud") ?: 0.0,
                    longitud = doc.getDouble("longitud") ?: 0.0
                )
            }
        }
    }

    // Mapa con markers de ferias
    GoogleMap(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        cameraPositionState = cameraPositionState
    ) {
        ferias.forEach { feria ->
            Marker(
                state = MarkerState(position = LatLng(feria.latitud, feria.longitud)),
                title = feria.nombre,
                snippet = "${feria.comuna} - ${feria.direccion}"
            )
        }
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