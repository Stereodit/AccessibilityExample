package com.accessibilityexample.ui.form

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.accessibilityexample.R
import com.accessibilityexample.ui.components.DemoTopBar
import com.accessibilityexample.ui.components.SectionHeading
import com.accessibilityexample.ui.theme.AccessibilityExampleTheme

private enum class ContactMethod {
    Call,
    Email,
    Push
}

private val contactMethods = listOf(
    ContactMethod.Call to R.string.method_call,
    ContactMethod.Email to R.string.method_email,
    ContactMethod.Push to R.string.method_push
)

private val EmailPattern = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

private fun isValidEmail(value: String): Boolean = EmailPattern.matches(value.trim())

@Composable
fun AppointmentFormScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val paneTitleText = stringResource(R.string.form_pane)
    val scrollState = rememberScrollState()
    val nameFocus = FocusRequester()
    val emailFocus = FocusRequester()

    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var reminders by rememberSaveable { mutableStateOf(true) }
    var remindHours by rememberSaveable { mutableFloatStateOf(2f) }
    var contactMethod by rememberSaveable { mutableStateOf(ContactMethod.Call) }
    var consent by rememberSaveable { mutableStateOf(false) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var announcement by rememberSaveable { mutableStateOf("") }
    var announcementIsError by rememberSaveable { mutableStateOf(false) }

    BackHandler(onBack = onBack)

    val hours = remindHours.toInt()
    val hoursValue = pluralStringResource(R.plurals.hours_value, hours, hours)
    val hoursText = if (reminders) {
        stringResource(R.string.remind_hours, hoursValue)
    } else {
        stringResource(R.string.reminders_off)
    }
    val sliderName = stringResource(R.string.slider_name)
    val sliderState = if (reminders) hoursValue else stringResource(R.string.slider_off)

    val completedSteps = listOf(name.isNotBlank(), isValidEmail(email), consent).count { it }
    val totalSteps = 3
    val progressFraction = completedSteps / totalSteps.toFloat()
    val progressValue = stringResource(R.string.progress_value, completedSteps, totalSteps)
    val progressName = stringResource(R.string.progress_name)

    val nameError = if (submitted && name.isBlank()) {
        stringResource(R.string.name_error)
    } else {
        null
    }
    val emailError = if (submitted && !isValidEmail(email)) {
        stringResource(R.string.email_error)
    } else {
        null
    }

    fun trySubmit() {
        if (!consent) {
            announcement = context.getString(R.string.consent_required)
            announcementIsError = true
            return
        }
        submitted = true
        when {
            name.isBlank() -> {
                announcement = context.getString(
                    R.string.error_prefixed,
                    context.getString(R.string.name_error)
                )
                announcementIsError = true
                nameFocus.requestFocus()
            }

            !isValidEmail(email) -> {
                announcement = context.getString(
                    R.string.error_prefixed,
                    context.getString(R.string.email_error)
                )
                announcementIsError = true
                emailFocus.requestFocus()
            }

            else -> {
                announcement = context.getString(R.string.submit_success, name.trim())
                announcementIsError = false
                submitted = false
            }
        }
    }

    fun clearForm() {
        name = ""
        email = ""
        reminders = true
        remindHours = 2f
        contactMethod = ContactMethod.Call
        consent = false
        submitted = false
        announcement = context.getString(R.string.form_cleared)
        announcementIsError = false
    }

    LaunchedEffect(announcement, announcementIsError) {
        if (announcement.isNotEmpty() && !announcementIsError) {
            withFrameNanos { }
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .semantics { paneTitle = paneTitleText },
        topBar = {
            DemoTopBar(
                title = stringResource(R.string.form_title),
                onBack = onBack,
                backDescription = stringResource(R.string.back_to_catalog)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .imePadding()
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.form_intro),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .clearAndSetSemantics {
                        contentDescription = progressName
                        stateDescription = progressValue
                        progressBarRangeInfo = ProgressBarRangeInfo(
                            current = progressFraction,
                            range = 0f..1f
                        )
                    }
            ) {
                Text(
                    text = progressValue,
                    style = MaterialTheme.typography.bodyLarge
                )
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }

            SectionHeading(text = stringResource(R.string.section_contacts))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .focusRequester(nameFocus),
                label = { Text(stringResource(R.string.name_label)) },
                isError = nameError != null,
                supportingText = if (nameError != null) {
                    { Text(nameError) }
                } else {
                    null
                },
                trailingIcon = if (nameError != null) {
                    { FieldErrorIcon() }
                } else {
                    null
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { emailFocus.requestFocus() }
                )
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(emailFocus),
                label = { Text(stringResource(R.string.email_label)) },
                isError = emailError != null,
                supportingText = if (emailError != null) {
                    { Text(emailError) }
                } else {
                    null
                },
                trailingIcon = if (emailError != null) {
                    { FieldErrorIcon() }
                } else {
                    null
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { trySubmit() })
            )

            SectionHeading(text = stringResource(R.string.section_reminder))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .toggleable(
                        value = reminders,
                        role = Role.Switch,
                        onValueChange = { reminders = it }
                    )
                    .semantics(mergeDescendants = true) {}
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.reminders_label),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = stringResource(R.string.reminders_supporting),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = reminders,
                    onCheckedChange = null
                )
            }
            Text(
                text = hoursText,
                style = MaterialTheme.typography.bodyLarge,
                // Значение озвучивает слайдер ниже, текст на экране его не повторяет.
                modifier = Modifier
                    .padding(top = 8.dp)
                    .semantics { hideFromAccessibility() }
            )
            Slider(
                value = remindHours,
                onValueChange = { remindHours = it },
                valueRange = 1f..24f,
                steps = 22,
                enabled = reminders,
                modifier = Modifier.semantics {
                    contentDescription = sliderName
                    stateDescription = sliderState
                }
            )

            SectionHeading(text = stringResource(R.string.section_contact_method))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup()
            ) {
                contactMethods.forEach { (method, labelRes) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .selectable(
                                selected = contactMethod == method,
                                role = Role.RadioButton,
                                onClick = { contactMethod = method }
                            )
                            .semantics(mergeDescendants = true) {},
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = contactMethod == method,
                            onClick = null
                        )
                        Text(
                            text = stringResource(labelRes),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }
            }

            SectionHeading(text = stringResource(R.string.section_summary))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .semantics { isTraversalGroup = true },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.price),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.semantics { traversalIndex = 1f }
                )
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.semantics { traversalIndex = 0f }
                ) {
                    Text(
                        text = stringResource(R.string.service_name),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.service_duration),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Text(
                text = stringResource(R.string.traversal_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .heightIn(min = 48.dp)
                    .toggleable(
                        value = consent,
                        role = Role.Checkbox,
                        onValueChange = { consent = it }
                    )
                    .semantics(mergeDescendants = true) {},
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = consent,
                    onCheckedChange = null
                )
                Text(
                    text = stringResource(R.string.consent),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
            if (announcement.isNotEmpty()) {
                AnnouncementBanner(
                    text = announcement,
                    isError = announcementIsError,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
            Text(
                text = stringResource(R.string.consent_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            Button(
                onClick = ::trySubmit,
                enabled = consent,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .heightIn(min = 48.dp)
            ) {
                Text(text = stringResource(R.string.submit))
            }
            TextButton(
                onClick = ::clearForm,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Text(text = stringResource(R.string.clear_form))
            }
        }
    }
}

@Composable
private fun AnnouncementBanner(
    text: String,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    val container = if (isError) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val content = if (isError) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                liveRegion = if (isError) LiveRegionMode.Assertive else LiveRegionMode.Polite
            },
        shape = MaterialTheme.shapes.medium,
        color = container,
        contentColor = content
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(
                    if (isError) R.drawable.ic_error else R.drawable.ic_check
                ),
                contentDescription = null,
                tint = content
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = content,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

@Composable
private fun FieldErrorIcon() {
    Icon(
        painter = painterResource(R.drawable.ic_error),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.error
    )
}

@Preview(showBackground = true, name = "Форма")
@Composable
private fun FormPreview() {
    AccessibilityExampleTheme {
        AppointmentFormScreen(onBack = {})
    }
}

@Preview(
    showBackground = true,
    name = "Форма, крупный шрифт",
    fontScale = 1.8f
)
@Composable
private fun FormLargeFontPreview() {
    AccessibilityExampleTheme {
        AppointmentFormScreen(onBack = {})
    }
}

@Preview(
    showBackground = true,
    name = "Форма, тёмная тема",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun FormDarkPreview() {
    AccessibilityExampleTheme {
        AppointmentFormScreen(onBack = {})
    }
}
