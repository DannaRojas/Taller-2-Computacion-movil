# RutaFoto — Taller 2 de Computación móvil

Aplicación Android en Kotlin y Jetpack Compose para fotografiar un recorrido.
En pantallas amplias muestra cámara y mapa lado a lado; en pantallas estrechas,
uno sobre otro. Incluye CameraX, Mapbox, ubicación y permisos, galería,
miniaturas horizontales, marcadores fotográficos y zoom de fotos.

## Abrir y ejecutar

1. Abre esta carpeta como proyecto en Android Studio.
2. Espera la sincronización de Gradle y selecciona el módulo `app`.
3. Selecciona un emulador con Google Play Services, por ejemplo Pixel 10 Pro Fold.
4. Pulsa Run y concede cámara y ubicación precisa.
5. Activa ubicación e introduce tu token público `pk...` en **Configurar Mapbox**.
   Se conserva durante la sesión. No hay un token incluido en el repositorio.

También puedes copiar `mapbox.properties.example` a `mapbox.properties` y
poner tu token allí antes de compilar. Ese archivo y `local.properties` están
excluidos de Git. El SDK local se configura desde Android Studio.

## Simular el recorrido tú misma

La simulación se controla desde el emulador. La aplicación registra las
posiciones recibidas; pulsar **Iniciar** empieza el registro.

1. En la barra del emulador pulsa **⋮** para abrir **Extended Controls**.
2. Entra en **Location**. Usa **Load GPX/KML** o la opción de importar una ruta,
   según la interfaz de tu emulador.
3. Abre [simulacion/Recorrido-prueba.gpx](simulacion/Recorrido-prueba.gpx).
4. Inicia la reproducción con **Play**. Espera la primera posición en RutaFoto.
5. En RutaFoto pulsa **Iniciar** mientras se reproduce.
6. Verás aumentar los metros y dibujarse la línea. Toma fotos durante la marcha.
7. **Pausar** en RutaFoto detiene el registro. El control de reproducción del
   emulador detiene el movimiento GPS. Puedes volver a reproducir la ruta.

El GPX es un trayecto sintético de prueba con 20 puntos, separados por
5 segundos y aproximadamente 10 metros. No es una ruta real recomendada.
Para cambiar su trazado edita `lat` (latitud) y `lon` (longitud); los elementos
`time` ordenan los puntos en el tiempo. La velocidad de reproducción también
puede ajustarse en los controles de ubicación cuando esa opción esté disponible.

Para una prueba sencilla también puedes enviar posiciones individuales desde
**Location**: usa dos coordenadas distintas y observa cómo se registra el cambio.
No basta con mover el mapa con el ratón: eso mueve la vista, no la posición GPS.

## Dónde modificar el código

| Archivo | Qué controla |
| --- | --- |
| `app/src/main/java/edu/javeriana/rutafoto/data/LocationUpdates.kt` | Solicitud GPS: intervalo de 1000 ms y callbacks del proveedor. |
| `app/src/main/java/edu/javeriana/rutafoto/data/RouteViewModel.kt` | Inicio, pausa, borrado y registro; agrega un punto a partir de 3 m de diferencia. |
| `app/src/main/java/edu/javeriana/rutafoto/ui/MapPanel.kt` | Mapa, línea, marcadores, centrado y botones de zoom. |
| `app/src/main/java/edu/javeriana/rutafoto/ui/CameraPanel.kt` | Vista previa, cámaras y captura en galería. |
| `app/src/main/java/edu/javeriana/rutafoto/ui/PhotoViewer.kt` | Zoom y desplazamiento de fotografías. |
| `app/src/main/java/edu/javeriana/rutafoto/MainActivity.kt` | Permisos y distribución de los dos paneles. |
| `app/src/main/res/values/strings.xml` | Textos de la interfaz. |

Cambiar el intervalo GPS cambia la frecuencia solicitada de actualizaciones;
no hace caminar al emulador. Para eso debes reproducir posiciones.

## Base en el material de clase

- **M_CL_08**, apartado «Obtener la localización una vez»: indica que en el
  emulador se pueden simular datos o cargar un archivo `*.gpx`.
- **08-Localizacion**, `LocationViewModel.kt`: `FusedLocationProviderClient`,
  `LocationRequest`, intervalos y `LocationCallback`.
- **09-Mapas**, `LocationViewModel.kt` y `MapComponent.kt`: registro de posiciones,
  inicio/pausa, Mapbox, marcadores y polilínea.
- **07-Camara**: CameraX, vista previa y captura. **06**: permisos. **05**: ViewModel.

Los ejemplos originales se entregaron en `ejemplos_clase-main.zip` y los PDF
en `compu movil conetnido clases.zip`. No se publican sus copias en este repo.
Consulta [EXPLICACION_PROYECTO.txt](EXPLICACION_PROYECTO.txt) para la revisión
de requisitos, archivos y funciones, con la distinción entre material de
clase y adaptaciones propias. También están [LEEME.txt](LEEME.txt) y
[ESTADO_VALIDACION.txt](ESTADO_VALIDACION.txt).

## Versiones y compilación

CameraX **1.6.0**, Mapbox Compose **11.31.1**, Accompanist **0.37.3** y
Play Services Location **21.4.0**. Se autorizó Mapbox 11.31.1 porque
11.31.11 no está publicada; 11.31.1 coincide con el ejemplo de clase.

Desde Android Studio genera el APK con la opción de compilar APK del menú Build.
Resultado: `app/build/outputs/apk/debug/app-debug.apk`.

Con el JDK y SDK configurados también puedes ejecutar:

```text
.\gradlew.bat assembleDebug lintDebug
.\gradlew.bat connectedDebugAndroidTest
```

La versión 1.2 se probó en Pixel 10 Pro Fold con seis pruebas en cada pantalla.
CameraX usa `ImplementationMode.EMBEDDED` para respetar el recorte al plegar.
El APK y los archivos generados por Gradle están excluidos de Git.

## Interfaz sencilla — versión 1.7

Letra sans serif condensada del dispositivo, títulos cortos y dos paneles: Recorrido y Cámara.
El recorrido tiene botones con texto: Iniciar/Pausar/Continuar y Borrar.
El mapa muestra la ubicación sin el rótulo grande que tapaba los marcadores.
Las miniaturas del mapa son compactas y abren la foto con su nombre completo.
La cabecera es un título sencillo; se quitaron el banner y los números de sección.
Fondo negro y paneles oscuros sin bordes. Los controles usan azul y verde intenso, con textos claros y mapa nocturno. La ubicación se indica en azul y el recorrido en verde.
Con menos de 760 dp disponibles se apilan los paneles, con el mapa arriba.
La fuente se define en `ui/RutaFotoTypography.kt`, sin añadir bibliotecas.
