package com.example.myinventarioapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.myinventarioapp.ui.model.Local
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

//CONTRUCTOR PARA CREAR LA LISTA DE VARIANTES
data class VarianteProducto(
    val id: String,
    val nombre: String,
    val modeloCod: String,
    val talla: String,
    val color: String,
    val local: String,
    val stock: Int,
    val codigo: String
)
class TranferViewModel : ViewModel(){

    private val db = FirebaseFirestore.getInstance()

    private val _locales= MutableStateFlow<List<Local>>(emptyList())
    val locales = _locales.asStateFlow()
    init {
        db.collection("locales").addSnapshotListener { snapshot, _ ->
            val lista = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Local::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            _locales.value = lista
        }
    }


}