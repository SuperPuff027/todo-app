package de.superpuff.todo.ui

import android.app.TimePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.superpuff.todo.daten.Aufgabe
import de.superpuff.todo.logik.Kalender
import kotlinx.coroutines.launch
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HauptBildschirm(vm: HauptViewModel) {
    val heute by vm.heute.collectAsStateWithLifecycle()
    val tag by vm.tag.collectAsStateWithLifecycle()
    val ansicht by vm.ansicht.collectAsStateWithLifecycle()
    val wochenStart by vm.wochenStart.collectAsStateWithLifecycle()
    val aufgaben by vm.aufgabenDesTages.collectAsStateWithLifecycle()
    val woche by vm.aufgabenDerWoche.collectAsStateWithLifecycle()
    val erinnerung by vm.erinnerung.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val hinweise = remember { SnackbarHostState() }
    val bereich = rememberCoroutineScope()
    var eingabeOffen by remember { mutableStateOf(false) }
    var mitSprache by remember { mutableStateOf(false) }

    BackHandler(enabled = ansicht == Ansicht.WOCHE) { vm.zumTag() }
    BackHandler(enabled = ansicht == Ansicht.TAG && tag != heute) { vm.zuHeute() }

    val erledigen: (Aufgabe) -> Unit = { aufgabe ->
        vm.erledigen(aufgabe)
        bereich.launch {
            hinweise.currentSnackbarData?.dismiss()
            val ergebnis = hinweise.showSnackbar(
                message = "Erledigt: ${aufgabe.text}",
                actionLabel = "Rückgängig",
                duration = SnackbarDuration.Short, // 4 Sekunden
            )
            if (ergebnis == SnackbarResult.ActionPerformed) vm.rueckgaengig(aufgabe)
        }
    }

    val erinnerungAendern: () -> Unit = {
        TimePickerDialog(
            context,
            { _, stunde, minute ->
                val zeit = LocalTime.of(stunde, minute)
                vm.setzeErinnerung(zeit)
                bereich.launch { hinweise.showSnackbar("Erinnerung täglich um ${Kalender.uhrzeit(zeit)}, wenn noch etwas offen ist") }
            },
            erinnerung.hour,
            erinnerung.minute,
            true,
        ).show()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(hinweise) { daten -> Snackbar(daten, shape = RoundedCornerShape(20.dp)) }
        },
        floatingActionButton = {
            if (ansicht == Ansicht.TAG && !tag.isBefore(heute)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SmallFloatingActionButton(
                        onClick = { mitSprache = true; eingabeOffen = true },
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(48.dp),
                    ) { Icon(Icons.Rounded.Mic, contentDescription = "Per Sprache eintragen") }
                    Spacer(Modifier.width(12.dp))
                    FloatingActionButton(
                        onClick = { mitSprache = false; eingabeOffen = true },
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(64.dp),
                    ) { Icon(Icons.Rounded.Add, contentDescription = "Aufgabe eintragen") }
                }
            }
        },
    ) { innen ->
        AnimatedContent(
            targetState = ansicht,
            transitionSpec = {
                if (targetState == Ansicht.WOCHE) {
                    // Rauszoomen: Der Tag schrumpft, die Woche kommt von etwas größer.
                    (fadeIn(tween(220)) + scaleIn(tween(280), initialScale = 1.08f)) togetherWith
                        (fadeOut(tween(180)) + scaleOut(tween(280), targetScale = 0.85f))
                } else {
                    // Reinzoomen: Der Tag wächst aus der Woche heraus.
                    (fadeIn(tween(220)) + scaleIn(tween(280), initialScale = 0.85f)) togetherWith
                        (fadeOut(tween(180)) + scaleOut(tween(280), targetScale = 1.08f))
                }
            },
            modifier = Modifier
                .padding(innen)
                .fillMaxSize()
                .pinchZoom(
                    onRaus = { if (vm.ansicht.value == Ansicht.TAG) vm.zurWoche() },
                    onRein = { if (vm.ansicht.value == Ansicht.WOCHE) vm.zumTag() },
                ),
            label = "zoom",
        ) { gezeigt ->
            when (gezeigt) {
                Ansicht.TAG -> TagesAnsicht(
                    tag = tag,
                    heute = heute,
                    aufgaben = aufgaben,
                    erinnerung = erinnerung,
                    onErledigen = erledigen,
                    onWoche = vm::zurWoche,
                    onHeute = vm::zuHeute,
                    onErinnerung = erinnerungAendern,
                )
                Ansicht.WOCHE -> WochenAnsicht(
                    wochenStart = wochenStart,
                    heute = heute,
                    aufgaben = woche,
                    onTag = { vm.zumTag(it) },
                    onVerschieben = vm::wocheVerschieben,
                    onDieseWoche = vm::dieseWoche,
                )
            }
        }
    }

    if (eingabeOffen) {
        ModalBottomSheet(
            onDismissRequest = { eingabeOffen = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        ) {
            EingabeFeld(
                zielTag = tag,
                heute = heute,
                startMitSprache = mitSprache,
                onSpeichern = vm::hinzufuegen,
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp, bottom = 28.dp)
                    .imePadding(),
            )
        }
    }
}
