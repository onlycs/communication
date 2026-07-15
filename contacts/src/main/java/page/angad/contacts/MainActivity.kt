package page.angad.contacts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import page.angad.contacts.ui.contacts.ContactsScreen
import page.angad.contacts.ui.theme.ContactsTheme
import page.angad.uicore.RequirePermissions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ContactsTheme {
                RequirePermissions(
                    permissions = listOf(
                        android.Manifest.permission.READ_CONTACTS,
                        android.Manifest.permission.WRITE_CONTACTS
                    )
                ) {
                    ContactsScreen()
                }
            }
        }
    }
}
