package page.angad.contacts.ui.list

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import page.angad.contacts.ui.list.components.ContactListBody
import page.angad.contacts.ui.list.page.SearchBar
import page.angad.contacts.ui.list.page.Toolbar
import page.angad.libcontacts.Contact

@Preview
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ContactList(context: Context = LocalContext.current) {
    val selection = remember { mutableStateMapOf<Long, Contact>() }
    val viewModel = ContactListViewModel.new(context)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = { SearchBar(viewModel) },
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding()),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = MaterialTheme.shapes.extraLarge.copy(
                bottomStart = CornerSize(0.dp),
                bottomEnd = CornerSize(0.dp)
            )
        ) {
            Box(Modifier.fillMaxSize()) {
                ContactListBody(
                    contacts = viewModel.list,
                    starred = viewModel.starred,
                    selection = selection,
                )

                Toolbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding(),
                    viewModel,
                    selection,
                )
            }
        }
    }
}
