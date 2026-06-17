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
import com.example.myinventarioapp.ui.viewmodel.VentaViewModel


@Composable
fun AppNavGraph(navController: NavHostController) {
    // 🔹 ventaViewModel se sigue creando aquí porque DetailVenta, ProductsVenta y
    // SearchProductScreen viven en este NavHost EXTERNO (a pantalla completa) y
    // necesitan compartir el mismo ViewModel que VentaScreen (que ahora vive dentro
    // de MainScaffold). Pasarlo desde aquí mantiene una sola instancia compartida.
    val ventaViewModel: VentaViewModel = viewModel()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background // Usa el color del tema activo
    ) {
        NavHost(navController = navController, startDestination = "login") {
            composable("login") {
                LoginScreen(
                    onNavigateToRegister = { navController.navigate("register") },
                    onLoginSuccess = { userName, rol, email ->
                        // 🔹 Pasamos los 3 valores como argumentos de ruta hacia "main",
                        // así MainScaffold los puede leer y entregárselos a HomeScreen.
                        navController.navigate("main/$userName/$rol/$email") {
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

            composable("local") {
                LocalScreen()
            }

            // 🔹 NUEVA RUTA: contiene el Scaffold con bottom bar (Home, Inventario,
            // Ventas, Reportes, Config) y su propio NavHost interno.
            // Recibe userName/rol/email del login como argumentos de ruta.
            composable(
                route = "main/{userName}/{rol}/{email}",
                arguments = listOf(
                    androidx.navigation.navArgument("userName") { type = androidx.navigation.NavType.StringType },
                    androidx.navigation.navArgument("rol") { type = androidx.navigation.NavType.StringType },
                    androidx.navigation.navArgument("email") { type = androidx.navigation.NavType.StringType }
                )
            ) { backStackEntry ->
                val userName = backStackEntry.arguments?.getString("userName") ?: ""
                val rol = backStackEntry.arguments?.getString("rol") ?: ""
                val email = backStackEntry.arguments?.getString("email") ?: ""
                MainScaffold(
                    rootNavController = navController,
                    userName = userName,
                    userRole = rol,
                    userEmail = email
                )
            }

            // 🔹 Las pantallas de abajo se mantienen en este NavHost externo porque son
            // flujos "a pantalla completa" (sin bottom bar): escaneo QR, detalle de una
            // venta puntual, búsqueda de productos para agregar a una venta, etc.
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
                        // 🔹 Volvemos al NavHost interno simplemente retrocediendo,
                        // ya que "main/{userName}/{rol}/{email}" sigue en el back stack
                        // con su estado guardado (Ventas seguirá seleccionado).
                        navController.popBackStack()
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
            ) { backStackEntry ->
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
        }
    }
}