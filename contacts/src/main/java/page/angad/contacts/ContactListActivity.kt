package page.angad.contacts

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import page.angad.contacts.ui.main.ContactList
import page.angad.contacts.ui.main.ContactListIntent
import page.angad.contacts.ui.theme.ContactsTheme
import page.angad.uicore.RequirePermissions

class ContactListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val isPick = intent.action == Intent.ACTION_PICK

        setContent {
            ContactsTheme {
                RequirePermissions(
                    permissions = listOf(
                        android.Manifest.permission.READ_CONTACTS,
                        android.Manifest.permission.WRITE_CONTACTS
                    )
                ) {
                    ContactList(
                        intent = when {
                            isPick -> ContactListIntent.Pick(
                                {
                                    setResult(RESULT_OK, Intent().setData(it.lookupUri()))
                                    finish()
                                },
                                {
                                    setResult(RESULT_CANCELED)
                                    finish()
                                }
                            )

                            else -> ContactListIntent.Ui
                        }
                    )
                }
            }
        }
    }
}
