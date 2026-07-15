package page.angad.contacts.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import contacts.core.entities.Contact
import kotlin.math.absoluteValue

@Composable
fun ContactPhoto(contact: Contact) {
    Box(Modifier.size(32.dp)) {
        contact.photoUri?.let {
            AsyncImage(
                model = it,
                contentDescription = contact.displayNamePrimary,
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(1F)
                    .clip(CircleShape)
            )
        } ?: InitialedContactPhoto(contact.displayNamePrimary ?: "(No name)")
    }
}

@Composable
private fun InitialedContactPhoto(name: String) {
    val initial = name.takeIf { it.isNotEmpty() }?.first()

    val color = remember(name) {
        val colors = listOf(
            Color(0xFF80DA88), Color(0xFFFFAEE4), Color(0xFFFFB683),
            Color(0xFF60D5F3), Color(0xFFD9BAFD), Color(0xFFFCBD00),
        )
        colors[(name.hashCode().absoluteValue) % colors.size]
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        if (initial != null) {
            Text(
                text = "$initial",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
        } else {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                tint = Color.White,
                contentDescription = "(No name)",
                modifier = Modifier.size(MaterialTheme.typography.titleLarge.fontSize.value.dp)
            )
        }
    }
}