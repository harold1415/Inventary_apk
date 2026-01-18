package com.example.myinventarioapp.ui.model

import com.google.firebase.Timestamp

data class Local(
    val id: String = "",
    val nombre: String = ""
)
data class Producto(
    val id: String = "", // ← ID del documento en Firestore
    val codigo: String = "",
    val nombre: String = "",
    val tipo: String = "",
    val talla: String = "",
    val stock: Int = 0,
    val material: String = "",
    val marca: String = "",
    val color: String = "",
    val diseno: String = "",
    val precioxMayor: Double = 0.0,
    val modeloCod: String = "",
    val corte: String = "",
    val local: String = "",
    val manga: String = "",
    val costo: Double = 0.0,
    val precio: Double = 0.0,
    val fecha: Timestamp? = null,
    val imagenUrl: String = "" // 🔥 Nuevo campo para la imagen
)

