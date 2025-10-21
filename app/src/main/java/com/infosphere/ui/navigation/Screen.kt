package com.infosphere.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Search : Screen("search")
    object AddEvent : Screen("add_event")
    object Profile : Screen("profile")
    object EventDetail : Screen("event_detail/{eventId}") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }
}
