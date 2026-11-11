# 7 Testdokumentation

## 7.1 Teststrategie

| Ebene | Was | Werkzeug | Automatisiert | Status |
|---|---|---|---|---|
| Unit-Tests | Labyrinth, Physik, Kollision, Hilfen, Tempo, Zeitformat, Platzierung auf Handy und Uhr | JUnit 4.13.2 auf der JVM (Modul `core`) | ✔ (lokal + CI) | 27/27 bestanden |
| Statische Prüfung | Jedes Modul (`core`, `app`, `wear`) einzeln mit Warnungen als Fehler kompiliert; Ressourcen-Referenzen und Formatplatzhalter je Modul | Kotlin-Compiler `-Werror`, Prüfskript | ✔ (lokal) | bestanden |
| Build + Lint | Echter Android-Build mit SDK 35, Android Lint | Gradle, GitHub Actions | ✔ (CI) | bestanden (7.3) |
| Gerätetest Handy | Sensor, Spielgefühl, Lebenszyklus, Darstellung | manuell, Testplan 7.4 (MT) | ✘ | **offen** |
| Gerätetest Uhr | Armsteuerung, Lesbarkeit, Krone, Gesten, Display-Verhalten, Akku | manuell, Testplan 7.4 (MW) | ✘ | **offen** |

Die Spiellogik ist bewusst Android-frei ([ADR-002](06-entscheidungen.md#adr-002-spiellogik-ohne-android-abhängigkeiten)). Deshalb lassen sich ganze Spielverläufe mit einem simulierten Spieler testen.

**Test-Bot:** Ein PD-Regler zielt auf die Mitte der nächsten Zelle des Lösungswegs, die Neigung ist wie bei einem Menschen auf 4 m/s² (≈ 24°) begrenzt. Er rechnet die gewünschte Beschleunigung über die tatsächliche Verstärkung (`accelPerTilt`) und Totzone der Engine in eine Neigung um. Dadurch funktioniert er in jeder Stufe und bei jedem Tempo. In jedem Frame prüft er, dass die Kugel keine Wand durchdringt.

## 7.2 Automatisierte Testfälle

### `MazeTest` – Labyrinth (40 Labyrinthe je Test: 30 zufällige Größen 2–13 × 2–26 Zellen, die vier Handy-Stufengrößen, die vier Uhr-Größen 5 × 5 … 8 × 8 sowie 7 × 14 und 12 × 26)

| # | Test | Prüft | Anforderung |
|---|---|---|---|
| T-01 | `isPerfectMaze_allCellsReachable_noLoops` | Alle Zellen erreichbar; genau n − 1 Durchgänge (Spannbaum) | FA-02 |
| T-02 | `openings_areSymmetric_andNeverLeaveTheGrid` | Jede Öffnung besteht in beiden Zellen; keine Öffnung nach außen | FA-02, FA-03 |
| T-03 | `goal_isFarthestCellFromStart` | Ziel hat maximale BFS-Weglänge vom Start | FA-02 |
| T-04 | `wallRects_matchOpenings` | An jeder Zellkante liegt genau dann ein Wand-Rechteck, wenn sie geschlossen ist; Zellmitten sind frei | FA-03, NFA-03 |
| T-05 | `cellIndex_containsEveryWallTheBallCanTouch` | 2 000 Zufallspositionen je Labyrinth: jede Wand näher als der Kugelradius steht im Zellenindex | FA-03, NFA-06 |
| T-06 | `mergedWalls_areFewerThanCellEdges` | Zusammenfassung reduziert die Rechteckzahl | NFA-01 |

### `GameEngineTest` – Physik und Spiellogik

| # | Test | Prüft | Anforderung |
|---|---|---|---|
| T-07 | `everyCombination_atEverySpeed_isSolvable_withoutWallPenetration` | 4 Stufen × 2 Hilfe-Arten × 3 Tempi (50/100/200 %) × 2 Labyrinthe = 48 vollständige Spiele: Ziel wird erreicht, Kugel nie in einer Wand oder außerhalb | FA-01, FA-03, FA-04, FA-05, NFA-06 |
| T-08 | `noTilt_meansNoMovement_inEveryMode` | Ohne Neigung bleibt die Kugel in jeder Stufe und Hilfe-Art bei 200 % Tempo 10 s exakt liegen | FA-06, FA-07, FA-15 |
| T-09 | `tiltWithinDeadZone_doesNotMoveBall` | Neigung unterhalb der Totzone bewegt die Kugel nicht | FA-01 |
| T-10 | `ballIsFast_atDefaultSpeed` | Bei 100 % und 2 m/s² (≈ 12°) nach 0,15 s > 2 Zellen/s | NFA-02 |
| T-11 | `speedSetting_scalesAcceleration` | 200 % ergibt das 2,00-fache, 50 % das 0,50-fache der Geschwindigkeit von 100 % (Toleranz ±0,02) | FA-10 |
| T-12 | `speedFactor_isClampedToMenuRange_andStepMovementStaysSmall` | Tempo wird auf 0,5–2,0 begrenzt; Bewegung pro Schritt ≤ 0,15 und < halber Kugelradius in allen Stufen bei 200 % | FA-10, NFA-06 |
| T-13 | `proLevel_hasZeroAssist_regardlessOfAssistMode` | Bei „Profi“ verlaufen 20 s Zufallseingaben mit beiden Hilfe-Arten **bitgenau identisch**; Totzone = Grundwert | FA-08 |
| T-14 | `sensitivityMode_reactsWeaker_thanZeroAssist` | Empfindlichkeit: Leicht < Mittel < Schwer < Profi; Software-Hilfe beschleunigt nicht schwächer als 0 % | FA-05, FA-06, FA-07 |
| T-15 | `softwareMode_redirectsTiltIntoOpenCorridor` | Schräges Kippen gegen eine Wand: mit Software-Hilfe (Leicht) mehr Fortschritt im offenen Gang als bei 0 % | FA-07 |
| T-16 | `finishedFlag_isReportedExactlyOnce_andStateFreezes` | Ziel wird genau einmal gemeldet; danach keine Bewegung, Zeit steht | FA-04 |
| T-17 | `randomMaxTilt_atMaxSpeed_withLagSpikes_neverPenetratesWalls` | 5 × 4 000 Frames zufälliges Vollkippen (±8 m/s²) bei „Profi“ und 200 %, zufällig etwa jeder 10. Frame ein 200-ms-Ruckler: nie in einer Wand | FA-03, NFA-06 |
| T-18 | `reset_restoresStartState` | Neustart setzt Position, Zeit und Zustand zurück; vor `start()` keine Bewegung | FA-13 |
| T-19 | `ballSpeed_menuValues` | Tempo-Raster: Begrenzung auf 50/200, Rundung auf 10er-Schritte | FA-10 |
| T-20 | `benchmark_physicsIsCheap` | Laufzeit pro 60-Hz-Frame bei „Schwer“, 200 %, Software-Hilfe < 200 µs (gemessen 0,64 µs) | NFA-01 |
| T-23 | `watchMazes_everyCombination_atEverySpeed_isSolvable_withoutWallPenetration` | Uhr-Labyrinthe: 4 Stufen (5 × 5 … 8 × 8) × 2 Hilfe-Arten × 3 Tempi × 2 Labyrinthe = 48 vollständige Spiele ohne Wanddurchdringung | FA-03, FA-18, NFA-06 |

### `MazeLayoutTest` – Platzierung auf Handy und Uhr

| # | Test | Prüft | Anforderung |
|---|---|---|---|
| T-24 | `fillRect_usesFullWidth_onTallScreens` | Handy: volle Breite, Zeilen aus der Höhe, vertikal zentriert | FA-02 |
| T-25 | `fillRect_shrinksCells_whenMinimumRowsDoNotFit` | Handy: bei sehr flacher Fläche mindestens 5 Zeilen, Zellen verkleinert, alles passt | FA-02 |
| T-26 | `squareInCircle_isSquare_centered_andInsideTheRoundDisplay` | Uhr (408, 456, 384 px und nicht quadratische Fläche), alle Stufen: quadratisch, zentriert, alle vier Ecken im Kreis, ≥ 95 % des Inkreis-Quadrats genutzt | NFA-14 |
| T-27 | `watchCells_areLargeEnough_onPixelWatch5_41mm` | 41-mm-Uhr, „Profi“: Zelle ≥ 32 px, Kugel ≥ 1,4 mm Durchmesser | NFA-14 |

### `FormatTest` – Zeitanzeige

| # | Test | Prüft | Anforderung |
|---|---|---|---|
| T-21 | `formatsWithGermanDecimalComma` | „0,0 s“, „12,3 s“, „59,9 s“, „123,0 s“ | FA-12, NFA-11 |
| T-22 | `physicsStepTimes_doNotLoseATenth` | Für **jede** Schrittanzahl bis 100 s wird die Zehntelsekunde korrekt angezeigt (keine Rundungsfehler) | FA-12 |

## 7.3 Testergebnisse

### Lokaler Lauf (Version 1.4.0)

| | |
|---|---|
| Datum | 11.11.2026 |
| Umgebung | OpenJDK 21, Kotlin 2.0.21, JUnit 4.13.2, Linux |
| Ausführung | Kotlin-Compiler + JUnitCore direkt (ohne Gradle, da in dieser Umgebung kein Android SDK verfügbar war) |
| Ergebnis | **27 von 27 bestanden**, Laufzeit 0,6 s |
| Messwert T-20 | 0,64 µs Physik pro 60-Hz-Frame (8 Schritte à 1/480 s) |

Zusätzlich wurden `core`, `app` und `wear` einzeln mit `-Werror` gegen eine Nachbildung der verwendeten Android-API kompiliert, ohne Fehler oder Warnungen. Ein Prüfskript hat je Modul alle `@…`-Referenzen in XML, alle `R.…`/`CoreR.…`-Referenzen im Code und die Anzahl der Formatargumente bei `getString` kontrolliert: ohne Befund.

### CI-Läufe (GitHub Actions)

| Datum | Lauf | Stand | Ergebnis |
|---|---|---|---|
| 30.10.2026 | 41128812406 | Version 1.3.0 (`6cdfa39`) | ✔ Unit-Tests, Lint, Handy-APK |
| 30.10.2026 | 41129414086 | aktualisierte Actions (`0a717ad`) | ✔ ohne Warnungen |
| 11.11.2026 | 43103924438 | Version 1.4.0, Branch `wear-os` (`97f28a4`) | ✔ Unit-Tests (alle Module), Lint (`core`, `app`, `wear`), Handy-APK (≈ 0,8 MB gepackt), Uhr-APK (≈ 0,8 MB gepackt) |

Umgebung: GitHub Actions `ubuntu-latest`, Temurin JDK 17, Gradle 8.9 (Wrapper, Prüfsumme validiert), Android SDK Platform 35. Damit sind beide Apps mit dem echten Android SDK kompiliert und gegen Android Lint geprüft, einschließlich der Wear-OS-spezifischen Lint-Regeln. Den aktuellen Stand zeigt das Badge in der [README](../README.md).

## 7.4 Manuelle Gerätetests

### Handy

Durchführung auf mindestens einem echten Gerät, idealerweise zusätzlich auf einem 120-Hz-Gerät und einem Gerät mit Android 15.

| ID | Testfall | Schritte | Erwartetes Ergebnis | Anf. | Status |
|---|---|---|---|---|---|
| MT-01 | Erststart | App installieren und öffnen | Menü erscheint; „Mittel“, „Software-Hilfe“, „100 % (Standard)“ vorgewählt | FA-09 | offen |
| MT-02 | Einstellungen bleiben | Auswahl ändern, App aus dem Verlauf wischen, neu öffnen | Letzte Auswahl ist vorgewählt | FA-14 | offen |
| MT-03 | 0-%-Stufe | „Profi“ wählen | Hilfe-Art ausgegraut und nicht wählbar; Infotext nennt 0 % | FA-08, FA-09 | offen |
| MT-04 | Tempo-Regler | Regler ganz links/rechts, Spiel starten | Anzeige 50 % bzw. 200 %; Kopfzeile im Spiel zeigt „Tempo …“; Kugel spürbar langsamer bzw. schneller | FA-10 | offen |
| MT-05 | Richtungen | Spiel starten, nacheinander rechts/links/oben/unten kippen | Kugel rollt jeweils zur abgesenkten Seite | FA-01 | offen |
| MT-06 | Kalibrierung schräg | Handy ca. 40° geneigt halten, antippen, dann seitlich kippen | Kugel bleibt zunächst liegen; seitliche Reaktion so stark wie bei flacher Haltung | FA-11 | offen |
| MT-07 | Empfindlichkeit | „Leicht“ + Empfindlichkeit, danach „Profi“ | Bei „Leicht“ deutlich trägere Reaktion; leichtes Zittern bewegt die Kugel nicht | FA-06 | offen |
| MT-08 | Software-Hilfe | „Leicht“ + Software-Hilfe; schräg in eine Kurve kippen; Handy ruhig halten | Kugel biegt leichter ab, prallt nicht ab; ohne Kippen bleibt sie liegen | FA-07, FA-15 | offen |
| MT-09 | Flüssigkeit | Entwickleroptionen → „Profil-GPU-Rendering“ als Balken; 1 min spielen | Keine sichtbaren Ruckler; Balken überwiegend unter der Frame-Linie | NFA-01 | offen |
| MT-10 | Ziel | Labyrinth bis ins Ziel spielen | Dialog mit Zeit und Bestzeit, beim ersten Mal „Neuer Rekord!“; alle drei Schaltflächen funktionieren | FA-04, FA-12, FA-13 | offen |
| MT-11 | Pause | Während des Spiels Home drücken, 10 s warten, zurückkehren | Zeit hat nicht weitergezählt; Spiel läuft weiter | NFA-13 | offen |
| MT-12 | Spielgefühl | Auftraggeber spielt jede Stufe bei 100 % und „Profi“ bei 150–200 % | Bewegung flüssig und zügig; „Profi“ ist fordernd | NFA-02, NFA-04, NFA-05 | offen |
| MT-13 | TalkBack | TalkBack an, Spielfeld fokussieren, Doppeltippen | Spielfeld wird angesagt; Doppeltippen startet das Spiel | NFA-10 | offen |
| MT-14 | Android 15 | Auf Android-15-Gerät Menü und Spiel öffnen | Inhalte nicht unter Status- oder Navigationsleiste | NFA-07 | offen |

### Uhr (Pixel Watch 5)

Durchführung auf der Pixel Watch 5 (41 mm oder 45 mm), Uhr am linken Handgelenk, sofern nicht anders angegeben.

| ID | Testfall | Schritte | Erwartetes Ergebnis | Anf. | Status |
|---|---|---|---|---|---|
| MW-01 | Installation und Start | Uhr-APK per WLAN-Debugging installieren, App aus der App-Liste starten | Menü erscheint mit Titel, Stufe, Hilfe-Art, Tempo, Start; nichts am Rand abgeschnitten | FA-16, NFA-14 | offen |
| MW-02 | Krone und Menü | Krone drehen; jeden Chip antippen; −/+ drücken | Menü scrollt mit der Krone; Stufe schaltet Leicht → … → Profi → Leicht; Hilfe-Art wechselt, bei Profi ausgegraut; Tempo 50–200 % | FA-18, FA-19 | offen |
| MW-03 | Einstellungen bleiben | Auswahl ändern, App schließen, neu öffnen | Letzte Auswahl angezeigt | FA-14, FA-18 | offen |
| MW-04 | Richtungen | Spiel starten; Display von dir weg / zu dir kippen (Unterarm drehen); Hand heben / senken | Kugel rollt Richtung 12 / 6 Uhr bzw. 9 / 3 Uhr; am rechten Handgelenk ebenfalls jeweils zur tieferen Displayseite | FA-17 | offen |
| MW-05 | Kalibrierung in Ablesehaltung | Arm angewinkelt, Display schräg zum Gesicht, antippen | Kugel bleibt liegen; beide Achsen reagieren gleich stark | FA-11, FA-17 | offen |
| MW-06 | Spielbarkeit je Stufe | Jede Stufe bei 100 % Tempo durchspielen | Kugel und Wände klar erkennbar; Ziel erreichbar; Bewegung flüssig | FA-18, NFA-14 | offen |
| MW-07 | Display bleibt an | Während des Spiels den Unterarm weit drehen und 30 s spielen | Display bleibt an und dunkelt nicht ab; falls doch: Spiel pausiert und läuft danach ohne Zeitsprung weiter | NFA-13, Risiko R-9 | offen |
| MW-08 | Gesten | Während des Spiels lang drücken; nach dem Ziel antippen bzw. lang drücken; nach rechts wischen | Neustart mit Haptik; neues bzw. gleiches Labyrinth; zurück zum Menü | FA-19 | offen |
| MW-09 | Ergebnis | Labyrinth bis ins Ziel spielen | Overlay mit Zeit, Bestzeit, beim ersten Mal „Neuer Rekord!“; Text vollständig im Kreis lesbar | FA-12, FA-19 | offen |
| MW-10 | Pause | Während des Spiels Seitentaste oder Krone drücken, 10 s warten, zurückkehren | Zeit hat nicht weitergezählt | NFA-13 | offen |
| MW-11 | Akku | Akkustand notieren, 10 min spielen | Verbrauch im üblichen Rahmen für eine aktive App mit Display an | NFA-15 | offen |
| MW-12 | Ohne Handy | Bluetooth am Handy aus, App starten und spielen | Funktioniert vollständig | FA-16 | offen |

**Protokoll:** Gerät, Android-/Wear-OS-Version, Bildrate, Datum, Ergebnis (✔ / ✘ + Beschreibung) je Testfall hier oder als GitHub-Issue festhalten.

## 7.5 Abdeckungsmatrix

| Anforderung | Umsetzung | Automatischer Nachweis | Manueller Nachweis |
|---|---|---|---|
| FA-01 Neigungssteuerung | `TiltInput`, `GameEngine.step` | T-07, T-09 | MT-05 |
| FA-02 Perfektes Labyrinth | `Maze.generate`, `Maze.bfs`, `MazeLayout.fillRect` | T-01, T-02, T-03, T-24, T-25 | – |
| FA-03 Keine Wanddurchdringung | `GameEngine.collide`, Zellenindex | T-02, T-04, T-05, T-07, T-17 | – |
| FA-04 Ziel und Spielende | `GameEngine.step`, `GameActivity.onFinished` | T-07, T-16 | MT-10 |
| FA-05 Stufen | `Difficulty` | T-07, T-14 | MT-07 |
| FA-06 Empfindlichkeit | `GameEngine.deadZone/accelPerTilt` | T-08, T-14 | MT-07 |
| FA-07 Software-Hilfe | `GameEngine.step` (Umlenkung, Zentrierung) | T-08, T-14, T-15 | MT-08 |
| FA-08 0 % Hilfe | `Difficulty.PRO` | T-13 | MT-03 |
| FA-09 Menü | `MenuActivity`, `activity_menu.xml` | – | MT-01, MT-03 |
| FA-10 Tempo-Regler | `BallSpeed`, `GameEngine.speedFactor` | T-11, T-12, T-19 | MT-04 |
| FA-11 Kalibrierung | `TiltInput.calibrate` | – | MT-06, MW-05 |
| FA-12 Zeit/Bestzeit | `GameEngine.elapsed`, `Format`, `GamePrefs` | T-21, T-22 | MT-10 |
| FA-13 Neustart/Neu/Menü | `GameView.restart/newMaze`, Dialog | T-18 | MT-10 |
| FA-14 Persistenz | `GamePrefs` | – | MT-02 |
| FA-15 Hilfe löst nicht selbst | `GameEngine.step` (nur bei Neigung) | T-08 | MT-08 |
| NFA-01 Flüssigkeit | `Choreographer`, Interpolation, Bitmap | T-06, T-20 | MT-09 |
| NFA-02 Zügige Reaktion | `TILT_TO_ACCEL`, `BASE_DAMP` | T-10 | MT-12 |
| NFA-03 Weg nicht sichtbar | Darstellung nur Start/Ziel/Wände | T-04 | MT-12 |
| NFA-04 Fordernd ohne Hilfe | Stufe Profi, Tempo bis 200 % | – | MT-12 |
| NFA-05 Physikalisch plausibel | Fachkonzept 3.3/3.4 | T-14 (Proportionalität) | MT-12 |
| NFA-06 Kein Tunneln | 480 Hz, `MAX_MOVE_PER_STEP`, `MAX_FRAME_DT` | T-05, T-07, T-12, T-17 | – |
| NFA-07 Plattform | `build.gradle.kts`, Manifest | CI-Build | MT-14 |
| NFA-08 Keine Bibliotheken | `build.gradle.kts` | CI-Build | – |
| NFA-09 Testbarkeit | Android-freie Kernklassen | alle T-Tests laufen ohne Android | – |
| NFA-10 Bedienbarkeit | `GameView.performClick`, Styles (48 dp) | CI-Lint | MT-13 |
| NFA-11 Lokalisierung | `strings.xml` | T-21, Ressourcen-Prüfung | – |
| NFA-12 Datenschutz | Manifest ohne Berechtigungen | CI-Lint | – |
| NFA-13 Energie/Pause | `onPause` → Sensor ab, Frames gestoppt | – | MT-11, MW-07, MW-10 |
| FA-16 Eigenständige Uhr-App | Modul `wear`, Manifest `standalone` | CI-Build + Lint (`wear`) | MW-01, MW-12 |
| FA-17 Steuerung mit dem Arm | `TiltInput` (gemeinsam), Fachkonzept 3.9.1 | T-23 (Physik der Uhr-Labyrinthe) | MW-04, MW-05 |
| FA-18 Gleiche Optionen auf der Uhr | `Difficulty.watchCols`, `WearMenuActivity` | T-23 | MW-02, MW-03, MW-06 |
| FA-19 Uhrgerechte Bedienung | `WearGameActivity`, `GameView.showResult`/Long-Click | – | MW-02, MW-08, MW-09 |
| NFA-14 Darstellung Uhr | `MazeLayout.squareInCircle`, `values-round` | T-26, T-27 | MW-01, MW-06 |
| NFA-15 Energie Uhr | Schwarzer Hintergrund, Lebenszyklus | – | MW-11 |
| NFA-16 Gemeinsame Logik | Modul `core` | alle T-Tests decken Handy und Uhr ab | – |
