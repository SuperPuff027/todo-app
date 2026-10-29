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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarViewWeek
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.superpuff.todo.daten.Aufgabe
import de.superpuff.todo.logik.Kalender
import java.time.LocalDate
import java.time.LocalTime

/** Die Liste eines Tages, bildschirmfüllend. */
@Composable
fun TagesAnsicht(
    tag: LocalDate,
    heute: LocalDate,
    aufgaben: List<Aufgabe>,
    erinnerung: LocalTime,
    onErledigen: (Aufgabe) -> Unit,
    onWoche: () -> Unit,
    onHeute: () -> Unit,
    onErinnerung: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val istHeute = tag == heute

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 12.dp, top = 20.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = Kalender.wochentag(tag),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = Kalender.tagUndMonat(tag) +
                        if (istHeute) " · Erinnerung ${Kalender.uhrzeit(erinnerung)}" else "",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onErinnerung) {
                Icon(Icons.Rounded.NotificationsNone, contentDescription = "Uhrzeit der Erinnerung ändern")
            }
            IconButton(onClick = onWoche) {
                Icon(Icons.Rounded.CalendarViewWeek, contentDescription = "Wochenübersicht")
            }
        }

        if (!istHeute) {
            FilledTonalButton(
                onClick = onHeute,
                shape = CircleShape,
                modifier = Modifier.padding(start = 20.dp, top = 10.dp),
            ) { Text("Zurück zu heute") }
        }

        Spacer(Modifier.height(16.dp))

        if (aufgaben.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (istHeute) "Nichts offen" else "Noch nichts geplant",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = when {
                            tag.isBefore(heute) -> "Offenes von diesem Tag ist schon auf heute gewandert."
                            else -> "Tippe auf + oder aufs Mikrofon."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(aufgaben, key = { it.id }) { aufgabe ->
                    AufgabeKarte(
                        aufgabe = aufgabe,
                        onErledigen = { onErledigen(aufgabe) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}
