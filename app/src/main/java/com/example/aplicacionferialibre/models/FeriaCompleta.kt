package com.example.aplicacionferialibre.models

data class FeriaCompleta(
    val nombre: String,
    val comuna: String,
    val direccion: String,
    val latitud: Double,
    val longitud: Double
)