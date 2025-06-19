package com.example.aplicacionferialibre.screens


import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat


@Composable
fun SolicitarPermisosUbicacion(onPermisoConcedido: () -> Unit) {
    val context = LocalContext.current
    var permisosConcedidos by rememberSaveable { mutableStateOf(false) }

    val permisos = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { resultados ->
        permisosConcedidos = resultados[Manifest.permission.ACCESS_FINE_LOCATION] == true
                || resultados[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(true) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            permisosConcedidos = true
        } else {
            launcher.launch(permisos)
        }
    }

    if (permisosConcedidos) {
        onPermisoConcedido()
    }
}
