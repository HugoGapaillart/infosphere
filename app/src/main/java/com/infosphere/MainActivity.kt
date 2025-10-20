package com.infosphere

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.infosphere.databinding.ActivityMainBinding
import com.infosphere.viewmodel.AuthState
import com.infosphere.viewmodel.AuthViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        observeAuthState()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val bottomNav: BottomNavigationView = binding.bottomNavigation
        bottomNav.setupWithNavController(navController)

        // Show/hide bottom navigation based on destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment -> {
                    bottomNav.visibility = View.GONE
                }
                else -> {
                    bottomNav.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun observeAuthState() {
        authViewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Authenticated -> {
                    // User is logged in, navigate to home if on login screen
                    if (navController.currentDestination?.id == R.id.loginFragment) {
                        navController.navigate(R.id.action_login_to_home)
                    }
                }
                is AuthState.Unauthenticated -> {
                    // User is logged out, navigate to login screen
                    if (navController.currentDestination?.id != R.id.loginFragment) {
                        navController.navigate(R.id.loginFragment)
                    }
                }
                else -> {
                    // Do nothing for Loading, Error, PasswordResetSent states
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}