package com.infosphere.ui.addevent

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.firebase.Timestamp
import com.infosphere.databinding.FragmentAddEventBinding
import com.infosphere.models.City
import com.infosphere.viewmodel.EventViewModel
import com.infosphere.viewmodel.OperationState
import com.infosphere.viewmodel.UserProfileViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddEventFragment : Fragment() {

    private var _binding: FragmentAddEventBinding? = null
    private val binding get() = _binding!!

    private val eventViewModel: EventViewModel by activityViewModels()
    private val userProfileViewModel: UserProfileViewModel by activityViewModels()
    
    private var selectedDate: Date? = null
    private var selectedCityId: String? = null
    private var selectedCityName: String? = null
    private val selectedTypeIds = mutableListOf<String>()
    private val selectedPhotoUris = mutableListOf<Uri>()
    
    private val pickImages = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        selectedPhotoUris.clear()
        selectedPhotoUris.addAll(uris)
        Toast.makeText(requireContext(), "${uris.size} photos sélectionnées", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupObservers()
        setupListeners()
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
        }

        // Observe operation state
        eventViewModel.operationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is OperationState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnCreateEvent.isEnabled = false
                }
                is OperationState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnCreateEvent.isEnabled = true
                    Toast.makeText(requireContext(), "Événement créé avec succès!", Toast.LENGTH_SHORT).show()
                    eventViewModel.resetOperationState()
                    findNavController().navigateUp()
                }
                is OperationState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnCreateEvent.isEnabled = true
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
                is OperationState.Idle -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnCreateEvent.isEnabled = true
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
            selectedCityName = cities[position].name
        }
    }

    private fun setupListeners() {
        binding.etDate.setOnClickListener {
            showDateTimePicker()
        }

        binding.btnAddPhotos.setOnClickListener {
            pickImages.launch("image/*")
        }

        binding.btnCreateEvent.setOnClickListener {
            createEvent()
        }
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                
                TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        
                        selectedDate = calendar.time
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH)
                        binding.etDate.setText(dateFormat.format(selectedDate))
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis()
        }.show()
    }

    private fun createEvent() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()
        
        // Validation
        when {
            title.isEmpty() -> {
                binding.tilTitle.error = "Titre requis"
                return
            }
            description.isEmpty() -> {
                binding.tilDescription.error = "Description requise"
                return
            }
            location.isEmpty() -> {
                binding.tilLocation.error = "Lieu requis"
                return
            }
            selectedDate == null -> {
                binding.tilDate.error = "Date requise"
                return
            }
            selectedCityId == null -> {
                binding.tilCity.error = "Ville requise"
                return
            }
            selectedTypeIds.isEmpty() -> {
                Toast.makeText(requireContext(), "Sélectionnez au moins un type", Toast.LENGTH_SHORT).show()
                return
            }
        }
        
        eventViewModel.createEvent(
            title = title,
            description = description,
            location = location,
            date = Timestamp(selectedDate!!),
            cityId = selectedCityId!!,
            cityName = selectedCityName!!,
            eventTypes = selectedTypeIds,
            photoUris = selectedPhotoUris
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
