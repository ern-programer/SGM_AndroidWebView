# SGM Android WebView — Instrucciones para el agente

## Qué es esto

Este repositorio contiene una **aplicación Android genérica tipo WebView**. Su único trabajo es abrir una URL en pantalla fullscreen dentro de un WebView, como si fuera un navegador embebido. No tiene lógica de negocio propia — toda la inteligencia vive en el servidor web al que apunta.

## Para qué sirve

Ernespock (el usuario) desarrolla **sistemas web** (HTML + JS + Node.js + SQLite) que se ejecutan en una PC local de la red. Necesita acceder a esos sistemas desde **tablets y celulares Android** sin abrir Chrome. Esta app es el envoltorio: una WebView con configuración de IP/puerto, detección de red y pantalla de error.

## Patrón de trabajo de Ernespock

1. Desarrolla un sistema web completo (server.js + app.js + index.html) en un PC de la red
2. Levanta el servidor (ej: `node server.js` en `192.168.0.113:3000`)
3. Clona este repo, le cambia la URL por defecto, el nombre, los íconos y el paquete
4. Compila el APK con `./gradlew assembleDebug`
5. Distribuye el APK por WhatsApp a su equipo (6-7 personas)
6. Los usuarios instalan el APK, la app conecta al servidor y listo

**Este repo es la plantilla base.** Cada vez que Ernespock tiene un sistema web nuevo, copia este repo, le pone la URL/IP del nuevo servidor, y genera un APK.

## Arquitectura

```
app/src/main/java/com/gammasys/sgm/
├── MainActivity.java      ← WebView fullscreen con detección de red
│                            Carga la URL desde SharedPreferences
│                            Muestra pantalla de error si no hay red o el server no responde
│                            Botón ⚙️ flotante para abrir Settings
│                            Barra de progreso mientras carga
│
└── SettingsActivity.java  ← Pantalla para configurar IP y puerto del servidor
                             Guarda en SharedPreferences ("sgm_prefs")
                             Botón "Restaurar" para volver a la IP/puerto por defecto
```

### Flujo de la app

```
App inicia → lee IP/puerto de SharedPreferences
  → hay red? → carga http://IP:PUERTO en WebView
  → sin red? → muestra pantalla de error con botones Reintentar / Configurar
  → server no responde? → muestra error similar
  → usuario toca ⚙️ → SettingsActivity → vuelve → recarga automáticamente (onResume)
```

## Archivos clave

| Archivo | Qué hace |
|---------|----------|
| `MainActivity.java` | WebView fullscreen, setup de WebSettings (JS, DOM storage, zoom), carga URL, manejo de errores de red |
| `SettingsActivity.java` | Edita IP y puerto del servidor, guarda en SharedPreferences |
| `app/build.gradle` | Configuración del módulo: package, SDK versions, versionCode/Name |
| `res/values/strings.xml` | Nombre de la app (app_name) |
| `res/values/colors.xml` | Colores del tema |
| `res/values/themes.xml` | Tema Material Design |
| `res/layout/activity_main.xml` | Layout: WebView + barra progreso + pantalla error + botones |
| `res/layout/activity_settings.xml` | Layout: campos IP, puerto, botones Guardar/Restaurar |
| `res/mipmap-*/` | Íconos de la app (mdpi, hdpi, xhdpi, xxhdpi) |
| `AndroidManifest.xml` | Permisos (INTERNET, ACCESS_NETWORK_STATE), orientación, tema |

## Qué modificar para un proyecto nuevo

Cuando se usa esta plantilla para un sistema web distinto:

1. **URL por defecto** → `MainActivity.java` línea `DEFAULT_URL`, y `SettingsActivity.java` líneas `DEFAULT_IP` / `DEFAULT_PORT`
2. **Nombre de la app** → `res/values/strings.xml` → `app_name`
3. **Íconos** → reemplazar PNGs en `res/mipmap-*/ic_launcher.png` y `ic_launcher_round.png` (4 densidades)
4. **Paquete Java** → `app/build.gradle` → `applicationId` y `namespace`, más renombrar el directorio `java/com/gammasys/sgm/`
5. **Colores del tema** → `res/values/colors.xml` y `res/values/themes.xml`
6. **Versión** → `app/build.gradle` → `versionCode` (entero, subir cada release) y `versionName` (string legible)

## WebSettings configurados

La WebView tiene:
- **JavaScript habilitado** (obligatorio para cualquier SPA/modern web app)
- **DOM Storage** habilitado (localStorage/sessionStorage)
- **Database** habilitado (para apps que usan Web SQL)
- **Wide viewport** + load with overview mode (para que el contenido se adapte a la pantalla)
- **Zoom** con controles pero sin los botones visibles (pinch-to-zoom nativo)
- **File access deshabilitado** (seguridad: la app no puede leer archivos locales)
- **Content access deshabilitado** (seguridad)
- **Cache mode LOAD_DEFAULT** (usa cache del navegador normalmente)

## Permisos

Solo dos permisos, ambos normales (no requieren runtime permission):
- `INTERNET` — para conectar al servidor
- `ACCESS_NETWORK_STATE` — para detectar si hay WiFi/datos antes de intentar

## Build

```bash
# Debug APK (para testing y distribución interna)
./gradlew assembleDebug
# Salida: app/build/outputs/apk/debug/app-debug.apk

# Release APK (requiere signing config)
./gradlew assembleRelease
```

**Requisitos de build:**
- JDK 17
- Android SDK con compileSdk 34
- Gradle 8.x (wrapper incluido en el repo)
- Sin dependencias externas de terceros (solo AndroidX)

## Distribución

El equipo de Ernespock son 6-7 personas con tablets/Android. Se distribuye el APK por WhatsApp. No hay Play Store. Los usuarios habilitan "Fuentes desconocidas" en sus dispositivos e instalan directamente.

## Notas para el agente

- **No tocar la URL/IP hardcodeada** salvo que Ernespock lo pida explícitamente. La URL por defecto es `192.168.0.113:3000` pero los usuarios la cambian desde la pantalla de configuración de la app.
- **No agregar funcionalidad nativa** salvo que se pida. Esta app es un wrapper WebView, no una app nativa. Toda la lógica vive en el servidor web.
- **Los íconos son placeholders de Gammasys.** Si Ernespock pone un proyecto nuevo, hay que cambiarlos.
- **No usar dependencias innecesarias.** La app ya funciona con solo AndroidX. No agregar librerías de terceros.
- **SharedPreferences key = "sgm_prefs"**, campos: `server_ip` (string) y `server_port` (string). Si se agregan más settings, mantener la misma SharedPreferences.
- **Orientación:** la app está en `screenOrientation="portrait"` en el manifest. Si el sistema web necesita landscape, hay que cambiarlo ahí.
- **El `onResume` recarga la URL** — esto es intencional: cuando el usuario vuelve de Settings, la app reconecta con la nueva IP sin necesidad de botón.
