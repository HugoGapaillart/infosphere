# Prochaines Étapes - Infosphere Compose 🚀

## ✅ Terminé

- [x] Migration complète vers Jetpack Compose
- [x] ViewModels avec StateFlow
- [x] Navigation Compose avec Bottom Bar
- [x] Thème Material3 personnalisé
- [x] 3 écrans principaux (Login, Home, Search)
- [x] Composants réutilisables (EventCard, LoadingIndicator, etc.)
- [x] Suppression de tout le code XML/Fragment legacy
- [x] Documentation complète (COMPOSE_MIGRATION.md)

## 📋 À implémenter

### 1. AddEventScreen (Priorité Haute)
**Fichier**: `app/src/main/java/com/infosphere/ui/screens/AddEventScreen.kt`

**Fonctionnalités**:
- [ ] Photo picker avec `rememberLauncherForActivityResult`
- [ ] Champs de formulaire validés
  - [ ] Titre (TextField)
  - [ ] Description (TextField multiline)
  - [ ] Date et heure (DatePicker + TimePicker)
  - [ ] Ville (ExposedDropdownMenu)
  - [ ] Types d'événements (FilterChip multi-sélection)
- [ ] Aperçu des photos sélectionnées (LazyRow)
- [ ] Bouton de création avec état de chargement
- [ ] Validation des données
- [ ] Navigation vers Home après création

**Exemple de structure**:
```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(
    eventViewModel: EventViewModel,
    userProfileViewModel: UserProfileViewModel,
    onEventCreated: () -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPhotos by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var selectedCityId by remember { mutableStateOf<String?>(null) }
    var selectedTypes by remember { mutableStateOf<Set<String>>(emptySet()) }
    
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        selectedPhotos = uris
    }
    
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Créer un événement") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Photo picker button
            Button(
                onClick = { 
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    ) 
                }
            ) {
                Icon(Icons.Default.Photo, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Ajouter des photos")
            }
            
            // Photo preview
            if (selectedPhotos.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    items(selectedPhotos) { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                }
            }
            
            // Title field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Titre") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(Modifier.height(8.dp))
            
            // Description field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Date picker
            // City dropdown
            // Types chips
            // Create button
        }
    }
}
```

### 2. ProfileScreen (Priorité Haute)
**Fichier**: `app/src/main/java/com/infosphere/ui/screens/ProfileScreen.kt`

**Fonctionnalités**:
- [ ] Affichage des infos utilisateur
  - [ ] Email
  - [ ] Nom (si disponible)
  - [ ] Date de création du compte
- [ ] Section "Mes villes"
  - [ ] Liste des villes sélectionnées avec chips
  - [ ] Bouton pour ouvrir un Dialog de sélection
  - [ ] Dialog avec liste de toutes les villes (checkbox multi-sélection)
- [ ] Section "Mes événements"
  - [ ] LazyColumn avec EventCard
  - [ ] Uniquement les événements créés par l'utilisateur
  - [ ] Click pour éditer/supprimer
- [ ] Bouton de déconnexion
- [ ] Bouton de suppression de compte (avec confirmation)

**Exemple de structure**:
```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    userProfileViewModel: UserProfileViewModel,
    eventViewModel: EventViewModel,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val user by userProfileViewModel.user.collectAsStateWithLifecycle()
    val allCities by userProfileViewModel.allCities.collectAsStateWithLifecycle()
    val userEvents by eventViewModel.userEvents.collectAsStateWithLifecycle()
    
    var showCityDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mon Profil") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // User info card
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = currentUser?.email ?: "",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Membre depuis ${/* format date */}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Cities section
            Text(
                text = "Mes villes",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                user?.cityIds?.forEach { cityId ->
                    val city = allCities.find { it.id == cityId }
                    if (city != null) {
                        FilterChip(
                            selected = true,
                            onClick = { /* Remove city */ },
                            label = { Text(city.name) }
                        )
                    }
                }
                
                FilterChip(
                    selected = false,
                    onClick = { showCityDialog = true },
                    label = { Text("+ Ajouter") }
                )
            }
            
            Spacer(Modifier.height(24.dp))
            
            // User events section
            Text(
                text = "Mes événements",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            
            if (userEvents.isEmpty()) {
                EmptyState(
                    message = "Vous n'avez pas encore créé d'événement",
                    icon = Icons.Default.Event
                )
            } else {
                userEvents.forEach { event ->
                    EventCard(
                        event = event,
                        onClick = { /* Navigate to edit */ }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Sign out button
            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Déconnexion")
            }
        }
    }
    
    // City selection dialog
    if (showCityDialog) {
        CitySelectionDialog(
            cities = allCities,
            selectedCityIds = user?.cityIds ?: emptyList(),
            onDismiss = { showCityDialog = false },
            onConfirm = { selectedIds ->
                userProfileViewModel.updateUserCities(selectedIds)
                showCityDialog = false
            }
        )
    }
}
```

### 3. Détails d'un événement
**Fichier**: `app/src/main/java/com/infosphere/ui/screens/EventDetailScreen.kt`

**Fonctionnalités**:
- [ ] Affichage complet d'un événement
- [ ] Galerie de photos (Pager)
- [ ] Carte avec localisation
- [ ] Boutons de partage
- [ ] Bouton d'édition (si créateur)
- [ ] Bouton de suppression (si créateur avec confirmation)

**Navigation**:
```kotlin
// Dans Screen.kt
data class EventDetail(val eventId: String) : Screen("event_detail/{eventId}") {
    fun createRoute(eventId: String) = "event_detail/$eventId"
}
```

### 4. Améliorations UI/UX

#### Animations
- [ ] Transitions entre écrans
- [ ] AnimatedVisibility pour les états de chargement
- [ ] Shared element transition pour les photos

#### Pull-to-refresh
- [x] Déjà implémenté dans HomeScreen
- [ ] Ajouter dans SearchScreen

#### Skeleton loading
- [ ] Placeholders pendant le chargement des images
- [ ] Shimmer effect pour les cartes

#### Dark mode
- [x] Déjà supporté via Material3
- [ ] Tester et ajuster les couleurs si nécessaire

### 5. Fonctionnalités avancées

#### Recherche avancée
- [ ] Filtres de date (aujourd'hui, cette semaine, ce mois)
- [ ] Tri (date, popularité, proximité)
- [ ] Recherche par mot-clé dans titre/description

#### Notifications
- [ ] Push notifications pour nouveaux événements
- [ ] Rappels avant un événement

#### Favoris
- [ ] Marquer des événements comme favoris
- [ ] Section "Mes favoris" dans le profil

#### Participation
- [ ] Bouton "Je participe"
- [ ] Liste des participants
- [ ] Compteur de participants

### 6. Tests

#### Unit tests
- [ ] ViewModels (AuthViewModel, EventViewModel, UserProfileViewModel)
- [ ] Repositories

#### UI tests
- [ ] Navigation flow
- [ ] Login/Signup
- [ ] Création d'événement
- [ ] Recherche

### 7. Performance

- [ ] Image caching avec Coil
- [ ] Pagination pour les listes d'événements
- [ ] Lazy loading des images
- [ ] Optimisation des recompositions

### 8. Sécurité

- [ ] Firestore Security Rules
  ```javascript
  rules_version = '2';
  service cloud.firestore {
    match /databases/{database}/documents {
      match /users/{userId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
      match /events/{eventId} {
        allow read: if request.auth != null;
        allow create: if request.auth != null;
        allow update, delete: if request.auth != null && 
          request.auth.uid == resource.data.creatorId;
      }
      match /cities/{cityId} {
        allow read: if request.auth != null;
      }
      match /event_types/{typeId} {
        allow read: if request.auth != null;
      }
    }
  }
  ```
- [ ] Storage Security Rules
- [ ] Validation des données côté serveur (Cloud Functions)

### 9. Déploiement

- [ ] Icône de l'app
- [ ] Splash screen
- [ ] Génération du keystore
- [ ] Build de release
- [ ] Upload sur Google Play Console

## 📚 Ressources utiles

### Compose
- [Compose Layouts](https://developer.android.com/jetpack/compose/layouts)
- [State in Compose](https://developer.android.com/jetpack/compose/state)
- [Side-effects in Compose](https://developer.android.com/jetpack/compose/side-effects)
- [Lists and grids](https://developer.android.com/jetpack/compose/lists)

### Material3
- [Material3 Components](https://m3.material.io/components)
- [Material3 in Compose](https://developer.android.com/jetpack/compose/designsystems/material3)

### Firebase
- [Firebase Authentication](https://firebase.google.com/docs/auth/android/start)
- [Cloud Firestore](https://firebase.google.com/docs/firestore/quickstart)
- [Cloud Storage](https://firebase.google.com/docs/storage/android/start)

### Best practices
- [Now in Android app](https://github.com/android/nowinandroid) - Exemple d'app moderne
- [Architecture guide](https://developer.android.com/topic/architecture)
- [Kotlin style guide](https://developer.android.com/kotlin/style-guide)

---

**Bon développement! 🚀**
