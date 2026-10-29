package de.superpuff.todo.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import de.superpuff.todo.logik.Kalender
import java.time.LocalDate
import java.util.Locale

/** Intent für die Spracherkennung des Systems (Google-Spracheingabe o. ä.). */
fun spracherkennungIntent(): Intent =
    Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        .putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
        .putExtra(RecognizerIntent.EXTRA_PROMPT, "Aufgabe sprechen")
        .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)

/**
 * Eingabe einer Aufgabe per Tastatur oder Mikrofon.
 * Gesprochenes landet erst im Textfeld, damit man es vor dem Speichern korrigieren kann.
 * Nach dem Speichern bleibt das Feld offen und leer, so lassen sich mehrere Aufgaben hintereinander eintragen.
 */
@Composable
fun EingabeFeld(
    zielTag: LocalDate,
    heute: LocalDate,
    onSpeichern: (String) -> Unit,
    modifier: Modifier = Modifier,
    startMitSprache: Boolean = false,
) {
    val context = LocalContext.current
    var text by rememberSaveable { mutableStateOf("") }
    val fokus = remember { FocusRequester() }

    val sprache = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { ergebnis ->
        if (ergebnis.resultCode == Activity.RESULT_OK) {
            val erkannt = ergebnis.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
                ?.trim()
            if (!erkannt.isNullOrEmpty()) {
                text = (if (text.isBlank()) erkannt else "${text.trim()} $erkannt")
                    .replaceFirstChar { it.uppercaseChar() }
            }
        }
        runCatching { fokus.requestFocus() }
    }

    val starteSprache: () -> Unit = {
        try {
            sprache.launch(spracherkennungIntent())
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "Keine Spracherkennung auf dem Gerät gefunden", Toast.LENGTH_LONG).show()
        }
    }

    val speichern: () -> Unit = {
        if (text.isNotBlank()) {
            onSpeichern(text)
            text = ""
        }
    }

    LaunchedEffect(Unit) {
        if (startMitSprache) starteSprache() else runCatching { fokus.requestFocus() }
    }

    Column(modifier = modifier) {
        Text(
            text = Kalender.zielText(zielTag, heute),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp),
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(fokus),
                placeholder = { Text("Neue Aufgabe") },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { speichern() }),
                trailingIcon = {
                    IconButton(onClick = starteSprache) {
                        Icon(Icons.Rounded.Mic, contentDescription = "Per Sprache eintragen")
                    }
                },
            )
            Spacer(Modifier.width(10.dp))
            FilledIconButton(
                onClick = speichern,
                enabled = text.isNotBlank(),
                shape = CircleShape,
                modifier = Modifier.size(54.dp),
            ) {
                Icon(Icons.Rounded.ArrowUpward, contentDescription = "Speichern")
            }
        }
    }
}
