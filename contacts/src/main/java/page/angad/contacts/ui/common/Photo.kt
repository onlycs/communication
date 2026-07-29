package page.angad.contacts.ui.common

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.toPath
import coil.compose.AsyncImage
import dev.vicart.compose.material.symbols.FilledRoundedSymbol
import dev.vicart.compose.material.symbols.FilledSymbol
import dev.vicart.compose.material.symbols.MaterialSymbols
import page.angad.libcontacts.Contact
import page.angad.libcontacts.schema.Contacts
import kotlin.math.absoluteValue
import androidx.compose.material3.MaterialShapes.Companion as ExpressiveShape

class MorphShape(
    private val morph: Morph,
    private val progress: Float,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = morph.toPath(progress).asComposePath()
        val matrix = Matrix().apply {
            scale(size.width, size.height)
        }

        path.transform(matrix)

        return Outline.Generic(path)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SelectablePhoto(
    contact: Contact,
    selected: Boolean = false,
) {
    val id = contact.id
    val poly = ExpressiveShape.Circle

    val morph = remember(id) { Morph(poly, MaterialShapes.SoftBurst) }
    val scheme = MaterialTheme.motionScheme

    val t by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = scheme.defaultSpatialSpec(),
        label = "Contact/Select/PhotoMorph"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.25f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "Contact/Select/PhotoScale"
    )
    val check by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        label = "Contact/Select/CheckScale"
    )

    val shape = remember(morph, t) { MorphShape(morph, t) }

    Box(
        Modifier
            .size(32.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
    ) {
        ContactPhoto(contact)

        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = check
                    scaleY = check
                }
                .background(MaterialTheme.colorScheme.primary)
        ) {
            FilledRoundedSymbol(
                icon = MaterialSymbols.CHECK,
                tint = MaterialTheme.colorScheme.onPrimary,
                size = 16.dp,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ContactPhoto(contact: Contact) {
    val photoUri = contact[Contacts.PhotoUri]
    val displayName = contact[Contacts.DisplayName]

    if (photoUri == null) {
        InitialedContactPhoto(displayName.orEmpty())
        return
    }

    AsyncImage(
        model = photoUri,
        contentDescription = displayName,
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(1f)
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun InitialedContactPhoto(name: String) {
    val initial = name.takeIf { it.isNotEmpty() }?.first()

    val color = remember(name) {
        val colors = listOf(
            Color(0xFF80DA88), Color(0xFFFFAEE4), Color(0xFFFFB683),
            Color(0xFF60D5F3), Color(0xFFD9BAFD), Color(0xFFFCBD00),
        )
        colors[name.hashCode().absoluteValue % colors.size]
    }


    if (initial != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$initial",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
        }
    } else {
        Box(
            Modifier
                .fillMaxSize()
                .background(color)
        ) {
            FilledSymbol(
                icon = MaterialSymbols.ACCOUNT_CIRCLE,
                tint = Color.White,
                size = 16.dp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .scale(3.15f)
            )
        }
    }
}