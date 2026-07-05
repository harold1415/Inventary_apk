package com.example.myinventarioapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.myinventarioapp.ui.screens.AppNavGraph
import com.example.myinventarioapp.ui.theme.MyInventarioAppTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                android.graphics.Color.BLACK
            )
        ) // 👈 solo aquí
        setContent {
            MyInventarioAppTheme {
                    val navController = rememberNavController()
                    AppNavGraph(navController = navController)
            }
        }
    }
}

