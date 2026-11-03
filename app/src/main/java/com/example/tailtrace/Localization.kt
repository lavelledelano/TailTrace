package com.example.tailtrace

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/** Keeps the real Activity underneath, but gives it translated resources. */
private class LocaleContext(base: Context, private val localized: Resources) : ContextWrapper(base) {
    override fun getResources(): Resources = localized
}

@Composable
fun LocalizedApp(lang: String, content: @Composable () -> Unit) {
    val base = LocalContext.current
    val baseConfig = LocalConfiguration.current
    val ctx = remember(lang, base, baseConfig) {
        val cfg = android.content.res.Configuration(baseConfig)
        cfg.setLocale(Locale.forLanguageTag(lang))
        LocaleContext(base, base.createConfigurationContext(cfg).resources)
    }
    CompositionLocalProvider(
        LocalContext provides ctx,
        LocalConfiguration provides ctx.resources.configuration,
    ) {
        content()
    }
}