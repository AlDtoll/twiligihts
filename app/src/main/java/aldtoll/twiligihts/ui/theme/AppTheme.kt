package aldtoll.twiligihts.ui.theme

import aldtoll.twiligihts.R
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource

/**
 * Compose-тема, зеркалирующая View-тему Theme.Twiligihts
 * (res/values/themes.xml и res/values-night/themes.xml).
 * Цвета берутся из тех же @color-ресурсов — один источник правды.
 * Типографика и формы — дефолтные Material3.
 *
 * Соответствие атрибутов View-темы ролям M3:
 * colorPrimary -> primary, colorOnPrimary -> onPrimary,
 * colorPrimaryVariant -> primaryContainer, colorSecondary -> secondary,
 * colorOnSecondary -> onSecondary, colorSecondaryVariant -> secondaryContainer.
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) darkScheme() else lightScheme(),
        content = content
    )
}

@Composable
private fun lightScheme(): ColorScheme = lightColorScheme(
    primary = colorResource(R.color.purple_500),
    onPrimary = colorResource(R.color.white),
    primaryContainer = colorResource(R.color.purple_700),
    secondary = colorResource(R.color.teal_200),
    onSecondary = colorResource(R.color.black),
    secondaryContainer = colorResource(R.color.teal_700),
)

@Composable
private fun darkScheme(): ColorScheme = darkColorScheme(
    primary = colorResource(R.color.purple_200),
    onPrimary = colorResource(R.color.black),
    primaryContainer = colorResource(R.color.purple_700),
    secondary = colorResource(R.color.teal_200),
    onSecondary = colorResource(R.color.black),
    secondaryContainer = colorResource(R.color.teal_200),
)
