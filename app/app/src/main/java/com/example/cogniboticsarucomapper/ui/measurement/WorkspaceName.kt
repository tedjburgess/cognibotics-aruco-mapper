package com.example.cogniboticsarucomapper.ui.measurement

// The API requires a non-empty cell name and the database column is 255 characters.
internal const val MAX_WORKSPACE_NAME_LENGTH = 255

/** Return the normalized name for a future scan request. */
internal fun normalizeWorkspaceName(value: String): String? {
    val name = value.trim()
    return name.takeIf { it.isNotEmpty() && it.length <= MAX_WORKSPACE_NAME_LENGTH }
}
