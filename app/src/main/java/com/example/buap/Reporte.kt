package com.example.buap

data class Reporte(
    val id: Int = 0,
    val nombre: String,
    val fecha: String,
    val hora: String,
    val direccion: String,
    val riesgo: String,
    val descripcion: String,
    val foto: String? = null
)
