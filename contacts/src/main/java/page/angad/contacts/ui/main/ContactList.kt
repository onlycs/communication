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
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import page.angad.contacts.ui.ContactsViewModel
import page.angad.contacts.ui.LoadingCounter
import page.angad.contacts.ui.add.AddFab
import page.angad.contacts.ui.main.components.ContactListBody
import page.angad.contacts.ui.main.components.Toolbar
import page.angad.contacts.ui.search.SearchBar
import page.angad.libcontacts.Contact
import kotlin.time.Duration.Companion.milliseconds

@Preview
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ContactList(context: Context = LocalContext.current) {
    val scope = rememberCoroutineScope()

    val loading = LoadingCounter(scope)
    val loadCount by loading.state()
    val loadState = rememberPullToRefreshState()

    val selection = remember { mutableStateMapOf<Long, Contact>() }
    val viewModel = ContactsViewModel.new(context, loading)

    BackHandler(selection.isNotEmpty()) { selection.clear() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = { SearchBar(viewModel, selection) },
    ) { padding ->
        Surface(
            modifier = Modifier
                .padding(top = padding.calculateTopPadding())
                .fillMaxSize(),
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.extraLarge.copy(
                bottomStart = CornerSize(0.dp),
                bottomEnd = CornerSize(0.dp)
            )
        ) {
            PullToRefreshBox(
                isRefreshing = loadCount > 0,
                onRefresh = {
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
                },
                state = loadState,
                indicator = {
                    PullToRefreshDefaults.LoadingIndicator(
                        state = loadState,
                        isRefreshing = loadCount > 0,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            ) {
                Box(Modifier.fillMaxSize()) {
                    ContactListBody(
                        contacts = viewModel.list,
                        starred = viewModel.starred,
                        selection = selection,
                    )

                    Toolbar(
                        viewModel = viewModel,
                        selection = selection,
                        loading = loading
                    )

                    AddFab(selection.isEmpty())
                }
            }
        }
    }
}
