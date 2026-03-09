package com.handyteejay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.handyteejay.ui.screens.auth.LoginScreen
import com.handyteejay.ui.screens.auth.SignUpScreen
import com.handyteejay.ui.screens.passenger.PassengerMapScreen
import com.handyteejay.ui.theme.HandyTeejayTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HandyTeejayTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    NavHost(navController = navController, startDestination = "login") {
                        composable("login") {
                            LoginScreen(
                                onNavigateToSignUp = { navController.navigate("signup") },
                                onLoginSuccess = { navController.navigate("passenger") { popUpTo(0) } }
                            )
                        }
                        composable("signup") {
                            SignUpScreen(
                                onNavigateToLogin = { navController.popBackStack() },
                                onSignUpSuccess = { navController.navigate("passenger") { popUpTo(0) } }
                            )
                        }
                        composable("passenger") {
                            PassengerMapScreen(
                                onMenuClick = { }
                            )
                        }
                    }
                }
            }
        }
    }

}
