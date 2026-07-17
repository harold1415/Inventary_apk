package com.example.myinventarioapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.KeyboardReturn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myinventarioapp.ui.theme.BrandBlack
import com.example.myinventarioapp.ui.theme.BrandWarmBackground
import com.example.myinventarioapp.ui.theme.BrandWarmWhite
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(){
    Scaffold(
        topBar = {TopAppBar(
            title = { Text("Movimientos", color = BrandWarmWhite) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandBlack),
        )},
        containerColor = BrandWarmBackground,
    ){ padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
        ){
            Spacer(modifier = Modifier.height(16.dp))

            TransactionCard(
                title = "Transferencia",
                description = "Mover productos entre locales",
                icon = Icons.Default.CompareArrows
            ) {
                // navegar
            }

            TransactionCard(
                title = "Devolución",
                description = "Registrar productos devueltos",
                icon = Icons.Default.KeyboardReturn
            ) {

            }

            TransactionCard(
                title = "Cambio",
                description = "Cambiar un producto por otro",
                icon = Icons.Default.SwapHoriz
            ) {

            }
        }
    }
}


// DISEÑO DEL CARD
@Composable
fun TransactionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = BrandWarmWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BrandBlack,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {

                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlack
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    color = Color.Gray
                )
            }
        }
    }
}