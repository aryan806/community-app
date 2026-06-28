package com.communityheroai // FIXED: 19

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.communityheroai.ui.components.BottomNav
import com.communityheroai.ui.screens.*
import com.communityheroai.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DependencyProvider.init(applicationContext) // FIXED: 7
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
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route
    val showBottomNav = currentUser != null && currentRoute in listOf("home", "report", "profile")

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
                    currentUser = currentUser, // FIXED: 14
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
                val factory = object : ViewModelProvider.Factory { // FIXED: 4
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        val handle = SavedStateHandle(mapOf("issueId" to issueId))
                        return IssueDetailsViewModel(handle, DependencyProvider.issueRepository) as T
                    }
                }
                val viewModel: IssueDetailsViewModel = viewModel(factory = factory)
                IssueDetailsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onViewOnMap = { lat, lng, title ->
                        val uri = android.net.Uri.parse("geo:$lat,$lng?q=$lat,$lng($title)")
                        navController.context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, uri))
                    }
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
