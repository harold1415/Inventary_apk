package com.example.myinventarioapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

@Composable
fun HomeScreen(
    onNavigateToInventario: () -> Unit,
    onNavigateToVentas: () -> Unit,
    onNavigateToSetting:() -> Unit,
    onNavigateToReport:() ->Unit,
    onLogout: () -> Unit) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid
    val db = FirebaseFirestore.getInstance()

    var userEmail by remember { mutableStateOf("") }
    var userRole by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }


    LaunchedEffect(uid) {
        uid?.let {
            db.collection("usuarios").document(it).get()
                .addOnSuccessListener { document ->
                    userEmail = document.getString("email") ?: ""
                    userName = document.getString("nombre") ?: ""
                    userRole = document.getString("rol") ?: ""
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error al obtener datos del usuario", Toast.LENGTH_SHORT).show()
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Bienvenido", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(userName, fontSize = 18.sp)
        Text("Rol: $userRole", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onNavigateToSetting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("⚙\uFE0F Configuracion")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onNavigateToInventario,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📦 Inventario")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNavigateToVentas,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🛒 Ventas")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onNavigateToReport,
            modifier = Modifier.fillMaxWidth()

        ){
            Text("\uD83E\uDDFE Reportes")
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Cerrar sesión", color = MaterialTheme.colorScheme.onError)
        }
    }
}
