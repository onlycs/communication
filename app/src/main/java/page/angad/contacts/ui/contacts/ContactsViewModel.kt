package page.angad.contacts.ui.contacts

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import page.angad.contacts.data.Contact

class ContactsViewModel(context: Context) : ViewModel() {
    var contacts by mutableStateOf<List<Contact>>(emptyList())
        private set

    init {
        viewModelScope.launch(Dispatchers.IO) {
            contacts = Contact.query(context)
        }
    }
}

class ContactsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ContactsViewModel(context.applicationContext) as T
    }
}