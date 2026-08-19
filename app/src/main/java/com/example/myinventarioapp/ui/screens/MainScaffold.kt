package com.example.myinventarioapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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

    val navBackStackEntry by innerNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val hideBottomBarRoutes = listOf(
        "detailventa",
        "scannerSearch",
        "SearchProducts",
        "productsventa",
        "scanner"
    )
    val hideBottomBar = hideBottomBarRoutes.any { route ->
        currentRoute?.startsWith(route) == true
    }
    Scaffold(
        contentWindowInsets = WindowInsets(0), // 👈 le dice al Scaffold que no aplique insets
        bottomBar = {
            if (!hideBottomBar) {
                BottomNavBar(innerNavController)
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize()
//                .background(BrandBlack)
        ) {
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
                        navArgument("codigoEscaneado") {
                            type = NavType.StringType
                            defaultValue = ""
                            nullable = true
                        }
                    )
                ) { backStackEntry ->
                    val codigoEscaneado =
                        backStackEntry.arguments?.getString("codigoEscaneado") ?: ""
                    // 🔹 OJO: InventarioScreen usa internamente navController.navigate("scanner")
                    // Como "scanner" no existe en este NavHost interno, le pasamos el rootNavController
                    // para que esa navegación a pantalla completa funcione correctamente.
                    InventarioScreen(
                        navController = innerNavController,
                        codigoEscaneado = codigoEscaneado
                    )
                }
                composable("scannerInventary") {
                    ScannerScreen(
                        onCodeScanned = { scannedCode ->
                            innerNavController.navigate("inventario?codigoEscaneado=$scannedCode") {
                                launchSingleTop = true
                                popUpTo("inventario?codigoEscaneado={codigoEscaneado}") { inclusive = true }
                            }
                        }
                    )
                }
//                composable("scanner") {
//                    ScannerScreen(
//                        onCodeScanned = { scannedCode ->
//                            innerNavController.navigate("inventario?codigoEscaneado=$scannedCode") {
//                                launchSingleTop = true
//                                popUpTo("inventario?codigoEscaneado={codigoEscaneado}") { inclusive = true }
//                            }
//                        }
//                    )
//                }

                composable("ventas") {
                    VentaScreen(
                        onNavigateToDetailVenta = { ventaId ->
                            innerNavController.navigate("detailventa/$ventaId")
                        },
                        ventaViewModel = ventaViewModel
                    )
                }

                composable(
                    "detailventa/{ventaid}",
                    listOf(
                        navArgument("ventaid") {
                            type = NavType.StringType
                            defaultValue = "New"
                        }
                    )
                ){
                    backStackEntry ->
                    val ventaId = backStackEntry.arguments?.getString("ventaid") ?: "New"
                    DetailVenta(
                        onVentaScreen = {
//                            innerNavController.popBackStack()
                            innerNavController.navigate("ventas"){
                                popUpTo("ventas"){inclusive = false}
                                launchSingleTop = true
                            }
                        },
                        onSearch = {innerNavController.navigate("SearchProducts")},
                        ventaViewModel = ventaViewModel,
                        ventaId = ventaId,
                        navController = innerNavController,
                    )
                }
                composable("scanner") {
                    ScannerScreen(
                        onCodeScanned = { scannedCode ->
                            innerNavController.navigate("inventario?codigoEscaneado=$scannedCode") {
                                launchSingleTop = true
                                popUpTo("inventario?codigoEscaneado={codigoEscaneado}") { inclusive = true }
                            }
                        }
                    )
                }

                composable(
                    "SearchProducts?codigoEscaneado={codigoEscaneado}",
                    listOf(
                        navArgument("codigoEscaneado"){
                            type = NavType.StringType
                            defaultValue = ""
                            nullable = true
                        }
                    )
                ){ backStackEntry ->
                    val codigoEscaneado = backStackEntry.arguments?.getString("codigoEscaneado") ?: ""
                    SearchProductScreen(
                        navController = innerNavController,
                        onToProductsVenta = {innerNavController.navigate("productsventa")},
                        codigoEscaneado = codigoEscaneado,
                        ventaViewModel = ventaViewModel
                    )
                }
                composable("scannerSearch") {
                    ScannerScreen(
                        onCodeScanned = { scannedCode ->
                            innerNavController.navigate("SearchProducts?codigoEscaneado=$scannedCode") {
                                launchSingleTop = true
                                popUpTo("SearchProducts?codigoEscaneado={codigoEscaneado}") { inclusive = true }
                            }
                        }
                    )
                }
                composable("productsventa"){
                    ProductsVenta(
                        onToDetailVenta = { ventaId ->
                            innerNavController.navigate("detailventa/$ventaId")
                        },
                        onSearch = {innerNavController.navigate("SearchProducts")},
                        ventaViewModel = ventaViewModel
                    )
                }


                composable("reporte") {
                    ReportScreen(
                        navController = rootNavController
                    )
                }

                composable(
                    "TransferProduct?codigoEscaneado={codigoEscaneado}",
                    listOf(
                        navArgument("codigoEscaneado"){
                            type = NavType.StringType
                            defaultValue =""
                            nullable = true
                        }
                    )

                ){  backStackEntry ->
                    val codigoEscaneado = backStackEntry.arguments?.getString("codigoEscaneado") ?: ""
                    TransactionsScreen(
                        navController = innerNavController,
                        codigoEscaneado = codigoEscaneado,
                    )
                }
                composable("scannerTrans") {
                    ScannerScreen(
                        onCodeScanned = { scannedCode ->
                            innerNavController.navigate("TransferProduct?codigoEscaneado=$scannedCode") {
                                launchSingleTop = true
                                popUpTo("TransferProduct?codigoEscaneado={codigoEscaneado}") { inclusive = true }
                            }
                        }
                    )
                }
//                composable(
//                    "SearchProducts?codigoEscaneado={codigoEscaneado}",
//                    listOf(
//                        navArgument("codigoEscaneado"){
//                            type = NavType.StringType
//                            defaultValue = ""
//                            nullable = true
//                        }
//                    )
//                ){ backStackEntry ->
//                    val codigoEscaneado = backStackEntry.arguments?.getString("codigoEscaneado") ?: ""
//                    SearchProductScreen(
//                        navController = innerNavController,
//                        onToProductsVenta = {innerNavController.navigate("productsventa")},
//                        codigoEscaneado = codigoEscaneado,
//                        ventaViewModel = ventaViewModel
//                    )
//                }
//                composable("scannerSearch") {
//                    ScannerScreen(
//                        onCodeScanned = { scannedCode ->
//                            innerNavController.navigate("SearchProducts?codigoEscaneado=$scannedCode") {
//                                launchSingleTop = true
//                                popUpTo("SearchProducts?codigoEscaneado={codigoEscaneado}") { inclusive = true }
//                            }
//                        }
//                    )
//                }
                composable("setting") {
                    SettingScreen(
                        onNavigateToLocal = { rootNavController.navigate("local") }
                    )
                }
            }
        }
    }
}