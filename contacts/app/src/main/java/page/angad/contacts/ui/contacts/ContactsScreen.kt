package page.angad.contacts.ui.contacts

import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchLeading(scope: CoroutineScope, state: SearchBarState) {
    when (state.currentValue) {
        SearchBarValue.Expanded -> {
            IconButton(onClick = { scope.launch { state.animateToCollapsed() } }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                )
            }
        }

        SearchBarValue.Collapsed -> {
            Icon(
                Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTrailing(scope: CoroutineScope, state: SearchBarState) {
    if (state.currentValue == SearchBarValue.Collapsed) {
        IconButton(onClick = { /* menu side panel thingy */ }) {
            Icon(
                Icons.Default.Menu,
                contentDescription = "Menu",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ContactsScreen() {
    val inputState = rememberTextFieldState()
    val barState = rememberSearchBarState()
    val scope = rememberCoroutineScope()
    val scroll = SearchBarDefaults.enterAlwaysSearchBarScrollBehavior()

    val inputField = @Composable {
        SearchBarDefaults.InputField(
            leadingIcon = { SearchLeading(scope, barState) },
            trailingIcon = { SearchTrailing(scope, barState) },
            searchBarState = barState,
            textFieldState = inputState,
            onSearch = { scope.launch { barState.animateToExpanded() } },
            placeholder = { Text("Search contacts") },
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scroll.nestedScrollConnection),
        topBar = {
            AppBarWithSearch(
                state = barState,
                inputField = inputField,
                scrollBehavior = scroll,
            )
            ExpandedFullScreenSearchBar(
                state = barState,
                inputField = inputField
            ) {
                // search results
            }
        }
    ) { padding ->
        // main content
    }
}