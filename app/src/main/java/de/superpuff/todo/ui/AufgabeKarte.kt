package de.superpuff.todo.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import de.superpuff.todo.daten.Aufgabe
import de.superpuff.todo.logik.Kalender
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Eine Aufgabe als abgerundete Karte. Ein Tipp auf den runden Haken erledigt sie. */
@Composable
fun AufgabeKarte(
    aufgabe: Aufgabe,
    onErledigen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var abgehakt by remember(aufgabe.id) { mutableStateOf(false) }
    val bereich = rememberCoroutineScope()
    val label = Kalender.offenLabel(Kalender.tageOffen(aufgabe.datum, aufgabe.urspruenglichesDatum, aufgabe.erstelltAm))

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(start = 6.dp, end = 18.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RunderHaken(
                abgehakt = abgehakt,
                beschreibung = "Erledigt: ${aufgabe.text}",
                onClick = {
                    if (!abgehakt) {
                        abgehakt = true
                        // Kurz den gefüllten Haken zeigen, dann verschwindet die Karte.
                        bereich.launch {
                            delay(280)
                            onErledigen()
                        }
                    }
                },
            )
            Spacer(Modifier.width(6.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 10.dp)
                    .alpha(if (abgehakt) 0.5f else 1f),
            ) {
                Text(
                    text = aufgabe.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (abgehakt) TextDecoration.LineThrough else TextDecoration.None,
                )
                if (label != null) {
                    Spacer(Modifier.height(6.dp))
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RunderHaken(abgehakt: Boolean, beschreibung: String, onClick: () -> Unit) {
    val fuellung by animateColorAsState(
        targetValue = if (abgehakt) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "fuellung",
    )
    val rand by animateColorAsState(
        targetValue = if (abgehakt) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        label = "rand",
    )
    // 48 dp Tippfläche, 28 dp sichtbarer Kreis
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(role = Role.Checkbox, onClickLabel = beschreibung, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(fuellung)
                .border(2.dp, rand, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedVisibility(visible = abgehakt, enter = scaleIn() + fadeIn()) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
