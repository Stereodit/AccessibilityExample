package com.accessibilityexample.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.accessibilityexample.ui.catalog.CatalogScreen
import com.accessibilityexample.ui.form.AppointmentFormScreen

private enum class DemoDestination {
    Catalog,
    Form
}

@Composable
fun AccessibilityApp() {
    var destination by rememberSaveable { mutableStateOf(DemoDestination.Catalog) }

    when (destination) {
        DemoDestination.Catalog -> CatalogScreen(
            onOpenForm = { destination = DemoDestination.Form }
        )

        DemoDestination.Form -> AppointmentFormScreen(
            onBack = { destination = DemoDestination.Catalog }
        )
    }
}
