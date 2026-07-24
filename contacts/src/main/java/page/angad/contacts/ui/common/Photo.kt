package page.angad.contacts.ui.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import contacts.core.entities.Contact
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
    val poly = ExpressiveShape.Circle

    val morph = remember(contact.id) { Morph(poly, MaterialShapes.SoftBurst) }
    val scheme = MaterialTheme.motionScheme
    val t by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = scheme.defaultSpatialSpec(),
        label = "Contact/Select/PhotoMorph/Progress"
    )

    val scale = remember(contact.id) { Animatable(1f) }
    val scaleSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val check = remember(contact.id) { Animatable(0f) }

    LaunchedEffect(selected) {
        val scaleTarget = if (selected) 1.25f else 1f
        val checkTarget = if (selected) 1f else 0f

        scale.animateTo(scaleTarget, animationSpec = scaleSpec)
        check.animateTo(checkTarget)
    }

    val shape = remember(morph, t) { MorphShape(morph, t) }

    Box(
        Modifier
            .size(32.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .clip(shape)
    ) {
        ContactPhoto(contact)

        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = check.value
                    scaleY = check.value
                }
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Icon(
                Icons.Filled.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(14.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ContactPhoto(contact: Contact) {
    if (contact.photoUri == null) {
        InitialedContactPhoto(contact.displayNamePrimary ?: "(No name)")
        return
    }

    AsyncImage(
        model = contact.photoUri,
        contentDescription = contact.displayNamePrimary,
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

    Box(
        modifier = Modifier
            .fillMaxSize()
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