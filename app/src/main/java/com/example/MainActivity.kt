package com.example

import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.BottomNav
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CommunityHeroApp()
            }
        }
    }
}

@Composable
fun CommunityHeroApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val isInitializing by authViewModel.isInitializing.collectAsStateWithLifecycle()
    
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route
    
    // Only show bottom nav on authenticated main screens
    val showBottomNav = currentUser != null && currentRoute in listOf("home", "report", "profile")

    if (isInitializing) {
        androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            androidx.compose.material3.CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                BottomNav(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (currentUser != null) "home" else "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }
            composable("home") {
                val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
                HomeScreen(
                    viewModel = homeViewModel,
                    currentUser = currentUser,
                    onNavigateToReport = { navController.navigate("report") },
                    onNavigateToIssue = { issueId -> navController.navigate("issue/$issueId") },
                    onNavigateToMap = { navController.navigate("map") }
                )
            }
            composable("report") {
                val reportViewModel: ReportViewModel = viewModel(factory = ReportViewModel.Factory)
                ReportScreen(
                    viewModel = reportViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("profile") {
                ProfileScreen(
                    viewModel = authViewModel,
                    onSignOut = {
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }
            composable("issue/{issueId}") { backStackEntry ->
                val issueId = backStackEntry.arguments?.getString("issueId") ?: ""
                IssueDetailsScreen(
                    issueId = issueId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("map") {
                val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
                MapScreen(
                    viewModel = homeViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToIssue = { issueId -> navController.navigate("issue/$issueId") },
                    onNavigateToReport = { navController.navigate("report") }
                )
            }
        }
    }
}
