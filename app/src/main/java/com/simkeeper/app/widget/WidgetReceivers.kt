package com.simkeeper.app.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.material3.ColorProviders
import com.simkeeper.app.ui.theme.DarkColors
import com.simkeeper.app.ui.theme.LightColors

/** Widget neutrals follow the app's light and dark tokens. */
val WidgetColors = ColorProviders(light = LightColors, dark = DarkColors)

class AllSimsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AllSimsWidget()
}

class SingleSimWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SingleSimWidget()
}
