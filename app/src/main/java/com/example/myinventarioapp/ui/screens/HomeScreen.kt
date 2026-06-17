package com.example.myinventarioapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myinventarioapp.ui.theme.BrandBlack
import com.example.myinventarioapp.ui.theme.BrandWarmBackground
import com.example.myinventarioapp.ui.theme.BrandWarmWhite

// ⚠️ VERSIÓN TEMPORAL — solo para probar que el bottom nav funciona.
// Cuando confirmes que la navegación anda bien, reemplazamos esto por el
// dashboard real con selector de sucursal + ventas + ganancias.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String,
    userRole: String,
    onLogout: () -> Unit
) {
    Scaffold(
        containerColor = BrandWarmBackground,
        topBar = {
            TopAppBar(
                title = { Text("Dashboard", color = BrandWarmWhite) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandBlack),
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Salir", color = BrandWarmWhite)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "✅ Navegación funcionando",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(8.dp))
            Text("Prueba tocar Inventario, Ventas, Reportes y Config abajo.")
        }
    }
}