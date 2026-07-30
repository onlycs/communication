package page.angad.contacts.ui.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AppBarWithSearch
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateMap
import dev.vicart.compose.material.symbols.MaterialSymbols
import dev.vicart.compose.material.symbols.OutlinedRoundedSymbol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import page.angad.contacts.ui.ContactsViewModel
import page.angad.libcontacts.Contact

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
fun SearchTrailing(barState: SearchBarState, inputState: TextFieldState) {
    when (barState.currentValue) {
        SearchBarValue.Expanded -> {
            IconButton(onClick = { inputState.clearText() }) {
                OutlinedRoundedSymbol(
                    MaterialSymbols.CLEAR,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

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
fun SearchBar(viewModel: ContactsViewModel, selection: SnapshotStateMap<Long, Contact>) {
    val inputState = rememberTextFieldState()
    val barState = rememberContainedSearchBarState()
    val scope = rememberCoroutineScope()

    val inputField = @Composable {
        SearchBarDefaults.InputField(
            leadingIcon = { SearchLeading(scope, barState) },
            trailingIcon = { SearchTrailing(barState, inputState) },
            searchBarState = barState,
            textFieldState = inputState,
            onSearch = { scope.launch { barState.animateToExpanded() } },
            placeholder = { Text("Search contacts") },
            colors = SearchBarDefaults.containedColors(barState).inputFieldColors
        )
    }

    Column {
        AppBarWithSearch(
            state = barState,
            inputField = inputField,
            colors = SearchBarDefaults.appBarWithSearchColors(
                appBarContainerColor = MaterialTheme.colorScheme.surface
            )
        )
        ExpandedFullScreenContainedSearchBar(
            state = barState,
            inputField = inputField,
            colors = SearchBarDefaults.containedColors(barState)
                .copy(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            SearchResults(inputState.text, selection)
        }
    }
}