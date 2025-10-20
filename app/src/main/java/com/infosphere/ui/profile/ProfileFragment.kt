package com.infosphere.ui.profile

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.infosphere.R
import com.infosphere.databinding.FragmentProfileBinding
import com.infosphere.ui.adapter.EventAdapter
import com.infosphere.viewmodel.AuthState
import com.infosphere.viewmodel.AuthViewModel
import com.infosphere.viewmodel.EventViewModel
import com.infosphere.viewmodel.UserProfileViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels()
    private val eventViewModel: EventViewModel by activityViewModels()
    private val userProfileViewModel: UserProfileViewModel by activityViewModels()

    private lateinit var eventAdapter: EventAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupObservers()
        setupListeners()
        
        eventViewModel.loadUserEvents()
    }

    private fun setupRecyclerView() {
        eventAdapter = EventAdapter(
            onEventClick = { event ->
                Toast.makeText(requireContext(), event.title, Toast.LENGTH_SHORT).show()
            }
        )
        
        binding.rvMyEvents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = eventAdapter
        }
    }

    private fun setupObservers() {
        // Observe current user
        authViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.tvUserName.text = user.displayName ?: "Utilisateur"
                binding.tvUserEmail.text = user.email
            }
        }

        // Observe user profile
        userProfileViewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                displaySelectedCities(user.selectedCityIds)
            }
        }

        // Observe cities for display
        userProfileViewModel.allCities.observe(viewLifecycleOwner) { cities ->
            userProfileViewModel.user.value?.let { user ->
                displaySelectedCities(user.selectedCityIds)
            }
        }

        // Observe user events
        eventViewModel.userEvents.observe(viewLifecycleOwner) { events ->
            if (events.isEmpty()) {
                binding.tvNoEvents.visibility = View.VISIBLE
                binding.rvMyEvents.visibility = View.GONE
            } else {
                binding.tvNoEvents.visibility = View.GONE
                binding.rvMyEvents.visibility = View.VISIBLE
                eventAdapter.submitList(events)
            }
        }

        // Observe event types for adapter
        userProfileViewModel.allEventTypes.observe(viewLifecycleOwner) { types ->
            eventAdapter.updateEventTypes(types)
        }

        // Observe auth state
        authViewModel.authState.observe(viewLifecycleOwner) { state ->
            if (state is AuthState.Unauthenticated) {
                findNavController().navigate(R.id.action_to_login)
            }
        }
    }

    private fun displaySelectedCities(cityIds: List<String>) {
        binding.chipGroupCities.removeAllViews()
        
        val allCities = userProfileViewModel.allCities.value ?: emptyList()
        cityIds.forEach { cityId ->
            val cityName = allCities.find { it.id == cityId }?.name ?: cityId
            val chip = Chip(requireContext()).apply {
                text = cityName
                isClickable = false
                isCheckable = false
            }
            binding.chipGroupCities.addView(chip)
        }
    }

    private fun setupListeners() {
        binding.btnEditCities.setOnClickListener {
            showCitySelectionDialog()
        }

        binding.btnSignOut.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Déconnexion")
                .setMessage("Voulez-vous vraiment vous déconnecter?")
                .setPositiveButton("Oui") { _, _ ->
                    authViewModel.signOut()
                }
                .setNegativeButton("Non", null)
                .show()
        }
    }

    private fun showCitySelectionDialog() {
        val allCities = userProfileViewModel.allCities.value ?: emptyList()
        val currentUser = userProfileViewModel.user.value
        
        if (allCities.isEmpty()) {
            Toast.makeText(requireContext(), "Chargement des villes...", Toast.LENGTH_SHORT).show()
            return
        }

        val cityNames = allCities.map { it.name }.toTypedArray()
        val selectedCityIds = currentUser?.selectedCityIds?.toMutableList() ?: mutableListOf()
        val checkedItems = allCities.map { city ->
            selectedCityIds.contains(city.id)
        }.toBooleanArray()

        AlertDialog.Builder(requireContext())
            .setTitle("Sélectionner vos villes")
            .setMultiChoiceItems(cityNames, checkedItems) { _, which, isChecked ->
                val cityId = allCities[which].id
                if (isChecked) {
                    if (!selectedCityIds.contains(cityId)) {
                        selectedCityIds.add(cityId)
                    }
                } else {
                    selectedCityIds.remove(cityId)
                }
            }
            .setPositiveButton("Enregistrer") { _, _ ->
                userProfileViewModel.updateUserCities(selectedCityIds)
                Toast.makeText(requireContext(), "Villes mises à jour", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
