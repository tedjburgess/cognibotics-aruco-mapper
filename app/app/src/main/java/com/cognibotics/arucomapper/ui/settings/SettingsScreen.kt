package com.cognibotics.arucomapper.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cognibotics.arucomapper.network.ApiClient
import com.cognibotics.arucomapper.ui.components.FeatureLayout
import com.cognibotics.arucomapper.ui.components.InfoPanel
import java.net.URI

@Composable
fun SettingsScreen(
    apiBaseUrl: String,
    defaultApiBaseUrl: String,
    onSave: (String) -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var draft by rememberSaveable(apiBaseUrl) { mutableStateOf(apiBaseUrl) }
    val trimmed = draft.trim()
    val isValid = isValidBaseUrl(trimmed)
    val differs = ApiClient.normalize(trimmed) != ApiClient.normalize(apiBaseUrl)
    val usesDefault = ApiClient.normalize(apiBaseUrl) == ApiClient.normalize(defaultApiBaseUrl)

    FeatureLayout(
        "Settings",
        "Choose the API server that measurements are saved to",
        onBack,
        modifier
    ) {
        InfoPanel(
            "API server",
            "Sites, measurements and history are read from this address."
        )
        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it },
            label = { Text("API base URL") },
            singleLine = true,
            isError = trimmed.isNotEmpty() && !isValid,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            if (isValid || trimmed.isEmpty()) {
                "Default: $defaultApiBaseUrl"
            } else {
                "Use an address that starts with http:// or https://"
            },
            style = MaterialTheme.typography.bodySmall
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { onSave(ApiClient.normalize(trimmed)) },
                enabled = isValid && differs
            ) {
                Text("Save")
            }
            OutlinedButton(
                onClick = onReset,
                enabled = !usesDefault
            ) {
                Text("Reset to default")
            }
        }
    }
}

private fun isValidBaseUrl(value: String): Boolean = runCatching {
    val uri = URI(value)
    (uri.scheme == "http" || uri.scheme == "https") && !uri.host.isNullOrBlank()
}.getOrDefault(false)
