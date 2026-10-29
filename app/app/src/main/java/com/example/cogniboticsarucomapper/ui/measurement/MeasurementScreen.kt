package com.example.cogniboticsarucomapper.ui.measurement

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.cogniboticsarucomapper.network.ApiClient
import com.example.cogniboticsarucomapper.network.model.SiteWithCellsResponse
import com.example.cogniboticsarucomapper.ui.components.ActionCard
import com.example.cogniboticsarucomapper.ui.components.FeatureLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val DEFAULT_SITE = "Test Site"
private const val DEFAULT_CELL = "Test Cell"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasurementScreen(
    onStartMeasurement: (String, String) -> Unit,
    onHistoryClick: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var sites by remember { mutableStateOf<List<SiteWithCellsResponse>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var reload by remember { mutableStateOf(0) }
    var siteName by remember { mutableStateOf(DEFAULT_SITE) }
    var cellName by remember { mutableStateOf(DEFAULT_CELL) }
    var siteMenuExpanded by remember { mutableStateOf(false) }
    var cellMenuExpanded by remember { mutableStateOf(false) }
    var showAllSites by remember { mutableStateOf(true) }
    var showAllCells by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var csvData by remember {
        mutableStateOf<ByteArray?>(null)
    }

    var downloadingCsv by remember {
        mutableStateOf(false)
    }

    var downloadError by remember {
        mutableStateOf<String?>(null)
    }

    val saveCsvLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("text/csv")
        ) { uri ->
            if (uri != null) {
                val data = csvData

                if (data != null) {
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            context.contentResolver
                                .openOutputStream(uri)
                                ?.use { output ->
                                    output.write(data)
                                }
                        }

                        csvData = null
                    }
                }
            }
        }

    LaunchedEffect(reload) {
        loading = true
        error = null
        try {
            sites = withContext(Dispatchers.IO) { ApiClient.getService().getSites() }
        } catch (e: Exception) {
            error = e.message ?: "Could not connect to the API"
        } finally {
            loading = false
        }
    }

    val siteOptions = (listOf(DEFAULT_SITE) + sites.map { it.name })
        .distinctBy { it.lowercase() }
    val siteSuggestions = if (showAllSites) {
        siteOptions
    } else {
        siteOptions.filter { it.contains(siteName, ignoreCase = true) }
    }
    val currentSite = sites.firstOrNull { it.name == siteName.trim() }
    val cellOptions = (listOf(DEFAULT_CELL) + currentSite?.cells.orEmpty().map { it.name })
        .distinctBy { it.lowercase() }
    val cellSuggestions = if (showAllCells) {
        cellOptions
    } else {
        cellOptions.filter { it.contains(cellName, ignoreCase = true) }
    }
    val canStart = siteName.trim().isNotEmpty() && cellName.trim().isNotEmpty()

    FeatureLayout("Measurement", "Scan markers or review previous measurements", onBack, modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("New scan", style = MaterialTheme.typography.titleMedium)

            ExposedDropdownMenuBox(
                expanded = siteMenuExpanded && siteSuggestions.isNotEmpty(),
                onExpandedChange = {
                    siteMenuExpanded = it
                    if (it) showAllSites = true
                }
            ) {
                OutlinedTextField(
                    value = siteName,
                    onValueChange = { value ->
                        siteName = value.take(MAX_SITE_CELL_NAME_LENGTH)
                        showAllSites = false
                        siteMenuExpanded = true
                    },
                    modifier = Modifier.fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable),
                    label = { Text("Site") },
                    singleLine = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(siteMenuExpanded)
                    }
                )
                ExposedDropdownMenu(
                    expanded = siteMenuExpanded && siteSuggestions.isNotEmpty(),
                    onDismissRequest = { siteMenuExpanded = false }
                ) {
                    siteSuggestions.forEach { site ->
                        DropdownMenuItem(
                            text = { Text(site) },
                            onClick = {
                                siteName = site
                                if (sites.none { it.name == site } || currentSite?.name != site) {
                                    cellName = DEFAULT_CELL
                                }
                                siteMenuExpanded = false
                                showAllCells = true
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = cellMenuExpanded && cellSuggestions.isNotEmpty(),
                onExpandedChange = {
                    cellMenuExpanded = it
                    if (it) showAllCells = true
                }
            ) {
                OutlinedTextField(
                    value = cellName,
                    onValueChange = { value ->
                        cellName = value.take(MAX_SITE_CELL_NAME_LENGTH)
                        showAllCells = false
                        cellMenuExpanded = true
                    },
                    modifier = Modifier.fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable),
                    label = { Text("Cell") },
                    singleLine = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(cellMenuExpanded)
                    }
                )
                ExposedDropdownMenu(
                    expanded = cellMenuExpanded && cellSuggestions.isNotEmpty(),
                    onDismissRequest = { cellMenuExpanded = false }
                ) {
                    cellSuggestions.forEach { cell ->
                        DropdownMenuItem(
                            text = { Text(cell) },
                            onClick = {
                                cellName = cell
                                cellMenuExpanded = false
                            }
                        )
                    }
                }
            }

            if (loading) Text("Loading saved sites", style = MaterialTheme.typography.bodySmall)
            if (error != null) {
                Text(
                    "API unavailable: $error",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                Button(onClick = { reload++ }) { Text("Retry") }
            }

            Button(
                onClick = { onStartMeasurement(siteName.trim(), cellName.trim()) },
                enabled = canStart,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start scan")
            }

            Button(
                onClick = {
                    scope.launch {
                        downloadingCsv = true
                        downloadError = null

                        try {
                            val response = withContext(Dispatchers.IO) {
                                ApiClient.getService().downloadScansCsv(
                                    siteName = siteName.trim(),
                                    cellName = cellName.trim()
                                )
                            }

                            if (response.isSuccessful) {
                                val body = response.body()
                                    ?: throw IllegalStateException("Empty CSV response")

                                csvData = withContext(Dispatchers.IO) {
                                    body.bytes()
                                }

                                val fileName =
                                    "${siteName.trim()}-${cellName.trim()}-measurements.csv"
                                        .replace(" ", "_")

                                saveCsvLauncher.launch(fileName)
                            } else {
                                downloadError =
                                    "Could not download measurements (${response.code()})"
                            }
                        } catch (e: Exception) {
                            downloadError =
                                e.message ?: "Could not download measurements"
                        } finally {
                            downloadingCsv = false
                        }
                    }
                },
                enabled = !downloadingCsv &&
                    siteName.isNotBlank() &&
                    cellName.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (downloadingCsv) {
                        "Preparing CSV..."
                    } else {
                        "Download CSV"
                    }
                )
            }
        }

        if (downloadError != null) {
            Text(
                text = downloadError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        ActionCard(
            Icons.Filled.DateRange,
            "Measurement history",
            "Review measurement history",
            onHistoryClick
        )
    }
}
