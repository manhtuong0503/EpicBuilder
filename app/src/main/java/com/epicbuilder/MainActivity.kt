package com.epicbuilder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.epicbuilder.ui.defense.DefenseScreen
import com.epicbuilder.ui.home.HomeScreen
import com.epicbuilder.ui.offense.OffenseScreen
import com.epicbuilder.ui.theme.EpicBuilderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EpicBuilderTheme(darkTheme = true) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    EpicBuilderNavHost()
                }
            }
        }
    }
}

@Composable
private fun EpicBuilderNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onOpenDefense = { navController.navigate("defense") },
                onOpenOffense = { navController.navigate("offense") }
            )
        }
        composable("defense") {
            DefenseScreen(onBack = { navController.popBackStack() })
        }
        composable("offense") {
            OffenseScreen(onBack = { navController.popBackStack() })
        }
    }
}
