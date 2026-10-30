package de.superpuff.todo.ui

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import de.superpuff.todo.logik.Kalender
import java.time.LocalDate
import java.util.Locale

/** Intent für die Spracherkennung (In-App über [Spracheingabe] oder als Systemdialog). */
fun spracherkennungIntent(): Intent =
    Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        .putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
        .putExtra(RecognizerIntent.EXTRA_PROMPT, "Aufgabe sprechen")
        .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)

/** Ab dieser Druckdauer gilt das Mikrofon als „gedrückt gehalten“: Loslassen beendet die Aufnahme. */
private const val HALTEN_AB_MS = 500L

/**
 * Eingabe einer Aufgabe per Tastatur oder Mikrofon.
 * Mikrofon antippen: hört zu, bis man aufhört zu sprechen. Gedrückt halten: hört zu, bis man loslässt.
 * Gesprochenes erscheint schon während des Sprechens im Textfeld und wird erst mit Enter gespeichert,
 * damit man es vorher korrigieren kann.
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
    // Was vor der Aufnahme schon im Feld stand; Erkanntes wird dahinter gehängt.
    var basis by remember { mutableStateOf("") }
    val fokus = remember { FocusRequester() }

    val spracheingabe = remember {
        Spracheingabe(
            context = context.applicationContext,
            onText = { erkannt, _ ->
                text = (if (basis.isBlank()) erkannt else "$basis $erkannt").replaceFirstChar { it.uppercaseChar() }
            },
            onFehler = { code ->
                val hinweis = when (code) {
                    SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Nichts verstanden"
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Spracherkennung braucht gerade Internet"
                    else -> "Spracherkennung fehlgeschlagen"
                }
                Toast.makeText(context, hinweis, Toast.LENGTH_SHORT).show()
                runCatching { fokus.requestFocus() }
            },
        )
    }
    DisposableEffect(Unit) { onDispose { spracheingabe.freigeben() } }

    // Rückfall ohne Mikrofon-Erlaubnis oder ohne Erkennungsdienst: Systemdialog.
    val systemDialog = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { ergebnis ->
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

    fun mitSystemDialog() {
        try {
            systemDialog.launch(spracherkennungIntent())
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "Keine Spracherkennung auf dem Gerät gefunden", Toast.LENGTH_LONG).show()
        }
    }

    fun zuhoeren() {
        basis = text.trim()
        spracheingabe.starten()
    }

    val mikrofonErlaubnis = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { erlaubt ->
        if (erlaubt) zuhoeren() else mitSystemDialog()
    }

    fun spracheStarten() {
        when {
            !spracheingabe.verfuegbar -> mitSystemDialog()
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED -> zuhoeren()
            else -> mikrofonErlaubnis.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
    val starten by rememberUpdatedState<() -> Unit> { spracheStarten() }

    val speichern: () -> Unit = {
        // Sonst würde das Endergebnis der Aufnahme das geleerte Feld wieder füllen.
        spracheingabe.abbrechen()
        if (text.isNotBlank()) {
            onSpeichern(text)
            text = ""
        }
    }

    LaunchedEffect(Unit) {
        if (startMitSprache) starten() else runCatching { fokus.requestFocus() }
    }

    val hoertZu = spracheingabe.hoertZu
    val mikrofonHintergrund by animateColorAsState(
        targetValue = if (hoertZu) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "mikrofon",
    )

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
                placeholder = { Text(if (hoertZu) "Ich höre zu …" else "Neue Aufgabe") },
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
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(mikrofonHintergrund)
                            .semantics {
                                role = Role.Button
                                contentDescription = if (hoertZu) "Aufnahme beenden" else "Per Sprache eintragen"
                                onClick {
                                    if (spracheingabe.hoertZu) spracheingabe.beenden() else starten()
                                    true
                                }
                            }
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        if (spracheingabe.hoertZu) {
                                            spracheingabe.beenden()
                                            return@detectTapGestures
                                        }
                                        val beginn = System.currentTimeMillis()
                                        starten()
                                        tryAwaitRelease()
                                        if (System.currentTimeMillis() - beginn >= HALTEN_AB_MS) spracheingabe.beenden()
                                    },
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Rounded.Mic,
                            contentDescription = null,
                            tint = if (hoertZu) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
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
