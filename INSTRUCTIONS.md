# DOCUMENT DE SPÉCIFICATIONS TECHNIQUES (GUIDANCE DE CODAGE IA)
## PROJET : tutoria'iad (Application Hôte Socratique Multimodale)
## TECH STACK : Kotlin Multiplatform (KMP), Compose Multiplatform, Ktor Client, SQLDelight/Room KMP
## INFRASTRUCTURE IA : Serveur local distinct (ex: techjarves/mobile-server ou cross-platform-llm-client) via localhost API OpenAI-compatible

Tu es un agent de codage IA de niveau expert. Ton objectif est d'implémenter l'application "tutoria'iad", un écosystème éducatif multimodal (écrit, audio, vocal, visuel) utilisant Kotlin Multiplatform et Compose Multiplatform pour cibler Android, iOS et Desktop. L'application fonctionne à 100% en local pour l’apprenant en se connectant à un serveur IA distinct qui tourne en tâche de fond sur l'appareil, avec une réplication Cloud asynchrone en mode "Zero-Rating" pour les interfaces de monitoring Web (Parents/Enseignants).

---

## 1. ARCHITECTURE TECHNIQUE GLOBALE

L'écosystème élimine l'inférence directe et délègue la génération à un serveur tiers local :
1. INFRASTRUCTURE IA (EXTERNE) : Serveur LLM/SLM tiers fonctionnant de manière isolée sur l'appareil (via architecture OpenAI-compatible sur localhost:11434 ou localhost/v1/chat/completions).
2. shared (Core KMP Module) : Contient le moteur de RAG multimodal local (gestion des métadonnées PDF/Images), le client HTTP Ktor paramrétré pour interroger le serveur local ET pour la synchronisation distante Zero-Rating, et la base de données relationnelle locale.
3. composeApp (UI partagée) : Interface utilisateur hautement interactive et multimodale conçue avec Compose Multiplatform (Android, iOS, Desktop).

---

## 2. ARBRE DES FICHIERS ET STRUCTURE DU PROJET KMP

Réalise l'implémentation selon l'architecture standard KMP :

tutoria_iad/
├── shared/                           # Code métier partagé (100% logique commune)
│   ├── src/
│   │   ├── commonMain/kotlin/
│   │   │   ├── core/                 # Prompts Socratiques, Constantes, DI (Koin)
│   │   │   ├── data/
│   │   │   │   ├── cache/            # Base de données locale (SQLDelight/Room KMP) - Modèles miroirs PostgreSQL
│   │   │   │   ├── rag/              # Moteur RAG Multimodal Local (Chunking, OCR local, Parsing)
│   │   │   │   └── network/          # Client Ktor (Double configuration : Localhost LLM & Cloud Distant Zero-Rating)
│   │   │   ├── domain/               # Modèles partagés (Student, Quiz, Metadatas, ExtendedChunks)
│   │   │   └── presentation/         # ViewModels partagés & Logique d'interception Socratique
│   │   ├── androidMain/              # Spécificités Android (Speech-to-Text & Text-to-Speech natifs)
│   │   ├── iosMain/                  # Spécificités iOS (TTS/STT Apple)
│   │   └── desktopMain/              # Spécificités Desktop (TTS/STT Windows/JVM)
├── composeApp/                       # UI partagée avec Compose Multiplatform
│   ├── src/commonMain/kotlin/
│   │   ├── screens/                  # ChatScreen, MonitoringScreen, RAGLibraryManager
│   │   └── components/               # MCP UI Widgets (QuizView, FlashcardView, DocumentViewerPanel)
└── docs/                             # Prompts Systèmes et Spécifications

---

## 3. SPÉCIFICATIONS DES COMPOSANTS ET FLUX DE DONNÉES

### COMPOSANT A : Client LLM Localhost & Prompt Socratique
L'application ne compile pas de modèle de langage. Elle utilise Ktor Client pour requêter l'API OpenAI-compatible du serveur local distinct (ex: `/v1/chat/completions`). Elle injecte à chaque ouverture de session le prompt système immuable :
```text
PROMPT_SYSTEME = """
Tu es le tuteur socratique d'élite de l'application "tutoria'iad". Ton rôle est de guider l'élève vers la résolution de ses devoirs par le questionnement exclusif (Maïeutique).
RÈGLES STRICTES :
1. Ne donne JAMAIS la réponse finale ou la solution d'un exercice.
2. Décompose chaque problème en sous-étapes logiques simples.
3. Formule des questions courtes, claires et adaptées au niveau de l'apprenant.
4. Base tes connaissances UNIQUEMENT sur le contexte extrait par le RAG local (fiches de cours burkinabè).
"""
```

### COMPOSANT B : Moteur RAG Local Étendu & Gestion des Métadonnées Visualisables
Le module RAG local (`shared/data/rag`) ne doit pas simplement extraire du texte brut, il doit permettre un ancrage visuel à l'écran pour l'étudiant :
*   **Parser & Chunking structurel :** Lors de l'indexation locale de documents (PDF, Images de cahiers via OCR local, fiches de révision), chaque fragment (chunk) doit inclure une classe `Metadata` Kotlin stricte :
    ```kotlin
    @Serializable
    data class ChunkMetadata(val sourceFile: String, val fileType: String, val pageNumber: Int? = null, val localUri: String? = null)
    ```
*   **Rappels Visuels Automatiques :** Si l'IA socratique extrait un contexte pour répondre, l'application doit intercepter les métadonnées de la source pour **ouvrir et afficher instantanément la page exacte du PDF d'origine, l'image du schéma lié ou le fichier associé** dans un volet scindé de l'interface Compose Multiplatform.

### COMPOSANT C : Le Moteur d'Interface Dynamique MCP UI
Lorsque le modèle local tiers renvoie du texte contenant des blocs de données JSON structurés, l'interface Compose Multiplatform intercepte le flux pour instancier des widgets graphiques interactifs :
```json
{
  "mcp_action": "display_widget",
  "widget_type": "quiz_option | flashcard | progress_tracker",
  "data": { ... }
}
```

### COMPOSANT D : Pipeline Multimodale Étendue (Entrées / Sorties)
*   **Entrées Apprenant (Interface de discussion locale) :**
    *   *Texte & Audio/Vocal :* Clavier classique et bouton d'enregistrement vocal (Speech-to-Text via l'API native du système configurée par expect/actual KMP).
    *   *Images, Photos & Fichiers :* Boutons d'importation de fichiers PDF ou de capture photo (exercices sur cahier, schémas de manuels). Les données textuelles extraites par OCR sont envoyées en contexte au SLM, et les fichiers d'origine sont enregistrés localement.
*   **Sorties de l'Application (Local outputs) :**
    *   *Texte & Audio :* Réponses textuelles socratiques couplées à une lecture vocale automatisée (Text-to-Speech natif via KMP expect/actual) pour les plus jeunes élèves.
    *   *Visuels & Rapports :* Affichage dynamique d'extraits de pages de fichiers, de fichiers images et génération de bilans de compétences au format texte/PDF léger.

### COMPOSANT E : Base SQLite Partagée & Synchro Asynchrone Zero-Rating
*   **Architecture de Persistance :**
    *   *Côté Client (KMP) :* SQLDelight ou Room KMP gère une base SQLite locale. Elle enregistre l'historique multimodal, le temps d'étude, les notes des devoirs et les progressions.
    *   *Côté Cloud Distant (Monitoring) :* Les modèles de données (classes de données Kotlin) sont structurés de manière identique à la base PostgreSQL du serveur central.
*   **Synchronisation Zero-Rating :** Le client Ktor encapsule et compresse les paquets de synchronisation. Les requêtes réseau ciblent les adresses IP/domaines validés auprès des opérateurs télécoms locaux pour le Zero-Rating (gratuit pour l'utilisateur final, facturé aux administrateurs). Dès qu'un accès réseau est détecté, la base locale réplique ses données vers le Cloud sans que la famille n'ait à payer de la data. Les parents et enseignants accèdent à la plateforme de monitoring web via les identifiants de l'apprenant.

---

## 5. STRATÉGIES D'OPTIMISATION DE LA MÉMOIRE VIVE (RAM) SUR MOBILE

Puisque l'application tutoria'iad agit en tant qu'application hôte et communique avec un serveur IA distinct (qui gère l'inférence lourde sur son propre processus système), l'optimisation de la RAM au niveau de l'interface Compose Multiplatform doit suivre les règles strictes suivantes :

1. **Isolation des Processus Système (Process Sandboxing) :**
   L'application hôte Kotlin et le serveur IA tiers (ex: mobile-server) s'exécutent dans deux environnements d'application distincts. Si le serveur IA sature la mémoire RAM en exécutant un SLM lourd, le système d'exploitation Android détruira le serveur en priorité sans faire crasher l'interface utilisateur de tutoria'iad.
2. **Libération Agressive de la RAM lors de l'Audio et de la Vision :**
   *   *Pour l'Audio (STT/TTS) :* Lors de l'utilisation de modules de dictée vocale (Speech-to-Text), les ressources d'enregistrement et de transcription ne doivent pas résider en mémoire. Utilisez des instances locales éphémères qui se détruisent automatiquement dès que le fichier audio est converti en chaîne de texte.
   *   *Pour l'Image (Visualiseur & RAG) :* L'affichage d'images de cours haute définition ou de pages entières de fichiers PDF sature rapidement la RAM des smartphones Infinix, Tecno ou Itel de 2 Go. Utilise la mise en cache de texture asynchrone (implémente la bibliothèque Coil ou Glide adaptée pour Compose Multiplatform) qui redimensionne dynamiquement les images à la taille exacte de l'écran avant de les charger en RAM.
3. **Limitation de la Fenêtre de Contexte Locale :**
   Configure le client Ktor pour forcer le paramètre de contexte (`num_ctx`) du serveur local à une valeur bloquée entre 1024 et 2048 tokens maximum. Cela empêche le cache KV (Key-Value) de saturer l'appareil lors des longs dialogues.
4. **Recyclage Graphique Strict (Lazy Components) :**
   L'affichage du chat multimodal doit obligatoirement utiliser des composants de type `LazyColumn` sous Compose Multiplatform pour recycler immédiatement la mémoire RAM des messages et images sortant de l'écran lors du défilement.

---

## 6. INSTRUCTIONS D'EXÉCUTION PAS À PAS POUR L'IA

1. **Étape 1 :** Implémente la base de données locale (SQLDelight/Room KMP) dans `shared/data/cache` avec les modèles partagés de suivi de compétences et les structures de métadonnées de fichiers.
