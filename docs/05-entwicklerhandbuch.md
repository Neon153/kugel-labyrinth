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
| `./gradlew testDebugUnitTest` | Unit-Tests aller Module (JVM, kein Gerät nötig; die Tests liegen in `core`) |
| `./gradlew lintDebug` | Statische Analyse aller Module; Berichte unter `<modul>/build/reports/lint-results-debug.html` |
| `./gradlew assembleDebug` | Debug-APKs: `app/build/outputs/apk/debug/app-debug.apk` und `wear/build/outputs/apk/debug/wear-debug.apk` |
| `./gradlew :app:installDebug` | Handy-App auf angeschlossenes Handy installieren |
| `./gradlew :wear:installDebug` | Uhr-App auf verbundene Uhr installieren (siehe 5.10) |
| `./gradlew :core:testDebugUnitTest` | nur die Tests der Spiellogik |
| `./gradlew assembleRelease` | Release-APKs (minifiziert; Signatur siehe 5.7) |

Sind Handy und Uhr gleichzeitig verbunden, gezielt mit `adb -s <serial> install <apk>` installieren (Seriennummern: `adb devices`).

Unter Windows `gradlew.bat` statt `./gradlew` verwenden.

## 5.4 Projektstruktur

```
kugel-labyrinth/
├── .github/workflows/android.yml   CI: Tests, Lint, beide APKs
├── core/                           Gemeinsames Modul (Android-Bibliothek)
│   └── src/
│       ├── main/
│       │   ├── java/com/example/kugellabyrinth/
│       │   │   ├── Settings.kt     Difficulty, AssistMode, BallSpeed      (Android-frei)
│       │   │   ├── Maze.kt         Labyrinth, Wände, Zellenindex          (Android-frei)
│       │   │   ├── GameEngine.kt   Physik, Kollision, Hilfen, Zustände    (Android-frei)
│       │   │   ├── MazeLayout.kt   Platzierung rechteckig / im Kreis      (Android-frei)
│       │   │   ├── Format.kt       Zeitformat                             (Android-frei)
│       │   │   ├── GameView.kt     Zeichnen, Spielschleife, Touch, Overlays
│       │   │   ├── TiltInput.kt    Sensor, Filter, Kalibrierung
│       │   │   └── GamePrefs.kt    Einstellungen, Bestzeiten, Anzeigetexte
│       │   └── res/values/strings.xml   gemeinsame Texte
│       └── test/java/…             JUnit-Tests (MazeTest, GameEngineTest, MazeLayoutTest, FormatTest)
├── app/                            Handy-App
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/kugellabyrinth/
│       │   ├── MenuActivity.kt     Menü
│       │   └── GameActivity.kt     Spielbildschirm, Ergebnisdialog
│       └── res/                    Layouts, Texte, Farben, Styles, Icon, Backup-Regeln
├── wear/                           Uhr-App (Wear OS)
│   └── src/main/
│       ├── AndroidManifest.xml     standalone, uses-feature watch
│       ├── java/com/example/kugellabyrinth/wear/
│       │   ├── WearMenuActivity.kt Menü (Chips, −/+, Krone)
│       │   └── WearGameActivity.kt Spielbildschirm, Ergebnis-Overlay
│       └── res/                    Layouts, Texte, Styles, Chips, Icon, values-round/dimens
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
4. `assembleDebug` (Handy und Uhr)
5. Artefakte: `kugel-labyrinth-handy-apk`, `kugel-labyrinth-uhr-apk` sowie `berichte` (Test- und Lint-Berichte aller Module, auch bei Fehlschlag)

GitHub Actions ist für private Repositories im Rahmen des Freikontingents kostenlos (Stand der Kontingente: siehe GitHub-Dokumentation).

## 5.6 Konventionen

- **Sprache:** Bezeichner auf Englisch, Kommentare, KDoc und Oberfläche auf Deutsch.
- **Module:** `app` und `wear` hängen von `core` ab, nie voneinander; `core` kennt weder `app` noch `wear`. Was beide Geräte brauchen, gehört nach `core`.
- **Schichten:** `Settings`, `Maze`, `GameEngine`, `MazeLayout` und `Format` dürfen **nichts** aus `android.*` importieren. Nur dann laufen sie in den JVM-Tests.
- **Ressourcen:** Wegen `android.nonTransitiveRClass` nutzen `app` und `wear` gemeinsame Texte über `import com.example.kugellabyrinth.core.R as CoreR`.
- **Keine Allokationen im Frame-Pfad** (`GameEngine.update/step/collide`, `GameView.doFrame/onDraw`): keine Lambdas mit Capture, kein Boxing, keine `String.format`, keine neuen Arrays.
- **Texte** nur über `strings.xml`. Texte mit wörtlichem `%` ohne Platzhalter erhalten `formatted="false"`, in Formatstrings wird `%%` verwendet.
- **Neue Konstanten** in das `companion object` der betroffenen Klasse und in die Parameterübersicht im [Fachkonzept 3.8](03-fachkonzept.md#38-parameterübersicht) eintragen.
- **Commits:** kurze Betreffzeile im Imperativ auf Deutsch, z. B. „Rollreibung als konstante Kraft modellieren“.

## 5.7 Release-Build

Für eine signierte Release-APK oder ein App Bundle:

1. Keystore erzeugen (einmalig, sicher aufbewahren, **nicht** ins Repository):
   `keytool -genkeypair -v -keystore release.jks -alias kugel -keyalg RSA -keysize 2048 -validity 10000`
2. In Android Studio: *Build → Generate Signed App Bundle / APK* und den Keystore auswählen.
3. Für den Play Store vorher den Paketnamen `com.example.kugellabyrinth` durch einen eigenen ersetzen: `applicationId` in `app` **und** `wear` (gleich lassen), die `namespace`-Einträge aller drei Module und die Paketnamen der Quelltexte.

Der Release-Build ist mit R8 minifiziert (`isMinifyEnabled`, `isShrinkResources`). Da keine Reflexion verwendet wird, sind keine eigenen ProGuard-Regeln nötig.

## 5.8 Versionierung und Änderungen

- `versionCode` (ganzzahlig, bei jeder Auslieferung +1) und `versionName` (SemVer) stehen in `app/build.gradle.kts` und `wear/build.gradle.kts`.
- **versionCode der Uhr = 100 000 + versionCode der Handy-App** (z. B. 5 ↔ 100 005). Beide Apps haben dieselbe `applicationId`; im Play Store müssen ihre versionCodes verschieden sein ([ADR-015](06-entscheidungen.md#adr-015-gleiche-applicationid-und-versioncode-versatz)).
- `versionName` ist für Handy und Uhr gleich.
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
1. Eintrag in `enum class Difficulty` (`core/…/Settings.kt`) mit Handy-Spalten, Uhr-Seitenlänge (`watchCols`) und Hilfestärke
2. Text `difficulty_…` in `core/src/main/res/values/strings.xml` und `labelRes()` in `GamePrefs.kt`
3. Handy: Text `menu_…` in `app/…/strings.xml`, `RadioButton` in `activity_menu.xml` und Zuordnung in `MenuActivity` (`check(...)` und `selectedDifficulty()`)
4. Uhr: nichts zu tun – der Stufen-Chip schaltet über `Difficulty.entries` durch.
5. Die Tests iterieren über `Difficulty.entries` und decken die neue Stufe automatisch ab, auch die Uhr-Größe.

## 5.10 Uhr-App entwickeln und testen

**Emulator:** In Android Studio im *Device Manager* ein Gerät der Kategorie *Wear OS* anlegen (z. B. „Wear OS Large Round“) und ein Wear-OS-System-Image wählen. Die Neigung lässt sich unter *Extended Controls → Virtual sensors → Device Pose* simulieren.

**Echte Uhr (empfohlen):** WLAN-Debugging wie im [Benutzerhandbuch 4.8](04-benutzerhandbuch.md#48-uhr-app-pixel-watch-5) beschrieben einrichten. Danach erscheint die Uhr in Android Studio als Zielgerät; die Run-Konfiguration **wear** wählen. Über die Kommandozeile: `./gradlew :wear:installDebug` bzw. `adb -s <IP:Port> install wear/build/outputs/apk/debug/wear-debug.apk`.

**Worauf beim Uhr-Code achten:**
- Inhalte am runden Rand werden abgeschnitten: Abstände in `wear/src/main/res/values-round/dimens.xml`, Labyrinth über `MazeLayout.squareInCircle`.
- Dialoge vermeiden (auf runden Displays abgeschnitten), Ergebnisse als Overlay im Spielfeld zeigen.
- Touch-Ziele mindestens 48 dp. Das Spielfeld nutzt die Standard-Touchauswertung der View (Klick, langes Drücken).
- Zum Akkusparen: schwarzer Hintergrund, Sensor nur zwischen `onResume` und `onPause`.
- Wischen nach rechts schließt eine Activity (System-Geste von `Theme.DeviceDefault` auf Wear OS).
