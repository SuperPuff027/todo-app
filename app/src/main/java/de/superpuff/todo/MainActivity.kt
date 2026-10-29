package de.superpuff.todo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import de.superpuff.todo.daten.Einstellungen
import de.superpuff.todo.ui.HauptBildschirm
import de.superpuff.todo.ui.HauptViewModel
import de.superpuff.todo.ui.TodoThema

class MainActivity : ComponentActivity() {

    private val vm: HauptViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TodoThema {
                BenachrichtigungenErlauben()
                HauptBildschirm(vm)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        vm.beimFortsetzen()
    }
}

/** Fragt ab Android 13 einmalig nach der Erlaubnis für Benachrichtigungen (nötig für die Erinnerung). */
@Composable
private fun BenachrichtigungenErlauben() {
    if (Build.VERSION.SDK_INT < 33) return
    val context = LocalContext.current
    val anfrage = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        val erlaubt = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!erlaubt && !Einstellungen.berechtigungGefragt(context)) {
            Einstellungen.merkeBerechtigungGefragt(context)
            anfrage.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
