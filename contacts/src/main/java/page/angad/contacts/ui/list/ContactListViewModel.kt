package page.angad.contacts.ui.list

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import contacts.core.Contacts
import contacts.core.ContactsFields
import contacts.core.Fields
import contacts.core.asc
import contacts.core.entities.Contact
import kotlinx.coroutines.launch

class ContactListViewModel(context: Context) : ViewModel() {
    val contacts = Contacts(context)
    var list = emptyList<Contact>()
    var starred = emptyList<Contact>()

    init {
        reload()
    }

    fun reload() {
        viewModelScope.launch {
            list = contacts
                .broadQuery()
                .orderBy(ContactsFields.DisplayNamePrimary.asc(ignoreCase = true))
                .include(
                    Fields.Contact.DisplayNamePrimary,
                    Fields.Contact.PhotoUri,
                    Fields.Contact.Options.Starred
                )
                .find()

            starred = list.filter { it.options?.starred ?: false }
        }
    }

    companion object {
        @Composable
        fun new(context: Context = LocalContext.current): ContactListViewModel {
            return viewModel(factory = ContactsViewModelFactory(context))
        }
    }
}

class ContactsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ContactListViewModel(context.applicationContext) as T
    }
}
