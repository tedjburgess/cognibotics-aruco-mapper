package com.example.cogniboticsarucomapper.ui.measurement

// The API requires a non-empty cell_name and the database column is 255 characters.
internal const val MAX_WORKSPACE_NAME_LENGTH = 255

/** Return the normalized name to be used later in ScanRequest.cell_name. */
internal fun normalizeWorkspaceName(value: String): String? {
    val name = value.trim()
    return name.takeIf { it.isNotEmpty() && it.length <= MAX_WORKSPACE_NAME_LENGTH }
}

// A site has the same 255-character database limit and cannot be blank.
internal fun normalizeSiteName(value: String): String? = normalizeWorkspaceName(value)
