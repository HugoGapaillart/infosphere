# Infosphere - Application d'Événements

Application Android en Kotlin avec Firebase pour gérer et découvrir des événements locaux.

## 📋 Fonctionnalités

- **Authentification** : Inscription et connexion avec Firebase Authentication
- **Gestion d'événements** : 
  - Créer des événements avec photos, description, lieu, date
  - Rechercher des événements par ville et par type
  - Voir les événements à venir dans vos villes favorites
- **Profil utilisateur** :
  - Sélectionner vos villes de rattachement
  - Voir vos événements créés
  - Événements passés accessibles uniquement par le créateur
- **Feed personnalisé** : Événements triés du plus récent au plus lointain

## 🚀 Configuration

### 1. Prérequis

- Android Studio Arctic Fox ou supérieur
- Compte Firebase
- JDK 11

### 2. Configuration Firebase

1. Créez un projet Firebase sur [Firebase Console](https://console.firebase.google.com)
2. Ajoutez une application Android avec le package `com.infosphere`
3. Téléchargez le fichier `google-services.json`
4. Remplacez le fichier `app/google-services.json` par votre fichier téléchargé
5. Activez les services Firebase :
   - **Authentication** : Email/Password
   - **Cloud Firestore** : Mode production
   - **Cloud Storage** : Mode production

### 3. Structure Firestore

Créez les collections suivantes dans Firestore :

#### Collection `cities`
```
{
  id: (auto-generated)
  name: "Paris"
  country: "France"
  region: "Île-de-France"
}
```

#### Collection `eventTypes`
```
{
  id: (auto-generated)
  name: "Concert"
  icon: "🎵"
}
```

Exemples de types d'événements à créer :
- Concert 🎵
- Sport ⚽
- Conférence 🎤
- Festival 🎪
- Exposition 🎨
- Cinéma 🎬
- Restaurant 🍽️

#### Collection `events`
Les événements sont créés automatiquement par l'application.

#### Collection `users`
Les profils utilisateurs sont créés automatiquement lors de l'inscription.

### 4. Règles de sécurité Firestore

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Cities - lecture publique, écriture admin uniquement
    match /cities/{cityId} {
      allow read: if true;
      allow write: if false; // À gérer via console
    }
    
    // Event Types - lecture publique, écriture admin uniquement
    match /eventTypes/{typeId} {
      allow read: if true;
      allow write: if false; // À gérer via console
    }
    
    // Users - lecture/écriture pour l'utilisateur authentifié
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Events - lecture publique, écriture pour utilisateurs authentifiés
    match /events/{eventId} {
      allow read: if true;
      allow create: if request.auth != null;
      allow update, delete: if request.auth != null && 
                               request.auth.uid == resource.data.createdBy;
    }
  }
}
```

### 5. Règles de sécurité Storage

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /event_photos/{imageId} {
      allow read: if true;
      allow write: if request.auth != null;
    }
  }
}
```

### 6. Initialiser des données de test

Pour tester l'application, ajoutez quelques villes et types d'événements via la console Firebase :

**Villes** :
- Paris, France, Île-de-France
- Lyon, France, Auvergne-Rhône-Alpes
- Marseille, France, Provence-Alpes-Côte d'Azur
- Toulouse, France, Occitanie
- Bordeaux, France, Nouvelle-Aquitaine

**Types d'événements** :
- Concert, 🎵
- Sport, ⚽
- Conférence, 🎤
- Festival, 🎪
- Exposition, 🎨

## 🏗️ Architecture

- **MVVM** : Model-View-ViewModel
- **Repository Pattern** : Abstraction de la couche données
- **Firebase SDK** : Backend serverless
- **Kotlin Coroutines** : Programmation asynchrone
- **Flow** : Reactive streams
- **View Binding** : Liaison de vues
- **Navigation Component** : Navigation entre fragments
- **Coil** : Chargement d'images

## 📱 Structure du projet

```
com.infosphere/
├── models/           # Data classes (Event, User, City, EventType)
├── repository/       # Firebase data access layer
├── viewmodel/        # Business logic (AuthViewModel, EventViewModel, UserProfileViewModel)
├── ui/
│   ├── adapter/      # RecyclerView adapters
│   ├── auth/         # LoginFragment
│   ├── home/         # HomeFragment
│   ├── search/       # SearchFragment
│   ├── addevent/     # AddEventFragment
│   └── profile/      # ProfileFragment
└── MainActivity      # Main activity with bottom navigation
```

## 🔧 Build

```bash
./gradlew build
```

## 📝 Notes

- Les événements passés ne sont plus affichés dans le feed mais restent accessibles dans le profil du créateur
- Les événements sont triés par date (du plus proche au plus lointain)
- L'utilisateur doit être connecté pour créer un événement
- L'utilisateur doit sélectionner au moins une ville pour voir des événements sur la page d'accueil
- Les photos sont stockées dans Firebase Storage
- Maximum 10 photos par événement (à configurer selon vos besoins)

## 📄 Licence

Ce projet est créé pour Infosphere.
