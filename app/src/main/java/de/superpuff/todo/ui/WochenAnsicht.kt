package de.superpuff.todo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.superpuff.todo.daten.Aufgabe
import de.superpuff.todo.logik.Kalender
import java.time.LocalDate

/** Sieben abgerundete Tageskacheln, Montag bis Sonntag. Tipp auf einen Tag öffnet ihn zum Planen. */
@Composable
fun WochenAnsicht(
    wochenStart: LocalDate,
    heute: LocalDate,
    aufgaben: Map<LocalDate, List<Aufgabe>>,
    onTag: (LocalDate) -> Unit,
    onVerschieben: (Long) -> Unit,
    onDieseWoche: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tage = Kalender.wocheAb(wochenStart)
    val istDieseWoche = wochenStart == Kalender.wochenStart(heute)

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 12.dp, top = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Woche ${Kalender.kalenderwoche(wochenStart)}",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = Kalender.wochenspanne(wochenStart),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = { onVerschieben(-1) }) {
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Vorige Woche")
            }
            IconButton(onClick = { onVerschieben(1) }) {
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Nächste Woche")
            }
        }

        if (!istDieseWoche) {
            FilledTonalButton(
                onClick = onDieseWoche,
                shape = CircleShape,
                modifier = Modifier.padding(start = 20.dp, top = 10.dp),
            ) { Text("Diese Woche") }
        }

        Text(
            text = "Tippe auf einen Tag, um ihn zu planen",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 24.dp, top = 12.dp, bottom = 4.dp),
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(tage, key = { it.toEpochDay() }) { tag ->
                TagKachel(
                    tag = tag,
                    heute = heute,
                    aufgaben = aufgaben[tag].orEmpty(),
                    onClick = { onTag(tag) },
                )
            }
        }
    }
}

@Composable
private fun TagKachel(
    tag: LocalDate,
    heute: LocalDate,
    aufgaben: List<Aufgabe>,
    onClick: () -> Unit,
) {
    val istHeute = tag == heute
    val vergangen = tag.isBefore(heute)

    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = if (istHeute) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 132.dp)
            .alpha(if (vergangen) 0.5f else 1f),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (istHeute) "Heute" else Kalender.wochentag(tag),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = Kalender.tagUndMonatKurz(tag),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (aufgaben.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(28.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                            Box(modifier = Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = aufgaben.size.toString(),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            aufgaben.take(3).forEach { aufgabe ->
                Text(
                    text = aufgabe.text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(vertical = 1.dp),
                )
            }
            if (aufgaben.size > 3) {
                Text(
                    text = "+ ${aufgaben.size - 3} weitere",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
