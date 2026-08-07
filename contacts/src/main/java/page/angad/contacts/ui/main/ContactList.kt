package page.angad.contacts.ui.main

import android.accounts.AccountManager
import android.content.ContentResolver
import android.content.Context
import android.os.Bundle
import android.provider.ContactsContract
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import page.angad.contacts.ui.ContactsViewModel
import page.angad.contacts.ui.LoadingCounter
import page.angad.contacts.ui.add.AddFab
import page.angad.contacts.ui.main.components.ContactListBody
import page.angad.contacts.ui.main.components.Toolbar
import page.angad.contacts.ui.search.SearchBar
import page.angad.libcontacts.Contact
import kotlin.time.Duration.Companion.milliseconds

sealed class ContactListIntent {
    data object Ui : ContactListIntent()
    data class Pick(
        val resolve: suspend (Contact) -> Unit,
        val cancel: () -> Unit
    ) : ContactListIntent()
}

data class ContactListState(
    val viewModel: ContactsViewModel,
    val selection: SnapshotStateMap<Long, Contact>,
    val loading: LoadingCounter,
    val intent: ContactListIntent,
) {
    companion object {
        val Local = compositionLocalOf<ContactListState> {
            error("No state provided")
        }

        val current @Composable get() = Local.current
    }
}

private fun reload(context: Context, state: ContactListState) {
    val (viewModel, _, loading) = state

    viewModel.launch(loading) {
        val mgr = AccountManager.get(context)
        val acts = mgr.accounts

        for (act in acts) {
            val extras = Bundle().apply {
                putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)
                putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)
            }
            ContentResolver.requestSync(act, ContactsContract.AUTHORITY, extras)
        }
    }

    viewModel.launch {
        delay(750.milliseconds)
        viewModel.reload()
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ContactListScaffold(
    state: ContactListState,
    context: Context = LocalContext.current,
    content: @Composable () -> Unit,
) {
    val (_, _, loading) = state
    val loadCount by loading.state()
    val loadState = rememberPullToRefreshState()

    CompositionLocalProvider(ContactListState.Local provides state) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.surface,
            topBar = { SearchBar() },
        ) { padding ->
            Surface(
                modifier = Modifier
                    .padding(top = padding.calculateTopPadding())
                    .fillMaxSize(),
                color = MaterialTheme.colorScheme.surface
            ) {
                PullToRefreshBox(
                    isRefreshing = loadCount > 0,
                    onRefresh = { reload(context, state) },
                    state = loadState,
                    indicator = {
                        PullToRefreshDefaults.LoadingIndicator(
                            state = loadState,
                            isRefreshing = loadCount > 0,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                ) {
                    content()
                }
            }
        }
    }
}

@Preview
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ContactList(
    context: Context = LocalContext.current,
    intent: ContactListIntent = ContactListIntent.Ui
) {
    val loading = LoadingCounter(rememberCoroutineScope())
    val state = ContactListState(
        ContactsViewModel.new(context, loading),
        remember { mutableStateMapOf() },
        loading,
        intent
    )

    BackHandler(state.selection.isNotEmpty()) { state.selection.clear() }

    ContactListScaffold(state) {
        Box(Modifier.fillMaxSize()) {
            ContactListBody()
            Toolbar()
            AddFab()
        }
    }
}