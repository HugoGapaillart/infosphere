# Architecture de l'application Infosphere

## 📐 Pattern architectural : MVVM (Model-View-ViewModel)

```
┌─────────────────────────────────────────────────────────┐
│                     View Layer                          │
│  (Fragments + Activities + XML Layouts)                │
│  - LoginFragment                                        │
│  - HomeFragment                                         │
│  - SearchFragment                                       │
│  - AddEventFragment                                     │
│  - ProfileFragment                                      │
│  - MainActivity                                         │
└──────────────────┬──────────────────────────────────────┘
                   │ observe LiveData / bind data
                   ▼
┌─────────────────────────────────────────────────────────┐
│                   ViewModel Layer                       │
│  (Business Logic + State Management)                   │
│  - AuthViewModel                                        │
│  - EventViewModel                                       │
│  - UserProfileViewModel                                 │
└──────────────────┬──────────────────────────────────────┘
                   │ calls repository methods
                   ▼
┌─────────────────────────────────────────────────────────┐
│                  Repository Layer                       │
│  (Data Access Abstraction)                             │
│  - AuthRepository                                       │
│  - EventRepository                                      │
│  - UserRepository                                       │
│  - CityRepository                                       │
│  - EventTypeRepository                                  │
└──────────────────┬──────────────────────────────────────┘
                   │ interacts with Firebase
                   ▼
┌─────────────────────────────────────────────────────────┐
│                    Data Layer                           │
│  (Firebase SDK + Local Storage)                        │
│  - Firebase Authentication                              │
│  - Cloud Firestore                                      │
│  - Cloud Storage                                        │
└─────────────────────────────────────────────────────────┘
```

## 🗂️ Structure des packages

```
com.infosphere/
│
├── models/                    # Data classes
│   ├── Event.kt              # Événement avec date, lieu, photos, types
│   ├── User.kt               # Utilisateur avec villes sélectionnées
│   ├── City.kt               # Ville avec nom, pays, région
│   └── EventType.kt          # Type d'événement avec nom, icône
│
├── repository/               # Data access layer
│   ├── AuthRepository.kt     # Authentification Firebase
│   ├── EventRepository.kt    # CRUD événements + recherche
│   ├── UserRepository.kt     # CRUD profil utilisateur
│   ├── CityRepository.kt     # Récupération villes
│   └── EventTypeRepository.kt # Récupération types
│
├── viewmodel/                # Business logic
│   ├── AuthViewModel.kt      # Gestion auth (login, signup, logout)
│   ├── EventViewModel.kt     # Gestion événements (CRUD, recherche)
│   └── UserProfileViewModel.kt # Gestion profil (villes, données)
│
├── ui/                       # User interface
│   ├── adapter/
│   │   └── EventAdapter.kt   # RecyclerView adapter pour événements
│   ├── auth/
│   │   └── LoginFragment.kt  # Connexion/Inscription
│   ├── home/
│   │   └── HomeFragment.kt   # Feed personnalisé d'événements
│   ├── search/
│   │   └── SearchFragment.kt # Recherche par ville/type
│   ├── addevent/
│   │   └── AddEventFragment.kt # Création d'événement
│   └── profile/
│       └── ProfileFragment.kt # Profil et mes événements
│
└── MainActivity.kt           # Point d'entrée avec navigation
```

## 🔄 Flux de données

### Exemple : Chargement des événements

```
1. HomeFragment observe eventViewModel.events (LiveData)
   ↓
2. UserProfileViewModel charge les villes sélectionnées de l'utilisateur
   ↓
3. EventViewModel.loadEventsByCities(cityIds)
   ↓
4. EventRepository.getUpcomingEventsByCities(cityIds)
   ↓
5. Firestore query avec whereIn("cityId", cityIds)
   ↓
6. Firestore renvoie les événements en temps réel (Flow)
   ↓
7. EventRepository transforme en List<Event>
   ↓
8. EventViewModel met à jour le LiveData _events
   ↓
9. HomeFragment observe le changement et met à jour l'UI
   ↓
10. EventAdapter affiche la liste dans RecyclerView
```

### Exemple : Création d'un événement

```
1. AddEventFragment collecte les données du formulaire
   ↓
2. Validation des champs
   ↓
3. EventViewModel.createEvent(...) appelé
   ↓
4. EventRepository.createEvent(event)
   ↓
5. Firestore crée le document event
   ↓
6. Pour chaque photo, EventRepository.uploadEventPhoto(eventId, uri)
   ↓
7. Storage upload avec path "event_photos/{eventId}_{uuid}.jpg"
   ↓
8. Récupération des downloadUrls
   ↓
9. EventRepository.updateEvent(eventId, {photoUrls: urls})
   ↓
10. OperationState.Success dans ViewModel
   ↓
11. AddEventFragment observe le succès et navigue en arrière
```

## 🔐 Sécurité

### Règles Firestore

- **cities** : Lecture publique, écriture admin uniquement
- **eventTypes** : Lecture publique, écriture admin uniquement
- **users** : Lecture authentifiée, écriture propriétaire uniquement
- **events** : Lecture publique, création authentifiée, modification propriétaire uniquement

### Règles Storage

- **event_photos/** : Lecture publique, écriture authentifiée uniquement

## 📱 Navigation

```
LoginFragment (startDestination si non authentifié)
    │
    └─→ [après login] → HomeFragment
                           │
            ┌──────────────┼──────────────┬──────────────┐
            │              │              │              │
       HomeFragment  SearchFragment  AddEventFragment  ProfileFragment
         (Accueil)    (Recherche)      (Ajouter)        (Profil)
            │                                               │
            │                                               │
            └───────────────────────────────────────────────┘
                              │
                              └─→ [logout] → LoginFragment
```

## 🔧 Technologies utilisées

### Core
- **Kotlin** : Langage principal
- **Android SDK** : Framework Android
- **View Binding** : Binding type-safe des vues

### Architecture
- **MVVM** : Séparation des responsabilités
- **Repository Pattern** : Abstraction de la source de données
- **LiveData** : Observable data holder
- **ViewModel** : Survit aux changements de configuration

### Async
- **Kotlin Coroutines** : Programmation asynchrone
- **Flow** : Reactive streams
- **kotlinx-coroutines-play-services** : Bridge Firebase-Coroutines

### Firebase
- **Firebase Authentication** : Authentification email/password
- **Cloud Firestore** : Base de données NoSQL temps réel
- **Cloud Storage** : Stockage de photos

### UI
- **Material Design 3** : Design system
- **Navigation Component** : Navigation entre fragments
- **RecyclerView** : Listes performantes
- **SwipeRefreshLayout** : Pull-to-refresh
- **Coil** : Chargement d'images

## 🎯 Principes de conception

1. **Single Responsibility** : Chaque classe a une responsabilité unique
2. **Dependency Inversion** : Les ViewModels dépendent d'abstractions (repositories)
3. **Don't Repeat Yourself** : Code réutilisable (adapter, repositories)
4. **Separation of Concerns** : UI, logique métier et données séparées
5. **Reactive Programming** : UI réagit aux changements de données

## 🚀 Évolutions possibles

### Court terme
- Page de détails d'événement
- Filtre par date dans la recherche
- Pagination des événements
- Cache local avec Room

### Moyen terme
- Géolocalisation des événements
- Carte interactive
- Notifications push pour nouveaux événements
- Favoris/Bookmarks
- Partage d'événements

### Long terme
- Mode offline avec synchronisation
- Messagerie entre utilisateurs
- Système de recommandation IA
- Intégration calendrier
- Analytics et statistiques
