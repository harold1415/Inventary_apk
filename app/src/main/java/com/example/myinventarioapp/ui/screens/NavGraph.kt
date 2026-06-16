package com.example.myinventarioapp.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myinventarioapp.ui.viewmodel.UserViewModel
import com.example.myinventarioapp.ui.viewmodel.VentaViewModel
import com.example.myinventarioapp.ui.utils.SessionManager
import androidx.compose.ui.platform.LocalContext

@Composable
fun AppNavGraph(navController: NavHostController) {
    val ventaViewModel: VentaViewModel = viewModel()
    val userViewModel: UserViewModel = viewModel()
    val context = LocalContext.current
    Surface(
        modifier = Modifier.fillMaxSize(),
//        color = MaterialTheme.colorScheme.background // Usa el color del tema activo
    ) {
        NavHost(navController = navController, startDestination = "login") {
            composable("login") {
                LoginScreen(
                    onNavigateToRegister = { navController.navigate("register") },
                    onLoginSuccess = {userName, userRole, userEmail ->
                        userViewModel.setUserData(userName, userRole, userEmail)
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }

            composable("register") {
                RegisterScreen(onRegisterSuccess = {
                    navController.popBackStack()
                })
            }
            composable("setting"){
                SettingScreen(
                    onNavigateToLocal = {navController.navigate("local")}
                )
            }
            composable("local"){
                LocalScreen()
            }

            composable("home") {
                HomeScreen(
                    userName = userViewModel.userName,
                    userRole = userViewModel.userRole,
                    onNavigateToInventario = { navController.navigate("inventario") },
                    onNavigateToVentas = { navController.navigate("ventas") },
                    onNavigateToReport = { navController.navigate("reporte") },
                    onNavigateToSetting ={ navController.navigate(("setting"))},
                    onLogout = {
                        userViewModel.clearUserData() // Limpiar datos al cerrar sesión
                        SessionManager(context).clearSession() // <--- Limpiar también la memoria del teléfono
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
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
                InventarioScreen(navController = navController, codigoEscaneado = codigoEscaneado)
            }

            composable("scanner") {
                ScannerScreen(
                    onCodeScanned = { scannedCode ->
                        navController.navigate("inventario?codigoEscaneado=$scannedCode") {
                            launchSingleTop = true
                            popUpTo("inventario?codigoEscaneado={codigoEscaneado}") { inclusive = true }
                        }
                    }
                )
            }
            composable("ventas") {
                VentaScreen(
                    onNavigateToDetailVenta = { ventaId ->
                        navController.navigate("detailventa/$ventaId")
                    },
                    ventaViewModel = ventaViewModel
                )
            }
            composable(
                route = "detailventa/{ventaId}",
                arguments = listOf(
                    androidx.navigation.navArgument("ventaId") {
                        type = androidx.navigation.NavType.StringType
                        defaultValue = "New" // si no se pasa nada = nueva venta
                    }
                )
            ) { backStackEntry ->
                val ventaId = backStackEntry.arguments?.getString("ventaId") ?: "New"
                DetailVenta(
                    onVentaScreen = {
                        navController.navigate("ventas") {
                            popUpTo("ventas") { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onSearch = { navController.navigate("SearchProducts") },
                    ventaViewModel = ventaViewModel,
                    ventaId = ventaId,
                    navController = navController,
                    )
            }
            composable("productsventa") {
                ProductsVenta(
                    onToDetailVenta = { ventaId ->
                        navController.navigate("detailventa/$ventaId")
                    },
                    onSearch = { navController.navigate("SearchProducts") },
                    ventaViewModel = ventaViewModel
                )
            }
            composable(
                route = "SearchProducts?codigoEscaneado={codigoEscaneado}",
                arguments = listOf(
                    androidx.navigation.navArgument("codigoEscaneado") {
                        type = androidx.navigation.NavType.StringType
                        defaultValue = ""
                        nullable = true
                    }
                )
            )
            { backStackEntry ->
                val codigoEscaneado = backStackEntry.arguments?.getString("codigoEscaneado") ?: ""
                SearchProductScreen(
                    navController = navController,
                    onToProductsVenta = { navController.navigate("productsventa") },
                    codigoEscaneado = codigoEscaneado,
                    ventaViewModel = ventaViewModel
                )
            }
            composable("scannerSearch") {
                ScannerScreen(
                    onCodeScanned = { scannedCode ->
                        navController.navigate("SearchProducts?codigoEscaneado=$scannedCode") {
                            launchSingleTop = true
                            popUpTo("SearchProducts?codigoEscaneado={codigoEscaneado}") { inclusive = true }
                        }
                    }
                )
            }
            composable("reporte"){
                ReportScreen(
                    navController = navController,
                )
            }
        }
    }
}


