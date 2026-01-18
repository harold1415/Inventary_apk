package com.example.myinventarioapp.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign

data class Local(
    val id: String = "", // ← ID del documento en Firestore
    val nombre: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalScreen(){
    var showDialogCreate by remember { mutableStateOf(false)}
    var locales by remember { mutableStateOf(listOf<Local>()) }
    val db =FirebaseFirestore.getInstance()
    var localSeleccionado by remember { mutableStateOf<Local?>(null)}
    var showDialogEdit by remember { mutableStateOf(false)}
    var showDialogDelete by remember { mutableStateOf(false)}
    val context = LocalContext.current

    // Leer ventas desde Firestore
    LaunchedEffect(Unit) {
        db.collection("locales").addSnapshotListener { snapshot, _ ->
            snapshot?.let {
                locales = it.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Local::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        // 👇 Si un documento no coincide, lo ignoramos
                        Log.e("VentaScreen", "Error parseando venta: ${e.message}")
                        null
                    }
                }
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Sucursales") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {showDialogCreate = true}
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Local")
            }
        }
    ){ padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(locales){local->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ){
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ){
                        Column(modifier = Modifier.weight(1f)){
                            Text(
                                "Local : ${local.nombre}",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Column(
                            modifier = Modifier.wrapContentWidth(),
                            verticalArrangement = Arrangement.spacedBy(0.dp) // separación entre botones
                        ){
                            IconButton(
                                onClick = {
                                    showDialogEdit = true
                                    localSeleccionado = local
                                },
                                modifier = Modifier.size(36.dp) // ⬅️ tamaño del botón
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Editar",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp) // ⬅️ tamaño del ícono
                                )
                            }
                            IconButton(
                                onClick = {
                                    showDialogDelete = true
                                    localSeleccionado = local
                                },
                                modifier = Modifier.size(36.dp) // ⬅️ tamaño del botón
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Eliminar",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp) // ⬅️ tamaño del ícono
                                )
                            }
                        }
                    }
                }
            }

        }
    }
    //EDITAR LOCAL
    if(showDialogEdit && localSeleccionado!=null){
        var editName by remember { mutableStateOf(localSeleccionado!!.nombre) }
        AlertDialog(
            onDismissRequest = {showDialogEdit=false},
            confirmButton = {
                Button(onClick = {
                    val actualizacion = mapOf(
                        "nombre" to editName,
                    )
                    db.collection("locales").document(localSeleccionado!!.id)
                        .update(actualizacion)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Local actualizado", Toast.LENGTH_SHORT)
                                .show()
                            showDialogEdit = false
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                context,
                                "Error al actualizar: ${it.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }) { Text("Guardar")}
            },
            dismissButton = {
                TextButton(onClick = {showDialogEdit=false}) { Text("Cancelar") }
            },
            title ={ Text("Editar Local")},
            text = {
                Column{
                    OutlinedTextField(
                        value = editName,
                        onValueChange ={editName = it} ,
                        label ={ Text("Nombre")}
                    )
                }
            }
        )
    }

    // ELIMINAR LOCAL
    if(showDialogDelete && localSeleccionado!=null){

        AlertDialog(
            onDismissRequest = {showDialogDelete= false},
            confirmButton = {},
            title = { Text("ELIMINAR LOCAL",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center)},
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("¿Desea eliminar este local : $localSeleccionado.nombre?")
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(onClick = {
                            localSeleccionado?.let { local ->
                                db.collection("locales")
                                    .document(local.id).delete()
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            context,
                                            "Local eliminado",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        showDialogDelete = false
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Error al eliminar: ${it.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }) {
                            Text("Eliminar")
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        TextButton(onClick = {
                            showDialogDelete = false
                        }) {
                            Text("Cerrar")
                        }
                    }
                }
            }
        )
    }

    //CREAR LOCAL
    if(showDialogCreate){
        var nombre by remember { mutableStateOf("")}
        AlertDialog(
            onDismissRequest = {showDialogEdit=false},
            confirmButton = {
                Button(onClick = {
                    val nuevoLocal = mapOf(
                        "nombre" to nombre,
                    )
                    db.collection("locales").add(nuevoLocal)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Local creado", Toast.LENGTH_SHORT)
                                .show()
                            nombre=""
                            showDialogCreate = false
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                context,
                                "Error al crear: ${it.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }) { Text("Guardar")}
            },
            dismissButton = {
                TextButton(onClick = {showDialogCreate=false}) { Text("Cancelar") }
            },
            title ={ Text("Crear Local")},
            text = {
                Column{
                    OutlinedTextField(
                        value = nombre,
                        onValueChange ={nombre=it} ,
                        label ={ Text("Nombre")}
                    )
                }
            }
        )
    }
}