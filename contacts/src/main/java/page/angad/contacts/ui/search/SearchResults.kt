package page.angad.contacts.ui.search

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import page.angad.contacts.ui.main.ContactListState
import page.angad.contacts.ui.main.components.SearchedContactList
import page.angad.contacts.ui.main.displayName
import page.angad.fuzzy.Fzf
import page.angad.fuzzy.FzfOptions
import page.angad.fuzzy.FzfResultItem
import page.angad.libcontacts.Contact

@Composable
fun SearchResults(field: TextFieldState, state: ContactListState = ContactListState.current) {
    fun Fzf(it: List<Contact>) = Fzf(
        it,
        FzfOptions({ it.displayName })
    )

    val (viewModel) = state
    var fzf by remember { mutableStateOf(Fzf(emptyList())) }
    var results by remember { mutableStateOf<List<FzfResultItem<Contact>>>(emptyList()) }

    LaunchedEffect(viewModel) {
        fzf = withContext(Dispatchers.Default) { Fzf(viewModel.list) }
    }

    LaunchedEffect(fzf, field) {
        snapshotFlow { field.text.toString() }
            .distinctUntilChanged()
            .collectLatest {
                results = withContext(Dispatchers.Default) { fzf.find(it) }
            }
    }

    SearchedContactList(results.map { it.item to it.positions })
}