package de.superpuff.todo.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Erkennt eine Pinch-Geste mit zwei Fingern.
 * Finger zusammen (Verkleinern unter 80 %) → [onRaus]; Finger auseinander (über 125 %) → [onRein].
 *
 * Die Ereignisse werden im Initial-Durchgang nur gelesen und nicht verbraucht,
 * damit Scrollen mit einem Finger in Listen weiter funktioniert.
 */
fun Modifier.pinchZoom(onRaus: () -> Unit, onRein: () -> Unit): Modifier = composed {
    val raus by rememberUpdatedState(onRaus)
    val rein by rememberUpdatedState(onRein)
    pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            var faktor = 1f
            var ausgeloest = false
            do {
                val ereignis = awaitPointerEvent(PointerEventPass.Initial)
                val finger = ereignis.changes.count { it.pressed }
                if (finger >= 2 && !ausgeloest) {
                    faktor *= ereignis.calculateZoom()
                    if (faktor < 0.8f) {
                        ausgeloest = true
                        raus()
                    } else if (faktor > 1.25f) {
                        ausgeloest = true
                        rein()
                    }
                }
            } while (ereignis.changes.any { it.pressed })
        }
    }
}
