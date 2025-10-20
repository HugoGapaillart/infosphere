package com.infosphere.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import com.infosphere.R
import com.infosphere.databinding.FragmentLoginBinding
import com.infosphere.viewmodel.AuthState
import com.infosphere.viewmodel.AuthViewModel

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels()
    private var isSignUpMode = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupTabLayout()
        setupObservers()
        setupListeners()
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        isSignUpMode = false
                        binding.tilDisplayName.visibility = View.GONE
                        binding.btnAuth.text = getString(R.string.sign_in)
                    }
                    1 -> {
                        isSignUpMode = true
                        binding.tilDisplayName.visibility = View.VISIBLE
                        binding.btnAuth.text = getString(R.string.sign_up)
                    }
                }
                clearErrors()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupObservers() {
        authViewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnAuth.isEnabled = false
                    binding.tvError.visibility = View.GONE
                }
                is AuthState.Authenticated -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnAuth.isEnabled = true
                    // Navigate to home
                    findNavController().navigate(R.id.action_login_to_home)
                }
                is AuthState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnAuth.isEnabled = true
                    binding.tvError.text = state.message
                    binding.tvError.visibility = View.VISIBLE
                }
                is AuthState.Unauthenticated -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnAuth.isEnabled = true
                }
                is AuthState.PasswordResetSent -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnAuth.isEnabled = true
                    Toast.makeText(
                        requireContext(),
                        "Email de réinitialisation envoyé",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnAuth.setOnClickListener {
            clearErrors()
            
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            
            // Validation
            when {
                email.isEmpty() -> {
                    binding.tilEmail.error = "Email requis"
                    return@setOnClickListener
                }
                password.isEmpty() -> {
                    binding.tilPassword.error = "Mot de passe requis"
                    return@setOnClickListener
                }
                password.length < 6 -> {
                    binding.tilPassword.error = "Mot de passe trop court (min 6 caractères)"
                    return@setOnClickListener
                }
            }
            
            if (isSignUpMode) {
                val displayName = binding.etDisplayName.text.toString().trim()
                if (displayName.isEmpty()) {
                    binding.tilDisplayName.error = "Nom requis"
                    return@setOnClickListener
                }
                authViewModel.signUp(email, password, displayName)
            } else {
                authViewModel.signIn(email, password)
            }
        }
    }

    private fun clearErrors() {
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilDisplayName.error = null
        binding.tvError.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
