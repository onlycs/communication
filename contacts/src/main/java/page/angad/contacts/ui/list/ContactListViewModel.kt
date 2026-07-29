package page.angad.contacts.ui.list

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import page.angad.contacts.util.LoadingCounter
import page.angad.contacts.util.attach
import page.angad.libcontacts.Contact
import page.angad.libcontacts.ContactsApi
import page.angad.libcontacts.asc
import page.angad.libcontacts.schema.Contacts
import page.angad.libcontacts.schema.RawContacts

class ContactListViewModel(context: Context, loading: LoadingCounter) : ViewModel() {
    val api = ContactsApi(context)

    var list by mutableStateOf(emptyList<Contact>())
        private set
    var map by mutableStateOf(emptyMap<Long, Contact>())
        private set
    var starred by mutableStateOf(emptyList<Contact>())
        private set

    init {
        viewModelScope.launch { reload() }.attach(loading)
    }

    suspend fun reload() {
        list = api
            .select(
                Contacts.DisplayName,
                Contacts.PhotoUri,
                Contacts.Starred,
                RawContacts.AccountType,
            )
            .orderBy(Contacts.DisplayName.asc(ignoreCase = true))
            .find()

        map = list.associateBy { it.id }
        starred = list.filter { it[Contacts.Starred] }
    }

    suspend fun reload(ids: Collection<Long>) {
        val fresh = ids.map { it to map[it]?.reload() }.associate { it }

        list = list.mapNotNull { if (it.id in fresh) fresh[it.id] else it }
        map = list.associateBy { it.id }
        starred = list.filter { it[Contacts.Starred] }
    }

    companion object {
        @Composable
        fun new(
            context: Context = LocalContext.current,
            loading: LoadingCounter
        ): ContactListViewModel {
            return viewModel(factory = ContactsViewModelFactory(context, loading))
        }
    }
}

class ContactsViewModelFactory(private val context: Context, private val loading: LoadingCounter) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ContactListViewModel(context.applicationContext, loading) as T
    }
}
