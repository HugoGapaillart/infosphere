package com.infosphere.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.firebase.Timestamp
import com.infosphere.models.Event
import com.infosphere.viewmodel.EventViewModel
import com.infosphere.viewmodel.OperationState
import com.infosphere.viewmodel.UnsplashViewModel
import com.infosphere.viewmodel.UserProfileViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventScreen(
    event: Event,
    eventId: String,
    eventViewModel: EventViewModel,
    userProfileViewModel: UserProfileViewModel,
    onEventUpdated: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val operationState by eventViewModel.operationState.collectAsStateWithLifecycle()
    val cities by userProfileViewModel.allCities.collectAsStateWithLifecycle()
    val eventTypes by eventViewModel.allEventTypes.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf(event.title) }
    var description by remember { mutableStateOf(event.description) }
    var existingPhotoUrls by remember { mutableStateOf(event.photoUrls) }
    var newPhotos by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var selectedDate by remember { mutableStateOf(event.date.toDate().time) }
    var selectedCityId by remember { mutableStateOf<String?>(event.cityId) }
    var selectedTypeIds by remember { mutableStateOf(event.eventTypes.toSet()) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showCityDropdown by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var titleError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var cityError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }
    var typeError by remember { mutableStateOf<String?>(null) }
    var isUpdating by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd MMMM yyyy à HH:mm", Locale.FRENCH) }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        newPhotos = uris
    }

    // Unsplash preview when no photos are selected
    val unsplashVm: UnsplashViewModel = viewModel()
    val unsplashPhoto by unsplashVm.photo.collectAsStateWithLifecycle()
    val unsplashLoading by unsplashVm.isLoading.collectAsStateWithLifecycle()

    // Reset operation state on screen entry
    LaunchedEffect(Unit) {
        eventViewModel.resetOperationState()
    }

    // Handle operation state
    LaunchedEffect(operationState) {
        if (isUpdating && operationState is OperationState.Success) {
            onEventUpdated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modifier l'événement") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showDeleteDialog = true }
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Supprimer",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Photos section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Photo,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Photos de l'événement",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Show new photos if selected
                        if (newPhotos.isNotEmpty()) {
                            Text(
                                "Nouvelles photos",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                newPhotos.take(3).forEach { uri ->
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }

                            if (newPhotos.size > 3) {
                                Text(
                                    "+${newPhotos.size - 3} photo(s)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            TextButton(onClick = { newPhotos = emptyList() }) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Annuler nouvelles photos")
                            }
                        }

                        // Show existing photos
                        if (existingPhotoUrls.isNotEmpty()) {
                            if (newPhotos.isNotEmpty()) {
                                Text(
                                    "Photos existantes",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                existingPhotoUrls.take(3).forEach { url ->
                                    AsyncImage(
                                        model = url,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }

                            if (existingPhotoUrls.size > 3) {
                                Text(
                                    "+${existingPhotoUrls.size - 3} photo(s)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { existingPhotoUrls = emptyList() }) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Supprimer existantes")
                                }
                            }
                        }

                        // Show Unsplash preview or load button when no photos
                        if (existingPhotoUrls.isEmpty() && newPhotos.isEmpty()) {
                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (unsplashPhoto != null) {
                                    AsyncImage(
                                        model = unsplashPhoto!!.urls.small,
                                        contentDescription = unsplashPhoto!!.altDescription ?: unsplashPhoto!!.description,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(160.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    OutlinedButton(
                                        onClick = { unsplashVm.loadRandomPhoto() },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Nouvelle image aléatoire")
                                    }
                                } else if (unsplashLoading) {
                                    Box(modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator()
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { unsplashVm.loadRandomPhoto() },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Image, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Charger une image aléatoire")
                                    }
                                }
                            }
                        }

                        // Always show add photos button
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (newPhotos.isEmpty()) "Ajouter des photos" else "Ajouter plus de photos")
                        }
                    }
                }

                // Title field
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = null
                    },
                    label = { Text("Titre de l'événement") },
                    leadingIcon = {
                        Icon(Icons.Default.Title, contentDescription = null)
                    },
                    isError = titleError != null,
                    supportingText = titleError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Description field
                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        descriptionError = null
                    },
                    label = { Text("Description") },
                    leadingIcon = {
                        Icon(Icons.Default.Description, contentDescription = null)
                    },
                    isError = descriptionError != null,
                    supportingText = descriptionError?.let { { Text(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    minLines = 4,
                    maxLines = 8
                )

                // Date & Time picker
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Event,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Date et heure",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                selectedDate?.let { dateFormatter.format(Date(it)) } ?: "Sélectionner une date"
                            )
                        }

                        if (dateError != null) {
                            Text(
                                dateError!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                // City dropdown
                ExposedDropdownMenuBox(
                    expanded = showCityDropdown,
                    onExpandedChange = { showCityDropdown = it }
                ) {
                    OutlinedTextField(
                        value = cities.find { it.id == selectedCityId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Ville") },
                        leadingIcon = {
                            Icon(Icons.Default.LocationCity, contentDescription = null)
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCityDropdown)
                        },
                        isError = cityError != null,
                        supportingText = cityError?.let { { Text(it) } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = showCityDropdown,
                        onDismissRequest = { showCityDropdown = false }
                    ) {
                        cities.forEach { city ->
                            DropdownMenuItem(
                                text = { Text(city.name) },
                                onClick = {
                                    selectedCityId = city.id
                                    cityError = null
                                    showCityDropdown = false
                                }
                            )
                        }
                    }
                }

                // Event types
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Category,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Types d'événement",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            eventTypes.forEach { type ->
                                FilterChip(
                                    selected = type.id in selectedTypeIds,
                                    onClick = {
                                        selectedTypeIds = if (type.id in selectedTypeIds) {
                                            selectedTypeIds - type.id
                                        } else {
                                            selectedTypeIds + type.id
                                        }
                                        typeError = null
                                    },
                                    label = { Text(type.name) },
                                    leadingIcon = if (type.id in selectedTypeIds) {
                                        {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    } else null
                                )
                            }
                        }

                        if (typeError != null) {
                            Text(
                                typeError!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                // Error message
                if (operationState is OperationState.Error) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                (operationState as OperationState.Error).message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // Update button
                Button(
                    onClick = {
                        var hasError = false

                        if (title.isBlank()) {
                            titleError = "Le titre est requis"
                            hasError = true
                        }

                        if (description.isBlank()) {
                            descriptionError = "La description est requise"
                            hasError = true
                        }

                        if (selectedDate == null) {
                            dateError = "La date est requise"
                            hasError = true
                        }

                        if (selectedCityId == null) {
                            cityError = "La ville est requise"
                            hasError = true
                        }

                        if (selectedTypeIds.isEmpty()) {
                            typeError = "Sélectionnez au moins un type"
                            hasError = true
                        }

                        if (!hasError) {
                            isUpdating = true
                            val selectedCity = cities.find { it.id == selectedCityId }
                            eventViewModel.updateEvent(
                                eventId = eventId,
                                title = title,
                                description = description,
                                date = Timestamp(Date(selectedDate!!)),
                                cityId = selectedCityId!!,
                                cityName = selectedCity?.name ?: "",
                                typeIds = selectedTypeIds.toList(),
                                existingPhotoUrls = existingPhotoUrls,
                                newPhotoUris = newPhotos,
                                unsplashPhotoUrl = if (existingPhotoUrls.isEmpty() && newPhotos.isEmpty())
                                    unsplashPhoto?.urls?.regular else null
                            )
                        }
                    },
                    enabled = operationState !is OperationState.Loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    if (operationState is OperationState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Mise à jour en cours...")
                    } else {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Mettre à jour l'événement")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        selectedDate = it
                        dateError = null
                    }
                    showDatePicker = false
                    showTimePicker = true
                }) {
                    Text("Suivant")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Annuler")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        val calendar = remember { Calendar.getInstance().apply { timeInMillis = selectedDate ?: System.currentTimeMillis() } }
        val timePickerState = rememberTimePickerState(
            initialHour = calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(Calendar.MINUTE)
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Sélectionner l'heure") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedDate?.let { dateMillis ->
                        val newCalendar = Calendar.getInstance().apply {
                            timeInMillis = dateMillis
                            set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            set(Calendar.MINUTE, timePickerState.minute)
                            set(Calendar.SECOND, 0)
                        }
                        selectedDate = newCalendar.timeInMillis
                    }
                    showTimePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Supprimer l'événement ?") },
            text = { Text("Cette action est irréversible. L'événement sera définitivement supprimé.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        eventViewModel.deleteEvent(eventId)
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

