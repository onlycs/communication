package page.angad.contacts.ui.header

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.ExpandedFullScreenContainedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberContainedSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import page.angad.contacts.ui.list.ContactListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchLeading(scope: CoroutineScope, state: SearchBarState) {
    when (state.currentValue) {
        SearchBarValue.Expanded -> {
            IconButton(onClick = { scope.launch { state.animateToCollapsed() } }) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        SearchBarValue.Collapsed -> {
            Icon(
                Icons.Default.Search,
                contentDescription = "Search",
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
                Icon(
                    Icons.Default.Clear,
                    contentDescription = "Clear",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        SearchBarValue.Collapsed -> {
            IconButton(onClick = { /* menu side panel thingy */ }) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(viewModel: ContactListViewModel) {
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

    Column(modifier = Modifier.padding(bottom = 16.dp)) {
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
            // search results
        }
    }
}