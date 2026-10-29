package de.superpuff.todo.erinnerung

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import de.superpuff.todo.MainActivity
import de.superpuff.todo.R
import de.superpuff.todo.logik.Kalender

object Benachrichtigung {
    private const val KANAL = "erinnerungen"
    private const val ID = 1

    fun kanalAnlegen(context: Context) {
        val kanal = NotificationChannel(
            KANAL,
            context.getString(R.string.kanal_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply { description = context.getString(R.string.kanal_beschreibung) }
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(kanal)
    }

    fun zeige(context: Context, offeneTexte: List<String>) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val oeffnen = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val vorschau = Kalender.vorschau(offeneTexte)
        val nachricht = NotificationCompat.Builder(context, KANAL)
            .setSmallIcon(R.drawable.ic_benachrichtigung)
            .setContentTitle(Kalender.erinnerungsTitel(offeneTexte.size))
            .setContentText(vorschau)
            .setStyle(NotificationCompat.BigTextStyle().bigText(offeneTexte.joinToString("\n") { "• $it" }))
            .setContentIntent(oeffnen)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(ID, nachricht)
        } catch (_: SecurityException) {
            // Berechtigung wurde zwischendurch entzogen.
        }
    }
}
