package page.angad.contacts.ui.add

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.MediumFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.vicart.compose.material.symbols.MaterialSymbols
import dev.vicart.compose.material.symbols.OutlinedRoundedSymbol
import page.angad.contacts.ui.main.ContactListIntent
import page.angad.contacts.ui.main.ContactListState

@Composable
fun BoxScope.AddFab(state: ContactListState = ContactListState.current) {
    val (_, selection, _, intent) = state
    val visible = selection.isEmpty() && intent is ContactListIntent.Ui

    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            animationSpec = FloatingToolbarDefaults.animationSpec(),
            initialOffsetX = { it / 2 }
        ),
        exit = slideOutHorizontally(
            animationSpec = FloatingToolbarDefaults.animationSpec(),
            targetOffsetX = { 3 * it / 2 }
        ),
        modifier = Modifier.align(Alignment.BottomEnd)
    ) {
        MediumFloatingActionButton(
            onClick = { }, // TODO:
            modifier = Modifier
                .padding(16.dp)
                .navigationBarsPadding(),
        ) {
            OutlinedRoundedSymbol(
                MaterialSymbols.ADD,
                size = 32.dp,
            )
        }
    }
}