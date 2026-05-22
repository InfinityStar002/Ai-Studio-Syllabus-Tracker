package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val BlueDarkColorScheme = darkColorScheme(primary = Blue80, secondary = BlueGrey80, tertiary = LightBlue80)
private val BlueLightColorScheme = lightColorScheme(primary = Blue40, secondary = BlueGrey40, tertiary = LightBlue40)

private val GreenDarkColorScheme = darkColorScheme(primary = Green80, secondary = GreenGrey80, tertiary = LightGreen80)
private val GreenLightColorScheme = lightColorScheme(primary = Green40, secondary = GreenGrey40, tertiary = LightGreen40)

private val PurpleDarkColorScheme = darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)
private val PurpleLightColorScheme = lightColorScheme(primary = Purple40, secondary = PurpleGrey40, tertiary = Pink40)

private val OrangeDarkColorScheme = darkColorScheme(primary = Orange80, secondary = OrangeGrey80, tertiary = DeepOrange80)
private val OrangeLightColorScheme = lightColorScheme(primary = Orange40, secondary = OrangeGrey40, tertiary = DeepOrange40)

@Composable
fun MyApplicationTheme(
  themeMode: Int = 0, // 0=System, 1=Light, 2=Dark
  colorPalette: Int = 0, // 0=Blue, 1=Green, 2=Purple, 3=Orange
  content: @Composable () -> Unit,
) {
  val darkTheme = when(themeMode) {
    1 -> false
    2 -> true
    else -> isSystemInDarkTheme()
  }

  val colorScheme = when (colorPalette) {
    1 -> if (darkTheme) GreenDarkColorScheme else GreenLightColorScheme
    2 -> if (darkTheme) PurpleDarkColorScheme else PurpleLightColorScheme
    3 -> if (darkTheme) OrangeDarkColorScheme else OrangeLightColorScheme
    else -> if (darkTheme) BlueDarkColorScheme else BlueLightColorScheme
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
