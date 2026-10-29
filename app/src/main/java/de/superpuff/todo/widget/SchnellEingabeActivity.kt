package de.superpuff.todo.widget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.superpuff.todo.TodoApp
import de.superpuff.todo.daten.Aufgaben
import de.superpuff.todo.ui.EingabeFeld
import de.superpuff.todo.ui.TodoThema
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Kleines Eingabefenster über dem Homescreen, geöffnet vom Plus- oder Mikrofon-Knopf im Widget.
 * Android erlaubt in Widgets selbst keine Texteingabe.
 */
class SchnellEingabeActivity : ComponentActivity() {

    companion object {
        const val MIT_SPRACHE = "mit_sprache"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mitSprache = savedInstanceState == null && intent.getBooleanExtra(MIT_SPRACHE, false)
        val heute = LocalDate.now()

        setContent {
            TodoThema {
                var gespeichert by remember { mutableIntStateOf(0) }
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    // Tippen neben das Fenster schließt es. Als Geschwister unter der Fläche,
                    // damit Tipps auf die Fläche selbst nicht hier ankommen.
                    Spacer(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { finish() },
                    )
                    Surface(
                        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .imePadding(),
                    ) {
                        Column(
                            modifier = Modifier
                                .navigationBarsPadding()
                                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp),
                        ) {
                            EingabeFeld(
                                zielTag = heute,
                                heute = heute,
                                startMitSprache = mitSprache,
                                onSpeichern = { text ->
                                    gespeichert++
                                    TodoApp.hintergrund.launch { Aufgaben.hinzufuegen(applicationContext, text, heute) }
                                },
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = when (gespeichert) {
                                        0 -> ""
                                        1 -> "1 Aufgabe gespeichert"
                                        else -> "$gespeichert Aufgaben gespeichert"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 8.dp),
                                )
                                TextButton(onClick = { finish() }) { Text("Fertig") }
                            }
                        }
                    }
                }
            }
        }
    }
}
