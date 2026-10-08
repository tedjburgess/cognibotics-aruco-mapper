package com.example.cogniboticsarucomapper.ui.export

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.cogniboticsarucomapper.ui.components.FeatureLayout
import com.example.cogniboticsarucomapper.ui.components.InfoPanel

@Composable
fun ExportScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    FeatureLayout("Export", "Measurement export workspace", onBack, modifier) {
        InfoPanel("Export tools", "Export controls will appear here when available.")
    }
}
