package com.example.myinventarioapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myinventarioapp.ui.theme.BrandBlack
import com.example.myinventarioapp.ui.theme.BrandTextSecondary
import com.example.myinventarioapp.ui.theme.BrandWarmBackground
import com.example.myinventarioapp.ui.theme.BrandWarmWhite
import com.example.myinventarioapp.ui.theme.BrandWoodLight
import com.example.myinventarioapp.ui.theme.BrandWoodMedium
import com.example.myinventarioapp.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Datos simulados para el top de tipo de prendas
private val topPrendasMock = listOf(
    "Casacas" to 0.1f,
    "Pantalones" to 0.72f,
    "Camisas" to 0.52f,
    "Polos" to 0.29f,
    "Chompas" to 0.18f
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String,
    userRole: String,
    userEmail: String = "",
    onLogout: () -> Unit,
    homeViewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val locales by homeViewModel.locales.collectAsState()
    val sucursalSeleccionada by homeViewModel.sucursalSeleccionada.collectAsState()
    val metrics by homeViewModel.metrics.collectAsState()
    val topSucursales by homeViewModel.topSucursales.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()
    val fechaSeleccionada by homeViewModel.fechaSeleccionada.collectAsState()

    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val fechaTexto = remember(fechaSeleccionada){
        val hoy = SimpleDateFormat("dd,MM,yyyy", Locale.getDefault()).format(Date())
        val elegida  = sdf.format(fechaSeleccionada)
        if(elegida == hoy){}
    }

    var dropdownExpanded by remember { mutableStateOf(false) }

    // 🔹 drawerState controla si el cajón está abierto o cerrado
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // 🔹 ModalNavigationDrawer envuelve TODO el contenido.
    // drawerContent = lo que se muestra dentro del cajón.
    // El 70% de ancho se logra con fillMaxWidth(0.70f).
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .fillMaxWidth(0.70f)  // 👈 ocupa el 70% del ancho
                    .fillMaxHeight(),
                drawerContainerColor = Color(0xFFF0E6D8),
                drawerContentColor =  Color(0xFF1A1A1A)
            ) {
                Spacer(Modifier.height(48.dp))

                // Avatar / inicial del usuario
                Box(
                    modifier = Modifier
                        .padding(start = 24.dp)
                        .size(72.dp)
                        .border(
                            width = 2.dp,
                            color = BrandWoodLight,
                            shape = CircleShape
                        )
                        .background(BrandWoodMedium, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlack
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Info del usuario
                DrawerInfoRow(
                    icon = Icons.Default.Person,
                    label = "Nombre",
                    value = userName
                )
                DrawerInfoRow(
                    icon = Icons.Default.Shield,
                    label = "Rol",
                    value = userRole
                )
                if (userEmail.isNotBlank()) {
                    DrawerInfoRow(
                        icon = Icons.Default.Email,
                        label = "Correo",
                        value = userEmail
                    )
                }

                Spacer(Modifier.weight(1f))

                // Botón cerrar sesión al fondo del drawer
                HorizontalDivider(
                    color = BrandWoodMedium.copy(alpha = 0.3f),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(Modifier.height(16.dp))
                TextButton(
                    onClick = {
                        scope.launch { drawerState.close() }
                        onLogout()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Cerrar sesión",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Cerrar sesión",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    ) {
        // 🔹 Contenido principal de la pantalla (el dashboard)
        Scaffold(
            containerColor = BrandWarmBackground,
            topBar = {
                TopAppBar(
                    title = { Text("Dashboard", color = BrandWarmWhite) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandBlack),
                    navigationIcon = {
                        // 🔹 Botón de menú (hamburguesa) que abre el drawer
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open()
                                else drawerState.close()
                            }
                        }) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Menú",
                                tint = BrandWarmWhite
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Hola, $userName",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = BrandBlack
                )
                Text(
                    "Rol: $userRole",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BrandTextSecondary
                )

                Spacer(Modifier.height(24.dp))

                // 🔽 Selector de sucursal
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = when (sucursalSeleccionada) {
                            null -> "Selecciona una sucursal"
                            "" -> "Todos los locales"
                            else -> sucursalSeleccionada!!
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Sucursal") },
                        shape = RoundedCornerShape(16.dp),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandBlack,
                            unfocusedBorderColor = BrandWoodMedium,
                            focusedContainerColor = BrandWarmWhite,
                            unfocusedContainerColor = BrandWarmWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
                    )

                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todos los locales") },
                            onClick = {
                                homeViewModel.seleccionarSucursal("")
                                dropdownExpanded = false
                            }
                        )
                        locales.forEach { local ->
                            DropdownMenuItem(
                                text = { Text(local.nombre) },
                                onClick = {
                                    homeViewModel.seleccionarSucursal(local.nombre)
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                if (sucursalSeleccionada == null) {
                    Spacer(Modifier.height(40.dp))
                    Text(
                        "Elige una sucursal para ver las ventas de hoy",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BrandTextSecondary
                    )
                } else if (isLoading) {
                    Spacer(Modifier.height(40.dp))
                    CircularProgressIndicator(color = BrandBlack)
                } else {
                    Text(
                        "Hoy",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = BrandBlack,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    MetricCard(
                        titulo = "Ventas totales",
                        valor = "S/ ${"%.2f".format(metrics.ventasTotales)}",
                        subtitulo = "${metrics.cantidadVentas} ventas registradas"
                    )
                    Spacer(Modifier.height(16.dp))
                    MetricCard(
                        titulo = "Ganancia total",
                        valor = "S/ ${"%.2f".format(metrics.gananciaTotal)}",
                        subtitulo = null
                    )
                }
            }
        }
    }
}

// 🔹 Fila de información dentro del drawer (ícono + label + valor)
@Composable
private fun DrawerInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = BrandWoodMedium,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = BrandTextSecondary.copy(alpha = 0.7f)
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = BrandBlack
            )
        }
    }
}

// 🔹 Card reutilizable para una métrica del dashboard
@Composable
private fun MetricCard(
    titulo: String,
    valor: String,
    subtitulo: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BrandWarmWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                titulo,
                style = MaterialTheme.typography.bodyMedium,
                color = BrandTextSecondary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                valor,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = BrandBlack
            )
            subtitulo?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = BrandWoodMedium
                )
            }
        }
    }
}