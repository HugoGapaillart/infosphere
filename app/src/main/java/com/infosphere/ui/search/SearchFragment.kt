package com.infosphere.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.infosphere.R
import com.infosphere.databinding.FragmentSearchBinding
import com.infosphere.models.City
import com.infosphere.models.EventType
import com.infosphere.ui.adapter.EventAdapter
import com.infosphere.viewmodel.EventViewModel
import com.infosphere.viewmodel.OperationState
import com.infosphere.viewmodel.UserProfileViewModel

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val eventViewModel: EventViewModel by activityViewModels()
    private val userProfileViewModel: UserProfileViewModel by activityViewModels()
    
    private lateinit var eventAdapter: EventAdapter
    private var selectedCityId: String? = null
    private val selectedTypeIds = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun setupRecyclerView() {
        eventAdapter = EventAdapter(
            onEventClick = { event ->
                Toast.makeText(requireContext(), event.title, Toast.LENGTH_SHORT).show()
            }
        )
        
        binding.rvSearchResults.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = eventAdapter
        }
    }

    private fun setupObservers() {
        // Observe cities
        userProfileViewModel.allCities.observe(viewLifecycleOwner) { cities ->
            setupCityDropdown(cities)
        }

        // Observe event types
        userProfileViewModel.allEventTypes.observe(viewLifecycleOwner) { types ->
            binding.chipGroupTypes.removeAllViews()
            types.forEach { type ->
                val chip = Chip(requireContext()).apply {
                    text = type.name
                    isCheckable = true
                    setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            selectedTypeIds.add(type.id)
                        } else {
                            selectedTypeIds.remove(type.id)
                        }
                    }
                }
                binding.chipGroupTypes.addView(chip)
            }
            
            // Update adapter with types
            eventAdapter = EventAdapter(
                onEventClick = { event ->
                    Toast.makeText(requireContext(), event.title, Toast.LENGTH_SHORT).show()
                },
                eventTypes = types as MutableList<EventType>
            )
            binding.rvSearchResults.adapter = eventAdapter
        }

        // Observe search results
        eventViewModel.searchResults.observe(viewLifecycleOwner) { events ->
            if (events.isEmpty()) {
                binding.tvNoResults.visibility = View.VISIBLE
                binding.rvSearchResults.visibility = View.GONE
            } else {
                binding.tvNoResults.visibility = View.GONE
                binding.rvSearchResults.visibility = View.VISIBLE
                eventAdapter.submitList(events)
            }
        }

        // Observe operation state
        eventViewModel.operationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is OperationState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSearch.isEnabled = false
                }
                is OperationState.Success, is OperationState.Idle -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSearch.isEnabled = true
                }
                is OperationState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSearch.isEnabled = true
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupCityDropdown(cities: List<City>) {
        val cityNames = cities.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, cityNames)
        binding.actvCity.setAdapter(adapter)
        
        binding.actvCity.setOnItemClickListener { _, _, position, _ ->
            selectedCityId = cities[position].id
        }
    }

    private fun setupListeners() {
        binding.btnSearch.setOnClickListener {
            performSearch()
        }
    }

    private fun performSearch() {
        eventViewModel.searchEvents(
            cityId = selectedCityId,
            eventTypes = selectedTypeIds.takeIf { it.isNotEmpty() }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
