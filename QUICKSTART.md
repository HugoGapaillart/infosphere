## 🎯 Guide de démarrage rapide

### Étape 1 : Configuration Firebase (10 min)

1. **Créer un projet Firebase**
   - Allez sur https://console.firebase.google.com
   - Cliquez sur "Ajouter un projet"
   - Nommez-le "Infosphere" ou selon votre préférence
   - Désactivez Google Analytics (optionnel)

2. **Ajouter l'application Android**
   - Dans votre projet Firebase, cliquez sur l'icône Android
   - Package name: `com.infosphere`
   - Téléchargez le fichier `google-services.json`
   - Remplacez `app/google-services.json` avec votre fichier

3. **Activer Authentication**
   - Dans Firebase Console, allez dans Authentication
   - Cliquez sur "Get started"
   - Activez "Email/Password"

4. **Activer Firestore**
   - Allez dans Firestore Database
   - Cliquez sur "Create database"
   - Choisissez "Start in production mode"
   - Sélectionnez votre région
   - Copiez les règles de sécurité depuis README.md

5. **Activer Storage**
   - Allez dans Storage
   - Cliquez sur "Get started"
   - Choisissez "Start in production mode"
   - Copiez les règles de sécurité depuis README.md

6. **Initialiser les données**
   - Suivez les instructions dans `FIREBASE_INIT.md`
   - Ajoutez au moins 3-4 villes
   - Ajoutez au moins 5-6 types d'événements

### Étape 2 : Compiler et lancer l'application

1. **Synchroniser Gradle**
   ```bash
   ./gradlew build
   ```

2. **Lancer l'application**
   - Connectez un appareil Android ou lancez un émulateur
   - Cliquez sur Run dans Android Studio
   - Ou utilisez : `./gradlew installDebug`

### Étape 3 : Tester l'application

1. **Créer un compte**
   - Ouvrez l'application
   - Cliquez sur "Inscription"
   - Entrez un nom, email et mot de passe
   - Cliquez sur "S'inscrire"

2. **Sélectionner des villes**
   - Allez dans l'onglet "Profil"
   - Cliquez sur "Modifier mes villes"
   - Sélectionnez 2-3 villes
   - Cliquez sur "Enregistrer"

3. **Créer un événement**
   - Allez dans l'onglet "Ajouter"
   - Remplissez tous les champs
   - Sélectionnez une date future
   - Choisissez une ville
   - Sélectionnez des types d'événement
   - (Optionnel) Ajoutez des photos
   - Cliquez sur "Créer l'événement"

4. **Voir les événements**
   - Allez dans l'onglet "Accueil"
   - Vous devriez voir votre événement
   - Tirez vers le bas pour rafraîchir

5. **Rechercher des événements**
   - Allez dans l'onglet "Rechercher"
   - Sélectionnez une ville
   - (Optionnel) Sélectionnez des types
   - Cliquez sur "Rechercher"

### Étape 4 : Problèmes courants

**Erreur "google-services.json not found"**
- Vérifiez que le fichier est dans `app/google-services.json`
- Synchronisez Gradle

**Aucun événement affiché**
- Vérifiez que vous avez sélectionné des villes dans votre profil
- Vérifiez que les événements ont une date future
- Vérifiez les règles Firestore

**Impossible de créer un événement**
- Vérifiez que vous êtes connecté
- Vérifiez les règles Firestore
- Vérifiez les logs Android

**Photos non chargées**
- Vérifiez les règles Storage
- Vérifiez votre connexion Internet
- Vérifiez les permissions dans AndroidManifest.xml

### Étape 5 : Logs et debugging

Pour voir les logs Firebase :
```bash
adb logcat | grep -i firebase
```

Pour voir tous les logs de l'application :
```bash
adb logcat | grep -i infosphere
```

### 🎉 C'est prêt !

Votre application Infosphere est maintenant opérationnelle ! Vous pouvez :
- Créer des événements
- Rechercher des événements par ville et type
- Gérer votre profil
- Voir vos événements créés

Pour aller plus loin :
- Personnalisez les couleurs dans `values/colors.xml`
- Ajoutez plus de villes dans Firestore
- Ajoutez plus de types d'événements
- Implémentez une page de détails d'événement
- Ajoutez des notifications push
- Implémentez la géolocalisation
