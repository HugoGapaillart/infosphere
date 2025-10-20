# Script d'initialisation Firestore
# Copiez ces commandes dans la console Firebase ou utilisez le Firebase Admin SDK

## VILLES (Collection: cities)

### Paris
{
  "name": "Paris",
  "country": "France",
  "region": "Île-de-France"
}

### Lyon
{
  "name": "Lyon",
  "country": "France",
  "region": "Auvergne-Rhône-Alpes"
}

### Marseille
{
  "name": "Marseille",
  "country": "France",
  "region": "Provence-Alpes-Côte d'Azur"
}

### Toulouse
{
  "name": "Toulouse",
  "country": "France",
  "region": "Occitanie"
}

### Bordeaux
{
  "name": "Bordeaux",
  "country": "France",
  "region": "Nouvelle-Aquitaine"
}

### Nice
{
  "name": "Nice",
  "country": "France",
  "region": "Provence-Alpes-Côte d'Azur"
}

### Nantes
{
  "name": "Nantes",
  "country": "France",
  "region": "Pays de la Loire"
}

### Strasbourg
{
  "name": "Strasbourg",
  "country": "France",
  "region": "Grand Est"
}

### Montpellier
{
  "name": "Montpellier",
  "country": "France",
  "region": "Occitanie"
}

### Lille
{
  "name": "Lille",
  "country": "France",
  "region": "Hauts-de-France"
}

## TYPES D'ÉVÉNEMENTS (Collection: eventTypes)

### Concert
{
  "name": "Concert",
  "icon": "🎵"
}

### Sport
{
  "name": "Sport",
  "icon": "⚽"
}

### Conférence
{
  "name": "Conférence",
  "icon": "🎤"
}

### Festival
{
  "name": "Festival",
  "icon": "🎪"
}

### Exposition
{
  "name": "Exposition",
  "icon": "🎨"
}

### Cinéma
{
  "name": "Cinéma",
  "icon": "🎬"
}

### Restaurant
{
  "name": "Restaurant",
  "icon": "🍽️"
}

### Théâtre
{
  "name": "Théâtre",
  "icon": "🎭"
}

### Marché
{
  "name": "Marché",
  "icon": "🛒"
}

### Networking
{
  "name": "Networking",
  "icon": "🤝"
}

---

## Instructions :

1. Allez sur Firebase Console : https://console.firebase.google.com
2. Sélectionnez votre projet
3. Allez dans Firestore Database
4. Créez la collection "cities"
5. Ajoutez les documents avec les données JSON ci-dessus (laissez l'ID auto-généré)
6. Créez la collection "eventTypes"
7. Ajoutez les documents avec les données JSON ci-dessus (laissez l'ID auto-généré)

Ou utilisez le script Node.js/Python avec Firebase Admin SDK pour automatiser.
