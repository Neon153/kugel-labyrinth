# 5 Entwicklerhandbuch

## 5.1 Voraussetzungen

| Werkzeug | Version |
|---|---|
| Android Studio | Ladybug (2024.2) oder neuer |
| JDK | 17 (in Android Studio enthalten) |
| Android SDK Platform | 35 (Android 15) |
| Gradle | 8.9 (über Wrapper, wird automatisch geladen) |
| Android Gradle Plugin | 8.7.3 |
| Kotlin | 2.0.21 |

## 5.2 Projekt öffnen und starten

1. Repository klonen: `git clone https://github.com/Neon153/kugel-labyrinth.git`
2. Android Studio → **File → Open** → Ordner `kugel-labyrinth`
3. Gradle-Sync abwarten. Fehlt die SDK Platform 35, bietet Android Studio die Installation an.
4. Handy per USB anschließen (Entwickleroptionen + USB-Debugging aktiv) und **Run ▶** drücken.

Im Emulator lässt sich die Neigung unter *Extended Controls → Virtual sensors → Device Pose* simulieren. Zum Testen des Spielgefühls ist ein echtes Gerät deutlich besser.

## 5.3 Kommandozeile

| Befehl | Zweck |
|---|---|
| `./gradlew testDebugUnitTest` | Unit-Tests (JVM, kein Gerät nötig) |
| `./gradlew lintDebug` | Statische Analyse; Bericht unter `app/build/reports/lint-results-debug.html` |
| `./gradlew assembleDebug` | Debug-APK unter `app/build/outputs/apk/debug/` |
| `./gradlew installDebug` | APK auf angeschlossenes Gerät installieren |
| `./gradlew assembleRelease` | Release-APK (minifiziert; Signatur siehe 5.7) |

Unter Windows `gradlew.bat` statt `./gradlew` verwenden.

## 5.4 Projektstruktur

```
kugel-labyrinth/
├── .github/workflows/android.yml   CI: Tests, Lint, APK
├── app/
│   ├── build.gradle.kts            Modul-Build (SDK-Versionen, Release-Optionen)
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/example/kugellabyrinth/
│       │   │   ├── Settings.kt     Difficulty, AssistMode, BallSpeed      (Android-frei)
│       │   │   ├── Maze.kt         Labyrinth, Wände, Zellenindex          (Android-frei)
│       │   │   ├── GameEngine.kt   Physik, Kollision, Hilfen, Zustände    (Android-frei)
│       │   │   ├── Format.kt       Zeitformat                             (Android-frei)
│       │   │   ├── GameView.kt     Zeichnen, Spielschleife, Touch
│       │   │   ├── TiltInput.kt    Sensor, Filter, Kalibrierung
│       │   │   ├── GameActivity.kt Spielbildschirm
│       │   │   ├── MenuActivity.kt Menü
│       │   │   └── GamePrefs.kt    Einstellungen, Bestzeiten
│       │   └── res/                Layouts, Texte, Farben, Styles, Icon, Backup-Regeln
│       └── test/java/…             JUnit-Tests (MazeTest, GameEngineTest, FormatTest)
├── docs/                           Diese Dokumentation
├── gradle/wrapper/                 Gradle-Wrapper
├── build.gradle.kts, settings.gradle.kts, gradle.properties
├── CHANGELOG.md
└── README.md
```

## 5.5 Continuous Integration

`.github/workflows/android.yml` läuft bei jedem Push auf `main`, bei Pull Requests und manuell (*Actions → Android CI → Run workflow*):

1. Checkout, JDK 17, Gradle (inkl. Prüfung der Wrapper-Prüfsumme, Cache)
2. `testDebugUnitTest`
3. `lintDebug` (bricht bei Lint-**Fehlern** ab; Warnungen nicht)
4. `assembleDebug`
5. Artefakte: `kugel-labyrinth-debug-apk` sowie `berichte` (Test- und Lint-Berichte, auch bei Fehlschlag)

GitHub Actions ist für private Repositories im Rahmen des Freikontingents kostenlos (Stand der Kontingente: siehe GitHub-Dokumentation).

## 5.6 Konventionen

- **Sprache:** Bezeichner auf Englisch, Kommentare, KDoc und Oberfläche auf Deutsch.
- **Schichten:** `Settings`, `Maze`, `GameEngine` und `Format` dürfen **nichts** aus `android.*` importieren. Nur dann laufen sie in den JVM-Tests.
- **Keine Allokationen im Frame-Pfad** (`GameEngine.update/step/collide`, `GameView.doFrame/onDraw`): keine Lambdas mit Capture, kein Boxing, keine `String.format`, keine neuen Arrays.
- **Texte** nur über `strings.xml`. Texte mit wörtlichem `%` ohne Platzhalter erhalten `formatted="false"`, in Formatstrings wird `%%` verwendet.
- **Neue Konstanten** in das `companion object` der betroffenen Klasse und in die Parameterübersicht im [Fachkonzept 3.8](03-fachkonzept.md#38-parameterübersicht) eintragen.
- **Commits:** kurze Betreffzeile im Imperativ auf Deutsch, z. B. „Rollreibung als konstante Kraft modellieren“.

## 5.7 Release-Build

Für eine signierte Release-APK oder ein App Bundle:

1. Keystore erzeugen (einmalig, sicher aufbewahren, **nicht** ins Repository):
   `keytool -genkeypair -v -keystore release.jks -alias kugel -keyalg RSA -keysize 2048 -validity 10000`
2. In Android Studio: *Build → Generate Signed App Bundle / APK* und den Keystore auswählen.
3. Für den Play Store vorher den Paketnamen `com.example.kugellabyrinth` (in `namespace` und `applicationId`) durch einen eigenen ersetzen.

Der Release-Build ist mit R8 minifiziert (`isMinifyEnabled`, `isShrinkResources`). Da keine Reflexion verwendet wird, sind keine eigenen ProGuard-Regeln nötig.

## 5.8 Versionierung und Änderungen

- `versionCode` (ganzzahlig, bei jeder Auslieferung +1) und `versionName` (SemVer) stehen in `app/build.gradle.kts`.
- Jede Version erhält einen Eintrag in `CHANGELOG.md` und ein Git-Tag `vX.Y.Z`.
- Architekturrelevante Entscheidungen als neuen Eintrag in [06-entscheidungen.md](06-entscheidungen.md) festhalten.

## 5.9 Typische Erweiterungen

**Spielgefühl anpassen:** Die Stellgrößen stehen im `companion object` von `GameEngine.kt`:

| Ziel | Konstante |
|---|---|
| Kugel generell schneller/langsamer | `TILT_TO_ACCEL`, `MAX_SPEED` (oder Tempo-Regler im Menü) |
| Länger ausrollen | `BASE_DAMP` verkleinern |
| Stärkeres Abprallen | `WALL_BOUNCE` |
| Software-Hilfe stärker/schwächer | `REDIRECT`, `CENTER_ACCEL`, `LATERAL_DAMP` |
| Sensor ruhiger/direkter | `TiltInput.FILTER_TAU` |

Nach Änderungen `./gradlew testDebugUnitTest` ausführen. Die Tests prüfen unter anderem, dass die Kugel bei allen Kombinationen keine Wand durchdringt.

**Neue Schwierigkeitsstufe:**
1. Eintrag in `enum class Difficulty` (`Settings.kt`) mit Spalten und Hilfestärke
2. Text in `strings.xml` (`difficulty_…`, `menu_…`) und `labelRes()` in `GamePrefs.kt`
3. `RadioButton` in `activity_menu.xml` und Zuordnung in `MenuActivity` (`check(...)` und `selectedDifficulty()`)
4. Die Tests iterieren über `Difficulty.entries` und decken die neue Stufe automatisch ab.
