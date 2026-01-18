package com.example.myinventarioapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.myinventarioapp.ui.screens.AppNavGraph
import com.example.myinventarioapp.ui.theme.MyInventarioAppTheme
import com.example.myinventarioapp.ui.theme.AjustarBarraEstado


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyInventarioAppTheme {
                AjustarBarraEstado()
                    val navController = rememberNavController()
                    AppNavGraph(navController = navController)
            }
        }
    }
}

