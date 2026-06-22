package com.example.myinventarioapp.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myinventarioapp.ui.viewmodel.VentaViewModel

// 🔹 Este Composable representa la app "una vez logueado".
// Tiene su PROPIO NavController interno, distinto al externo (rootNavController).
// Así, cambiar de pestaña (Home -> Ventas -> Home) no afecta el back stack de Login/Register.
@Composable
fun MainScaffold(
    rootNavController: NavHostController,
    userName: String,
    userRole: String,
    userEmail: String
) {
    val innerNavController = rememberNavController()
    val ventaViewModel: VentaViewModel = viewModel()

    Scaffold(
        bottomBar = { BottomNavBar(innerNavController) }
    ) { padding ->
        NavHost(
            navController = innerNavController,
            startDestination = "home",
            modifier = Modifier.padding(padding) // 👈 respeta el espacio que ocupa la bottom bar
        ) {
            composable("home") {
                HomeScreen(
                    userName = userName,
                    userRole = userRole,
                    onLogout = {
                        // 🔹 El logout navega en el NavController EXTERNO, ya que login
                        // vive fuera de este Scaffold con bottom bar
                        rootNavController.navigate("login") {
                            popUpTo(0) // 👈 limpia TODO el back stack (incluida la ruta "main/.../..." con argumentos)
                        }
                    }
                )
            }

            composable(
                route = "inventario?codigoEscaneado={codigoEscaneado}",
                arguments = listOf(
                    androidx.navigation.navArgument("codigoEscaneado") {
                        type = androidx.navigation.NavType.StringType
                        defaultValue = ""
                        nullable = true
                    }
                )
            ) { backStackEntry ->
                val codigoEscaneado = backStackEntry.arguments?.getString("codigoEscaneado") ?: ""
                // 🔹 OJO: InventarioScreen usa internamente navController.navigate("scanner")
                // Como "scanner" no existe en este NavHost interno, le pasamos el rootNavController
                // para que esa navegación a pantalla completa funcione correctamente.
                InventarioScreen(navController = rootNavController, codigoEscaneado = codigoEscaneado)
            }

            composable("ventas") {
                VentaScreen(
                    onNavigateToDetailVenta = { ventaId ->
                        rootNavController.navigate("detailventa/$ventaId")
                    },
                    ventaViewModel = ventaViewModel
                )
            }

            composable("reporte") {
                ReportScreen(
                    navController = rootNavController
                )
            }

            composable("setting") {
                SettingScreen(
                    onNavigateToLocal = { rootNavController.navigate("local") }
                )
            }
        }
    }
}