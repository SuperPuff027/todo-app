package de.superpuff.todo.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.material3.ColorProviders
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import de.superpuff.todo.MainActivity
import de.superpuff.todo.R
import de.superpuff.todo.daten.Aufgabe
import de.superpuff.todo.daten.Aufgaben
import de.superpuff.todo.logik.Kalender
import de.superpuff.todo.ui.DunkleFarben
import de.superpuff.todo.ui.HelleFarben
import java.time.LocalDate

private val WidgetFarben = ColorProviders(light = HelleFarben, dark = DunkleFarben)

/** Homescreen-Widget: offene Aufgaben von heute, direkt abhakbar. */
class AufgabenWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val heute = LocalDate.now()
        // Direkt über die Datenbank, nicht über Aufgaben.uebertragen(): das würde das Widget erneut aktualisieren.
        Aufgaben.dao(context).uebertrage(heute.toEpochDay())
        val fluss = Aufgaben.dao(context).offeneAmTag(heute.toEpochDay())
        provideContent {
            val aufgaben by fluss.collectAsState(initial = emptyList())
            GlanceTheme(colors = WidgetFarben) {
                WidgetInhalt(heute, aufgaben)
            }
        }
    }
}

class AufgabenWidgetEmpfaenger : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AufgabenWidget()
}

private val AufgabenId = ActionParameters.Key<Long>("aufgabe_id")

/** Wird beim Tippen auf den runden Haken im Widget ausgeführt. */
class ErledigenAktion : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val id = parameters[AufgabenId] ?: return
        Aufgaben.erledigen(context, id)
    }
}

@Composable
private fun WidgetInhalt(heute: LocalDate, aufgaben: List<Aufgabe>) {
    val context = LocalContext.current
    val kompakt = LocalSize.current.height < 130.dp

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(24.dp)
            .background(GlanceTheme.colors.background)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = GlanceModifier.defaultWeight().clickable(actionStartActivity<MainActivity>()),
            ) {
                Text(
                    text = if (kompakt) "${aufgaben.size} offen" else Kalender.wochentag(heute),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    maxLines = 1,
                )
                if (!kompakt) {
                    Text(
                        text = Kalender.tagUndMonat(heute),
                        style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 13.sp),
                        maxLines = 1,
                    )
                }
            }
            RunderKnopf(
                symbol = R.drawable.ic_mikrofon,
                beschreibung = "Per Sprache eintragen",
                intent = Intent(context, SchnellEingabeActivity::class.java)
                    .setAction("de.superpuff.todo.SCHNELL_SPRACHE")
                    .putExtra(SchnellEingabeActivity.MIT_SPRACHE, true)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
                betont = false,
            )
            Spacer(GlanceModifier.width(8.dp))
            RunderKnopf(
                symbol = R.drawable.ic_plus,
                beschreibung = "Aufgabe eintragen",
                intent = Intent(context, SchnellEingabeActivity::class.java)
                    .setAction("de.superpuff.todo.SCHNELL_TEXT")
                    .putExtra(SchnellEingabeActivity.MIT_SPRACHE, false)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
                betont = true,
            )
        }

        if (!kompakt) {
            Spacer(GlanceModifier.height(10.dp))
            if (aufgaben.isEmpty()) {
                Box(
                    modifier = GlanceModifier.fillMaxSize().clickable(actionStartActivity<MainActivity>()),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Alles erledigt",
                        style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 14.sp),
                    )
                }
            } else {
                LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                    items(aufgaben, itemId = { it.id }) { aufgabe ->
                        WidgetZeile(aufgabe)
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetZeile(aufgabe: Aufgabe) {
    val label = Kalender.offenLabel(Kalender.tageOffen(aufgabe.datum, aufgabe.urspruenglichesDatum, aufgabe.erstelltAm))
    Column(modifier = GlanceModifier.fillMaxWidth().padding(bottom = 6.dp)) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .cornerRadius(16.dp)
                .background(GlanceTheme.colors.secondaryContainer)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_kreis),
                contentDescription = "Erledigt: ${aufgabe.text}",
                colorFilter = ColorFilter.tint(GlanceTheme.colors.primary),
                modifier = GlanceModifier
                    .size(26.dp)
                    .clickable(actionRunCallback<ErledigenAktion>(actionParametersOf(AufgabenId to aufgabe.id))),
            )
            Spacer(GlanceModifier.width(10.dp))
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = aufgabe.text,
                    style = TextStyle(color = GlanceTheme.colors.onSecondaryContainer, fontSize = 14.sp),
                    maxLines = 2,
                )
                if (label != null) {
                    Text(
                        text = label,
                        style = TextStyle(color = GlanceTheme.colors.tertiary, fontSize = 11.sp),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun RunderKnopf(symbol: Int, beschreibung: String, intent: Intent, betont: Boolean) {
    Box(
        modifier = GlanceModifier
            .size(40.dp)
            .cornerRadius(20.dp)
            .background(if (betont) GlanceTheme.colors.primary else GlanceTheme.colors.secondaryContainer)
            .clickable(actionStartActivity(intent)),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            provider = ImageProvider(symbol),
            contentDescription = beschreibung,
            colorFilter = ColorFilter.tint(
                if (betont) GlanceTheme.colors.onPrimary else GlanceTheme.colors.onSecondaryContainer,
            ),
            modifier = GlanceModifier.size(22.dp),
        )
    }
}
