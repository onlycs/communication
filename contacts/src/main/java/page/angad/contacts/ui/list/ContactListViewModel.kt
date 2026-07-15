package page.angad.contacts.ui.list

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import contacts.core.Contacts
import contacts.core.ContactsFields
import contacts.core.Fields
import contacts.core.asc
import contacts.core.desc
import contacts.core.entities.Contact
import kotlinx.coroutines.launch

class ContactListViewModel(context: Context) : ViewModel() {
    val contacts = Contacts(context)
    var list = emptyList<Contact>()

    init {
        reload()
    }

    fun reload() {
        viewModelScope.launch {
            list = contacts
                .broadQuery()
                .orderBy(
                    ContactsFields.Options.Starred.desc(),
                    ContactsFields.DisplayNamePrimary.asc(ignoreCase = true)
                )
                .include(
                    Fields.Contact.DisplayNamePrimary,
                    Fields.Contact.PhotoUri,
                    Fields.Contact.Options.Starred
                )
                .find()
        }
    }
}

class ContactsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ContactListViewModel(context.applicationContext) as T
    }
}