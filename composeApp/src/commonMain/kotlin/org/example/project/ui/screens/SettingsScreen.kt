package org.example.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.example.project.core.Constants
import org.example.project.data.update.AppVersion
import org.example.project.data.update.NetworkType
import org.example.project.data.update.UpdateChecker
import org.example.project.data.update.UpdateInstaller
import org.example.project.data.update.UpdateResult
import org.example.project.data.update.detectNetworkType
import org.example.project.domain.ServerConfig
import io.ktor.client.HttpClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    config: ServerConfig,
    onConfigChanged: (ServerConfig) -> Unit,
    onTestConnection: () -> Unit,
    testResult: String? = null,
    modifier: Modifier = Modifier,
    updateChecker: UpdateChecker? = null,
    updateInstaller: UpdateInstaller? = null
) {
    var serverUrl by remember(config) { mutableStateOf(config.serverUrl) }
    var apiEndpoint by remember(config) { mutableStateOf(config.apiEndpoint) }
    var modelName by remember(config) { mutableStateOf(config.modelName) }
    var apiKey by remember(config) { mutableStateOf(config.apiKey) }
    var updateStatus by remember { mutableStateOf<UpdateResult?>(null) }
    var isChecking by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var isDownloading by remember { mutableStateOf(false) }
    var installResult by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Paramètres", fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Connexion au Serveur LLM",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Configurez l'adresse de votre serveur IA local (Ollama, LM Studio, etc.)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedTextField(
                value = serverUrl,
                onValueChange = { serverUrl = it },
                label = { Text("URL du serveur") },
                placeholder = { Text("http://localhost:11434") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = apiEndpoint,
                onValueChange = { apiEndpoint = it },
                label = { Text("Chemin de l'API") },
                placeholder = { Text("/v1/chat/completions") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = modelName,
                onValueChange = { modelName = it },
                label = { Text("Nom du modèle") },
                placeholder = { Text("llama3") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("Clé API (optionnelle)") },
                placeholder = { Text("sk-...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (apiKey.isNotEmpty()) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = {
                    val newConfig = ServerConfig(
                        serverUrl = serverUrl,
                        apiEndpoint = apiEndpoint,
                        modelName = modelName,
                        apiKey = apiKey
                    )
                    onConfigChanged(newConfig)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Enregistrer")
            }

            OutlinedButton(
                onClick = onTestConnection,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Tester la connexion")
            }

            if (testResult != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (testResult.startsWith("OK"))
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = testResult,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Mise à jour",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Version actuelle : ${AppVersion.CURRENT}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))

                    if (updateChecker == null) {
                        Text(
                            text = "Non disponible (client HTTP requis)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        if (isDownloading) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                LinearProgressIndicator(
                                    progress = { downloadProgress },
                                    modifier = Modifier.fillMaxWidth().height(8.dp),
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Téléchargement... ${(downloadProgress * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        } else if (installResult != null) {
                            val resultText: String = installResult ?: ""
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (resultText == "Installation réussie ! Redémarrez l'application.")
                                        MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Text(
                                    text = resultText,
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        } else {
                            val networkType = remember { detectNetworkType() }
                            val networkLabel = when (networkType) {
                                NetworkType.WIFI -> "WiFi détecté"
                                NetworkType.MOBILE -> "Réseau mobile détecté (zero-rating)"
                                NetworkType.UNKNOWN -> "Réseau inconnu"
                            }

                            Text(
                                text = networkLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    scope.launch {
                                        isChecking = true
                                        updateStatus = null
                                        val result = updateChecker.checkForUpdate()
                                        updateStatus = result
                                        isChecking = false
                                    }
                                },
                                enabled = !isChecking,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isChecking) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(Modifier.width(8.dp))
                                }
                                Text("Vérifier les mises à jour")
                            }

                            when (val status = updateStatus) {
                                is UpdateResult.UpToDate -> {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    ) {
                                        Text(
                                            text = "✓ Vous avez la dernière version",
                                            modifier = Modifier.padding(12.dp),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                                is UpdateResult.UpdateAvailable -> {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = "Nouvelle version : ${status.release.version}",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            if (status.release.releaseNotes.isNotBlank()) {
                                                Spacer(Modifier.height(4.dp))
                                                Text(
                                                    text = status.release.releaseNotes.take(200),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    maxLines = 4
                                                )
                                            }
                                            Spacer(Modifier.height(8.dp))
                                            Text(
                                                text = "Taille : ${status.release.fileSize / 1024 / 1024} Mo",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Spacer(Modifier.height(8.dp))
                                            Button(
                                                onClick = {
                                                    scope.launch {
                                                        isDownloading = true
                                                        installResult = null
                                                        try {
                                                            val bytes = updateInstaller?.downloadUpdate(
                                                                url = status.release.downloadUrl,
                                                                onProgress = { p -> downloadProgress = p }
                                                            )
                                                            if (bytes != null) {
                                                                val ok = updateInstaller.install(bytes, status.release.fileName)
                                                                installResult = if (ok)
                                                                    "Installation réussie ! Redémarrez l'application."
                                                                else
                                                                    "Échec de l'installation. Téléchargez manuellement depuis GitHub."
                                                            } else {
                                                                installResult = "Échec du téléchargement."
                                                            }
                                                        } catch (e: Exception) {
                                                            installResult = "Erreur : ${e.message}"
                                                        }
                                                        isDownloading = false
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text("Télécharger et installer")
                                            }
                                        }
                                    }
                                }
                                is UpdateResult.NoAsset -> {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer
                                        )
                                    ) {
                                        Text(
                                            text = "Aucun fichier compatible trouvé pour cette plateforme",
                                            modifier = Modifier.padding(12.dp),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                                is UpdateResult.Error -> {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer
                                        )
                                    ) {
                                        Text(
                                            text = "⚠ ${status.message}",
                                            modifier = Modifier.padding(12.dp),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                                null -> {}
                            }
                        }
                    }
                }
            }
        }
    }
}
