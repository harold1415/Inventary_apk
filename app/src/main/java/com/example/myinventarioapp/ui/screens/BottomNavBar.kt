package com.example.myinventarioapp.ui.screens

import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.myinventarioapp.ui.theme.BrandBlack
import com.example.myinventarioapp.ui.theme.BrandTextSecondary
import com.example.myinventarioapp.ui.theme.BrandWarmWhite
import androidx.compose.runtime.getValue

// 🔹 Representa cada pestaña de la barra inferior (ruta, ícono y etiqueta)
data class BottomTab(
    val route: String,
    val label: String,
    val icon: ImageVector
)

// 🔹 Lista fija de pestañas — agregar/quitar una pestaña solo requiere editar esta lista
val bottomTabs = listOf(
    BottomTab("home", "Inicio", Icons.Default.Home),
    BottomTab("inventario", "Inventario", Icons.Default.Inventory),
    BottomTab("ventas", "Ventas", Icons.Default.ShoppingCart),
    BottomTab("reporte", "Reportes", Icons.Default.Receipt),
    BottomTab("setting", "Config", Icons.Default.Settings)
)

@Composable
fun BottomNavBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        containerColor = BrandWarmWhite,
        windowInsets = NavigationBarDefaults.windowInsets
//        modifier = androidx.compose.ui.Modifier.height(64.dp)
    ) {
        bottomTabs.forEach { tab ->
            // 🔹 Compara contra la jerarquía completa (no solo la ruta exacta),
            // así "inventario?codigoEscaneado=..." también marca el tab de Inventario como activo
            val selected = currentDestination?.hierarchy?.any {
                it.route?.startsWith(tab.route) == true
            } == true

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(tab.route) {
                            // 🔹 Evita acumular copias de la misma pantalla en el back stack
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BrandWarmWhite,
                    selectedTextColor = BrandBlack,
                    indicatorColor = BrandBlack, // 👈 "burbuja" detrás del ícono activo
                    unselectedIconColor = BrandTextSecondary,
                    unselectedTextColor = BrandTextSecondary
                )
            )
        }
    }
}