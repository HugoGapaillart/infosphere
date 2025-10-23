package com.infosphere.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.infosphere.enums.GameMode
import com.infosphere.ui.screens.*
import com.infosphere.models.Event
import com.infosphere.viewmodel.AuthState
import com.infosphere.viewmodel.AuthViewModel
import com.infosphere.viewmodel.EventViewModel
import com.infosphere.viewmodel.UserProfileViewModel

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

@Composable
fun InfosphereApp(
    authViewModel: AuthViewModel = viewModel(),
    eventViewModel: EventViewModel = viewModel(),
    userProfileViewModel: UserProfileViewModel = viewModel()
) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    // Navigate to login if not authenticated
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Unauthenticated -> {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
            is AuthState.Authenticated -> {
                if (navController.currentDestination?.route == Screen.Login.route) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }
            else -> {}
        }
    }

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Home.route, Icons.Filled.Home, "Accueil"),
        BottomNavItem(Screen.Search.route, Icons.Filled.Search, "Rechercher"),
        BottomNavItem(Screen.AddEvent.route, Icons.Filled.Add, "Ajouter"),
        BottomNavItem(Screen.Profile.route, Icons.Filled.Person, "Profil"),
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = currentDestination?.route != Screen.Login.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = authViewModel,
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    authViewModel = authViewModel,
                    eventViewModel = eventViewModel,
                    userProfileViewModel = userProfileViewModel,
                    onEventClick = { event ->
                        navController.navigate(Screen.EventDetail.createRoute(event.id))
                    }
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    eventViewModel = eventViewModel,
                    userProfileViewModel = userProfileViewModel,
                    onEventClick = { eventId ->
                        navController.navigate(Screen.EventDetail.createRoute(eventId))
                    }
                )
            }

            composable(Screen.AddEvent.route) {
                AddEventScreen(
                    eventViewModel = eventViewModel,
                    userProfileViewModel = userProfileViewModel,
                    onEventCreated = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    navController,
                    authViewModel = authViewModel,
                    userProfileViewModel = userProfileViewModel,
                    eventViewModel = eventViewModel,
                    onSignOut = {
                        authViewModel.signOut()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onEventClick = { eventId ->
                        navController.navigate(Screen.EventDetail.createRoute(eventId))
                    }
                )
            }

            composable(Screen.EventDetail.route) {
                backStackEntry ->
                    val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                    EventDetailScreen(
                        eventId = eventId,
                        eventViewModel = eventViewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onEditEvent = { event, id ->
                            navController.navigate(Screen.EditEvent.createRoute(id))
                        }
                    )
            }

            composable(Screen.EditEvent.route) {
                backStackEntry ->
                    val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                    var event by remember { mutableStateOf<Event?>(null) }
                    var isLoading by remember { mutableStateOf(true) }

                    LaunchedEffect(eventId) {
                        val result = eventViewModel.getEvent(eventId)
                        result.onSuccess { eventData ->
                            event = eventData
                            isLoading = false
                        }.onFailure {
                            navController.popBackStack()
                        }
                    }

                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (event != null) {
                        EditEventScreen(
                            event = event!!,
                            eventId = eventId,
                            eventViewModel = eventViewModel,
                            userProfileViewModel = userProfileViewModel,
                            onEventUpdated = {
                                navController.popBackStack()
                            },
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
            }

            composable(Screen.GameMenu.route) {
                navBackStackEntry -> GameMenuScreen(navController)
            }

            composable(Screen.GameScreen.route) {
                navBackStackEntry ->
                    val modeName = navBackStackEntry.arguments?.getString("modeName")
                    val gameMode = runCatching { GameMode.valueOf(modeName ?: GameMode.NORMAL.name) }.getOrDefault(GameMode.NORMAL)
                    GameScreen(gameMode = gameMode, navController = navController)
            }

        }
    }
}
