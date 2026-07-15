package page.angad.contacts.ui.list

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import page.angad.contacts.ui.header.SearchLeading
import page.angad.contacts.ui.header.SearchTrailing

@Preview
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ContactList(context: Context = LocalContext.current) {
    val inputState = rememberTextFieldState()
    val barState = rememberSearchBarState()
    val scope = rememberCoroutineScope()
    val viewModel: ContactListViewModel = viewModel(
        factory = ContactsViewModelFactory(context)
    )

    val inputField = @Composable {
        SearchBarDefaults.InputField(
            leadingIcon = { SearchLeading(scope, barState) },
            trailingIcon = { SearchTrailing(barState) },
            searchBarState = barState,
            textFieldState = inputState,
            onSearch = { scope.launch { barState.animateToExpanded() } },
            placeholder = { Text("Search contacts") },
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                AppBarWithSearch(
                    state = barState,
                    inputField = inputField,
                    colors = SearchBarDefaults.appBarWithSearchColors(
                        appBarContainerColor = Color.Transparent
                    )
                )
                ExpandedFullScreenSearchBar(state = barState, inputField = inputField) {
                    // search results
                }
            }
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = MaterialTheme.shapes.extraLarge.copy(
                bottomStart = CornerSize(0.dp),
                bottomEnd = CornerSize(0.dp)
            )
        ) {
            ContactListBody(contacts = viewModel.list)
        }
    }
}