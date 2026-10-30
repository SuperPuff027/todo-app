package de.superpuff.todo.ui

import android.content.Context
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Spracherkennung direkt in der App über [SpeechRecognizer], ohne Systemdialog.
 * Zwischenergebnisse kommen sofort über [onText] (fertig = false), das Endergebnis mit fertig = true.
 * Alle Aufrufe müssen auf dem Main-Thread passieren.
 */
class Spracheingabe(
    private val context: Context,
    private val onText: (erkannt: String, fertig: Boolean) -> Unit,
    private val onFehler: (code: Int) -> Unit,
) {
    var hoertZu by mutableStateOf(false)
        private set

    private var erkenner: SpeechRecognizer? = null

    val verfuegbar: Boolean get() = SpeechRecognizer.isRecognitionAvailable(context)

    fun starten() {
        if (hoertZu) return
        val e = erkenner ?: SpeechRecognizer.createSpeechRecognizer(context).also {
            it.setRecognitionListener(zuhoerer)
            erkenner = it
        }
        hoertZu = true
        e.startListening(
            spracherkennungIntent()
                .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                .putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true),
        )
    }

    /** Beendet die Aufnahme; das Endergebnis kommt danach noch über [onText]. */
    fun beenden() {
        if (hoertZu) erkenner?.stopListening()
    }

    /** Bricht die Aufnahme ab, ohne dass noch ein Ergebnis kommt. */
    fun abbrechen() {
        if (hoertZu) erkenner?.cancel()
        hoertZu = false
    }

    fun freigeben() {
        erkenner?.destroy()
        erkenner = null
        hoertZu = false
    }

    private fun ersterTreffer(ergebnisse: Bundle?): String? =
        ergebnisse?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.firstOrNull()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

    private val zuhoerer = object : RecognitionListener {
        override fun onPartialResults(teilErgebnisse: Bundle?) {
            ersterTreffer(teilErgebnisse)?.let { onText(it, false) }
        }

        override fun onResults(ergebnisse: Bundle?) {
            hoertZu = false
            ersterTreffer(ergebnisse)?.let { onText(it, true) }
        }

        override fun onError(fehler: Int) {
            hoertZu = false
            onFehler(fehler)
        }

        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
}
