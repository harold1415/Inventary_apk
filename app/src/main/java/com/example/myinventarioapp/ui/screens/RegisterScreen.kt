package com.example.myinventarioapp.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.Image
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.myinventarioapp.R // Asegúrate de que este import sea correcto para tu proyecto

// TODO: Reemplaza con el ID de tu logo en drawable
val APP_LOGO_RES_ID1 = R.drawable.ic_menu_logo // Placeholder, ¡cámbialo!

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("vendedor") } // Rol por defecto
    var showRoleDropdown by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var confirmPasswordError by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Colores de ejemplo (ajusta a tu marca)
    val brandPrimaryColor = Color(0xFF1A1A1A) // Negro
    val brandSecondaryColor = Color(0xFFF5EFE6) // Hueso/Blanco
    val brandAccentColor = Color(0xFFB8864B) // Dorado elegante

    val validateFields: () -> Boolean = {
        nameError = name.isBlank()
        emailError = email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        passwordError = password.isBlank() || password.length < 6
        confirmPasswordError = confirmPassword.isBlank() || confirmPassword != password

        !nameError && !emailError && !passwordError && !confirmPasswordError
    }

    val performRegister: () -> Unit = {
        if (validateFields()) {
            isLoading = true
            keyboardController?.hide()
            focusManager.clearFocus()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                        val user = hashMapOf(
                            "nombre" to name,
                            "email" to email,
                            "rol" to selectedRole
                        )
                        db.collection("usuarios").document(uid).set(user)
                            .addOnSuccessListener { documentReference ->
                                isLoading = false
                                Toast.makeText(context, "Registro exitoso!", Toast.LENGTH_SHORT).show()
                                onRegisterSuccess()
                            }
                            .addOnFailureListener { e ->
                                isLoading = false
                                Toast.makeText(context, "Error al guardar datos: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        isLoading = false
                        Toast.makeText(context, "Error de registro: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brandPrimaryColor) // Fondo principal negro
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus() // Cierra el foco
                    keyboardController?.hide() // Esconde el teclado
                })
            }
    ) {
        // Contenido superior (Logo y Título)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.35f) // Ocupa el 35% superior
                .align(Alignment.TopCenter)
                .padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Image(
                painter = painterResource(id = APP_LOGO_RES_ID1),
                contentDescription = "App Logo",
                modifier = Modifier.size(100.dp) // Tamaño fijo para el logo
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Crear Cuenta",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }

        // Tarjeta de registro (Hueso/Blanco) - Ocupa el 65% inferior
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f) // Ocupa el 65% inferior
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = brandSecondaryColor // Color hueso/blanco
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Ingresa tus Datos",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Campo Nombre
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre Completo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    isError = nameError,
                    supportingText = { if (nameError) Text("El nombre no puede estar vacío") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = brandPrimaryColor,
                        unfocusedBorderColor = Color(0xFFC8A882),
                        errorBorderColor = MaterialTheme.colorScheme.error,
                        focusedContainerColor = Color(0xFFFDFAF7),
                        unfocusedContainerColor = Color(0xFFFDFAF7)
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Campo Correo Electrónico
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo electrónico") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    isError = emailError,
                    supportingText = { if (emailError) Text("Introduce un correo electrónico válido") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = brandPrimaryColor,
                        unfocusedBorderColor = Color(0xFFC8A882),
                        errorBorderColor = MaterialTheme.colorScheme.error,
                        focusedContainerColor = Color(0xFFFDFAF7),
                        unfocusedContainerColor = Color(0xFFFDFAF7)
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Campo Contraseña
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; passwordError = false },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    trailingIcon = {
                        val image = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        val description = if (showPassword) "Ocultar contraseña" else "Mostrar contraseña"
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(imageVector = image, contentDescription = description)
                        }
                    },
                    isError = passwordError,
                    supportingText = { if (passwordError) Text("La contraseña debe tener al menos 6 caracteres") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = brandPrimaryColor,
                        unfocusedBorderColor = Color(0xFFC8A882),
                        errorBorderColor = MaterialTheme.colorScheme.error,
                        focusedContainerColor = Color(0xFFFDFAF7),
                        unfocusedContainerColor = Color(0xFFFDFAF7)
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Campo Confirmar Contraseña
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; confirmPasswordError = false },
                    label = { Text("Confirmar Contraseña") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { performRegister() }),
                    trailingIcon = {
                        val image = if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        val description = if (showConfirmPassword) "Ocultar contraseña" else "Mostrar contraseña"
                        IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                            Icon(imageVector = image, contentDescription = description)
                        }
                    },
                    isError = confirmPasswordError,
                    supportingText = { if (confirmPasswordError) Text("Las contraseñas no coinciden") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = brandPrimaryColor,
                        unfocusedBorderColor = Color(0xFFC8A882),
                        errorBorderColor = MaterialTheme.colorScheme.error,
                        focusedContainerColor = Color(0xFFFDFAF7),
                        unfocusedContainerColor = Color(0xFFFDFAF7)
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Selector de Rol
                ExposedDropdownMenuBox(
                    expanded = showRoleDropdown,
                    onExpandedChange = { showRoleDropdown = !showRoleDropdown },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedRole,
                        onValueChange = { /* No editable directamente */ },
                        readOnly = true,
                        label = { Text("Rol") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showRoleDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = brandPrimaryColor,
                            unfocusedBorderColor = Color(0xFFC8A882),
                            errorBorderColor = MaterialTheme.colorScheme.error,
                            focusedContainerColor = Color(0xFFFDFAF7),
                            unfocusedContainerColor = Color(0xFFFDFAF7)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = showRoleDropdown,
                        onDismissRequest = { showRoleDropdown = false }
                    ) {
                        DropdownMenuItem(text = { Text("admin") }, onClick = { selectedRole = "admin"; showRoleDropdown = false })
                        DropdownMenuItem(text = { Text("vendedor") }, onClick = { selectedRole = "vendedor"; showRoleDropdown = false })
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                // Botón de Registro
                Button(
                    onClick = performRegister,
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = brandPrimaryColor) // Botón negro
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Registrarse", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

//                // Botón para volver al Login
//                TextButton(onClick = onNavigateToLogin) {
//                    Text(
//                        "¿Ya tienes cuenta? Inicia Sesión",
//                        color = brandAccentColor, // Dorado elegante
//                        style = MaterialTheme.typography.bodyLarge
//                    )
//                }
            }
        }
    }
}
