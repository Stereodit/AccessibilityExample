package com.accessibilityexample.ui.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.CollectionItemInfo
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.collectionItemInfo
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.accessibilityexample.R
import com.accessibilityexample.ui.components.DemoTopBar
import com.accessibilityexample.ui.components.SectionHeading
import com.accessibilityexample.ui.theme.AccessibilityExampleTheme

private data class Technique(
    val titleRes: Int,
    val bodyRes: Int
)

private val techniques = listOf(
    Technique(R.string.technique_headings_title, R.string.technique_headings_body),
    Technique(R.string.technique_descriptions_title, R.string.technique_descriptions_body),
    Technique(R.string.technique_grouping_title, R.string.technique_grouping_body),
    Technique(R.string.technique_collection_title, R.string.technique_collection_body),
    Technique(R.string.technique_actions_title, R.string.technique_actions_body)
)

private val StudiedSaver = listSaver<List<Boolean>, Int>(
    save = { value -> value.map { if (it) 1 else 0 } },
    restore = { saved ->
        List(techniques.size) { index -> saved.getOrNull(index) == 1 }
    }
)

@Composable
fun CatalogScreen(
    onOpenForm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val paneTitleText = stringResource(R.string.catalog_pane)
    var helpVisible by rememberSaveable { mutableStateOf(false) }
    var studied by rememberSaveable(stateSaver = StudiedSaver) {
        mutableStateOf(List(techniques.size) { false })
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .semantics { paneTitle = paneTitleText },
        topBar = {
            DemoTopBar(
                title = stringResource(R.string.catalog_title),
                actions = {
                    IconButton(onClick = { helpVisible = !helpVisible }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_info),
                            contentDescription = stringResource(
                                if (helpVisible) R.string.hide_hint else R.string.show_hint
                            )
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_decorative),
                // null — иллюстрация декоративная и не попадает в дерево доступности.
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 16.dp)
                    .size(72.dp)
            )
            Text(
                text = stringResource(R.string.catalog_intro),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 12.dp)
            )
            if (helpVisible) {
                Text(
                    text = stringResource(R.string.hint_body),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .semantics { liveRegion = LiveRegionMode.Polite }
                )
            }
            SectionHeading(text = stringResource(R.string.section_techniques))
            Column(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .semantics {
                        collectionInfo = CollectionInfo(
                            rowCount = techniques.size,
                            columnCount = 1
                        )
                    }
            ) {
                techniques.forEachIndexed { index, technique ->
                    TechniqueCard(
                        index = index,
                        title = stringResource(technique.titleRes),
                        body = stringResource(technique.bodyRes),
                        studied = studied[index],
                        onStudiedChange = { checked ->
                            studied = studied.mapIndexed { itemIndex, value ->
                                if (itemIndex == index) checked else value
                            }
                        }
                    )
                }
            }
            Button(
                onClick = onOpenForm,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 24.dp)
                    .heightIn(min = 48.dp)
            ) {
                Text(text = stringResource(R.string.open_form))
            }
        }
    }
}

@Composable
private fun TechniqueCard(
    index: Int,
    title: String,
    body: String,
    studied: Boolean,
    onStudiedChange: (Boolean) -> Unit
) {
    val actionLabel = stringResource(
        if (studied) R.string.unmark_studied else R.string.mark_studied
    )
    val statusLabel = stringResource(if (studied) R.string.studied else R.string.not_studied)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .heightIn(min = 48.dp)
            .toggleable(
                value = studied,
                role = Role.Checkbox,
                onValueChange = onStudiedChange
            )
            .semantics(mergeDescendants = true) {
                collectionItemInfo = CollectionItemInfo(
                    rowIndex = index,
                    rowSpan = 1,
                    columnIndex = 0,
                    columnSpan = 1
                )
                customActions = listOf(
                    CustomAccessibilityAction(actionLabel) {
                        onStudiedChange(!studied)
                        true
                    }
                )
            },
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (studied) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(start = 12.dp)
            ) {
                Text(
                    text = stringResource(R.string.item_position, index + 1, techniques.size),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.semantics { hideFromAccessibility() }
                )
                Icon(
                    painter = painterResource(
                        if (studied) R.drawable.ic_check else R.drawable.ic_circle
                    ),
                    contentDescription = null,
                    tint = if (studied) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .size(24.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Каталог")
@Composable
private fun CatalogPreview() {
    AccessibilityExampleTheme {
        CatalogScreen(onOpenForm = {})
    }
}
