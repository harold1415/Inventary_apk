package com.example.myinventarioapp.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.* // Importar remember y mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import com.example.myinventarioapp.R // Asegúrate de que este import sea correcto para tu proyecto

// TODO: Reemplaza con el ID de tu logo en drawable
// Por ejemplo: R.drawable.tu_logo_aqui
// Asegúrate de tener un archivo PNG o Vector Asset en la carpeta res/drawable
val APP_LOGO_RES_ID = R.drawable.ic_menu_logo // Placeholder, ¡cámbialo!

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Estados para la animación
    val splashVisible = remember { MutableTransitionState(true) }
    val loginCardVisible = remember { MutableTransitionState(false) }

    // Colores de ejemplo (ajusta a tu marca)
    val brandPrimaryColor = Color(0xFF00A859) // Verde Interbank
    val brandSecondaryColor = Color(0xFF00468C) // Azul Interbank

    LaunchedEffect(Unit) {
        // Recuperar datos guardados al iniciar
        val sharedPref = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        email = sharedPref.getString("email", "") ?: ""
        rememberMe = email.isNotEmpty()

        // Iniciar animación después de un breve retraso
        delay(1000) // Espera 1 segundo en el splash inicial
        splashVisible.targetState = false
        loginCardVisible.targetState = true
    }

    val validateFields: () -> Boolean = {
        emailError = email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        passwordError = password.isBlank() || password.length < 6 // Ejemplo de validación de longitud
        !emailError && !passwordError
    }

    val performLogin: () -> Unit = {
        if (validateFields()) {
            isLoading = true
            keyboardController?.hide()
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                        db.collection("usuarios").document(uid).get()
                            .addOnSuccessListener { document ->
                                isLoading = false
                                val rol = document.getString("rol")
                                val sharedPref = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                                sharedPref.edit {
                                    if (rememberMe) {
                                        putString("email", email)
                                    } else {
                                        clear()
                                    }
                                    apply()
                                }
                                if (rol == "admin" || rol == "vendedor") {
                                    Toast.makeText(
                                        context,
                                        "Bienvenido $rol",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    onLoginSuccess()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Rol no reconocido",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            .addOnFailureListener {
                                isLoading = false
                                Toast.makeText(
                                    context,
                                    "Error al leer datos: ${it.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        isLoading = false
                        Toast.makeText(
                            context,
                            "Error: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo de marca (verde) - Ocupa toda la pantalla inicialmente
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(brandPrimaryColor),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top // Alineado arriba para el logo y texto
        ) {
            // Logo y texto de bienvenida (visibles solo durante el splash o arriba)
            AnimatedVisibility(
                visibleState = splashVisible,
                enter = fadeIn(animationSpec = tween(durationMillis = 500)),
                exit = fadeOut(animationSpec = tween(durationMillis = 500))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 120.dp)) {
                    Image(
                        painter = painterResource(id = APP_LOGO_RES_ID),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(96.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "¡Bienvenido a tu App!",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        "Inicia sesión para gestionar tu inventario",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            // Contenido superior después del splash (ej. "Hola, Harold")
            AnimatedVisibility(
                visible = !splashVisible.currentState && loginCardVisible.currentState,
                enter = fadeIn(animationSpec = tween(durationMillis = 500, delayMillis = 700)),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 64.dp) // Ajusta este padding para posicionar el texto
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.Start // Alineado a la izquierda como en la imagen
                ) {
                    Text(
                        "Hola, Harold", // Puedes reemplazar "Harold" con el nombre del usuario si lo tienes
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    // Puedes añadir más elementos aquí si es necesario, como el botón de menú o el candado
                }
            }
        }

        // Tarjeta de login que sube y se posiciona
        AnimatedVisibility(
            visibleState = loginCardVisible,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(durationMillis = 700, delayMillis = 300)) +
                    fadeIn(animationSpec = tween(durationMillis = 2000, delayMillis = 300)),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.65f), // Ajustado para que ocupe el 65% inferior
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp), // Esquinas redondeadas
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Ingresa tu contraseña", // Texto como en la imagen de Interbank
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo electrónico") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        isError = emailError,
                        supportingText = {
                            if (emailError) {
                                Text("Introduce un correo electrónico válido")
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = brandPrimaryColor,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            errorBorderColor = MaterialTheme.colorScheme.error,
                            focusedContainerColor = Color.Transparent, // Fondo transparente
                            unfocusedContainerColor = Color.Transparent // Fondo transparente
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // Campo de Contraseña (simplificado como en la imagen)
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = false
                        },
                        label = { Text("Contraseña") }, // El label se mantiene para accesibilidad
                        placeholder = { Text("Ingresa tu contraseña") }, // Placeholder visible
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { performLogin() }),
                        trailingIcon = {
                            val image = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            val description = if (showPassword) "Ocultar contraseña" else "Mostrar contraseña"
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(imageVector = image, contentDescription = description)
                            }
                        },
                        isError = passwordError,
                        supportingText = {
                            if (passwordError) {
                                Text("La contraseña debe tener al menos 6 caracteres")
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = brandPrimaryColor,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            errorBorderColor = MaterialTheme.colorScheme.error,
                            focusedContainerColor = Color.Transparent, // Fondo transparente
                            unfocusedContainerColor = Color.Transparent // Fondo transparente
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = performLogin,
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(50), // Botón más redondeado
                        colors = ButtonDefaults.buttonColors(containerColor = brandSecondaryColor) // Color azul
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Ingresar", style = MaterialTheme.typography.titleMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón para activar huella digital
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(50), // Forma redondeada
                        color = MaterialTheme.colorScheme.surfaceVariant, // Un color de fondo sutil
                        onClick = { /* TODO: Implementar activación de huella */ }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Fingerprint, contentDescription = "Huella Digital", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Activar Huella Digital", style = MaterialTheme.typography.bodyLarge)
                            }
                            Switch(
                                checked = rememberMe, // Usamos rememberMe como placeholder para el estado del switch
                                onCheckedChange = { rememberMe = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = brandPrimaryColor)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    TextButton(onClick = { /* TODO: Implementar navegación a pantalla de recuperación */ }) {
                        Text("Olvidé mi contraseña", color = brandPrimaryColor, style = MaterialTheme.typography.bodyLarge)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(onClick = onNavigateToRegister) {
                        Text(
                            "¿No tienes cuenta? Regístrate",
                            color = brandPrimaryColor,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "v23.0", // Versión de la app
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}