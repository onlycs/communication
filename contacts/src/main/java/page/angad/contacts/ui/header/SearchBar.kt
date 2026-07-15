package page.angad.contacts.ui.header

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.SearchBarValue
import androidx.compose.runtime.Composable
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
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTrailing(state: SearchBarState) {
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
