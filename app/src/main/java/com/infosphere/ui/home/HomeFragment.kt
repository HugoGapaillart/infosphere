package com.infosphere.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.infosphere.R
import com.infosphere.databinding.FragmentHomeBinding
import com.infosphere.ui.adapter.EventAdapter
import com.infosphere.viewmodel.AuthViewModel
import com.infosphere.viewmodel.EventViewModel
import com.infosphere.viewmodel.UserProfileViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
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
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupObservers()
        setupSwipeRefresh()
    }

    private fun setupRecyclerView() {
        eventAdapter = EventAdapter(
            onEventClick = { event ->
                // Handle event click - could navigate to event detail
                Toast.makeText(requireContext(), event.title, Toast.LENGTH_SHORT).show()
            }
        )
        
        binding.rvEvents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = eventAdapter
        }
    }

    private fun setupObservers() {
        // Observe current user
        authViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.tvWelcome.text = getString(R.string.welcome_user, user.displayName ?: user.email)
            }
        }

        // Observe user profile to get selected cities
        userProfileViewModel.user.observe(viewLifecycleOwner) { user ->
            binding.swipeRefresh.isRefreshing = false
            if (user != null) {
                if (user.selectedCityIds.isEmpty()) {
                    binding.tvNoCities.visibility = View.VISIBLE
                    binding.rvEvents.visibility = View.GONE
                    eventAdapter.submitList(emptyList()) // Clear events
                } else {
                    binding.tvNoCities.visibility = View.GONE
                    binding.rvEvents.visibility = View.VISIBLE
                    eventViewModel.loadEventsByCities(user.selectedCityIds)
                }
            }
        }

        // Observe events
        eventViewModel.events.observe(viewLifecycleOwner) { events ->
            binding.progressBar.visibility = View.GONE
            eventAdapter.submitList(events)
        }

        // Observe event types for adapter
        userProfileViewModel.allEventTypes.observe(viewLifecycleOwner) { types ->
            eventAdapter.updateEventTypes(types)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            userProfileViewModel.loadUserProfile()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
