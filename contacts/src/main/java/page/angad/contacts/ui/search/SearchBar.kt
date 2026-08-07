package page.angad.contacts.ui.search

import androidx.compose.animation.core.snap
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExpandedFullScreenContainedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberContainedSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.vicart.compose.material.symbols.MaterialSymbols
import dev.vicart.compose.material.symbols.OutlinedRoundedSymbol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import page.angad.contacts.ui.main.ContactListIntent
import page.angad.contacts.ui.main.ContactListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchLeading(scope: CoroutineScope, state: SearchBarState) {
    when (state.currentValue) {
        SearchBarValue.Expanded -> {
            IconButton(onClick = { scope.launch { state.animateToCollapsed() } }) {
                OutlinedRoundedSymbol(
                    MaterialSymbols.CHEVRON_LEFT,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        SearchBarValue.Collapsed -> {
            OutlinedRoundedSymbol(
                MaterialSymbols.SEARCH,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTrailing(barState: SearchBarState) {
    when (barState.currentValue) {
        SearchBarValue.Expanded -> {}

        SearchBarValue.Collapsed -> {
            IconButton(onClick = { /* menu side panel thingy */ }) {
                OutlinedRoundedSymbol(
                    MaterialSymbols.MENU,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(state: ContactListState = ContactListState.current) {
    val (_, _, _, intent) = state

    val inputState = rememberTextFieldState()
    val barState = when (intent) {
        is ContactListIntent.Ui -> rememberContainedSearchBarState()
        else -> rememberContainedSearchBarState(
            animationSpecForExpand = snap(),
            animationSpecForCollapse = snap()
        )
    }
    val scope = rememberCoroutineScope()

    val inputField = @Composable {
        SearchBarDefaults.InputField(
            leadingIcon = { SearchLeading(scope, barState) },
            trailingIcon = { SearchTrailing(barState) },
            searchBarState = barState,
            textFieldState = inputState,
            onSearch = { scope.launch { barState.animateToExpanded() } },
            placeholder = { Text("Search contacts") },
            colors = SearchBarDefaults.containedColors(barState).inputFieldColors,
        )
    }

    LaunchedEffect(barState.currentValue) {
        if (barState.currentValue != SearchBarValue.Collapsed) return@LaunchedEffect
        inputState.clearText()
    }

    Column {
        if (intent is ContactListIntent.Pick) {
            CenterAlignedTopAppBar(
                title = { Text("Select a Contact") },
                navigationIcon = {
                    IconButton({ intent.cancel() }) {
                        OutlinedRoundedSymbol(MaterialSymbols.ARROW_BACK)
                    }
                },
                actions = {
                    IconButton({ scope.launch { barState.animateToExpanded() } }) {
                        OutlinedRoundedSymbol(MaterialSymbols.SEARCH)
                    }
                }
            )
        } else {
            AppBarWithSearch(
                state = barState,
                inputField = inputField,
                colors = SearchBarDefaults.appBarWithSearchColors(
                    appBarContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        ExpandedFullScreenContainedSearchBar(
            state = barState,
            inputField = inputField,
            colors = SearchBarDefaults.containedColors(barState)
                .copy(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            SearchResults(inputState)
        }
    }
}