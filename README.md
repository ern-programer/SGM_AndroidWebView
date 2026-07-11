# SGM Android WebView

Aplicación Android genérica tipo **WebView** para acceder a sistemas web internos desde tablets y teléfonos.

## Características

- **WebView fullscreen** con JavaScript, DOM Storage y zoom habilitado
- **Pantalla de configuración** para cambiar la URL del servidor sin recompilar
- **Detección de red** con pantalla de error y reintento automático
- **Botón de configuración flotante** accesible desde la pantalla principal
- **Splash screen nativo** mientras carga el sistema web

## Estructura del proyecto

```
app/src/main/
├── java/com/gammasys/sgm/
│   ├── MainActivity.java      # WebView principal
│   └── SettingsActivity.java   # Configuración de URL
├── res/
│   ├── layout/                 # Layouts XML
│   ├── drawable/               # Recursos vectoriales
│   ├── mipmap-*/               # Íconos de app
│   └── values/                 # Colores, strings, temas
└── AndroidManifest.xml
```

## Uso para nuevos proyectos

1. **Cambiar el nombre del paquete** en `build.gradle` (app) y `AndroidManifest.xml`
2. **Cambiar la URL por defecto** en `MainActivity.java` → `DEFAULT_URL`
3. **Cambiar el nombre de la app** en `res/values/strings.xml`
4. **Cambiar los íconos** en `res/mipmap-*/` (tamaños: mdpi, hdpi, xhdpi, xxhdpi)
5. **Cambiar colores del tema** en `res/values/colors.xml` y `res/values/themes.xml`

### URL por defecto

```java
// MainActivity.java, línea ~33
private static final String DEFAULT_URL = "http://192.168.0.113:3000";
```

Los usuarios pueden cambiar la URL desde la app (botón ⚙️) sin necesidad de recompilar.

## Compilar

```bash
# Debug APK
./gradlew assembleDebug

# El APK queda en:
# app/build/outputs/apk/debug/app-debug.apk
```

## Requisitos

- Android Studio (o solo JDK + Android SDK para compilar desde terminal)
- Min SDK: 24 (Android 7.0)
- Target SDK: 34 (Android 14)
- Sin dependencias externas (solo AndroidX)

## Licencia

Uso interno Gammasys.
