# 6 Architekturentscheidungen (ADR)

Jede Entscheidung folgt dem Schema **Kontext → Entscheidung → Konsequenzen**. Status: *angenommen*, *ersetzt* oder *verworfen*.

| ADR | Titel | Status | Version |
|---|---|---|---|
| 001 | Native Android-Views und Canvas, ohne Bibliotheken | angenommen | 1.0 |
| 002 | Spiellogik ohne Android-Abhängigkeiten | angenommen | 1.1 |
| 003 | Fester Physik-Zeitschritt mit Interpolation | angenommen | 1.1, 480 Hz seit 1.3 |
| 004 | Software-Hilfe als Lenkhilfe statt Zielführung | angenommen | 1.1 |
| 005 | Kalibrierung per Rotationsmatrix | angenommen | 1.1 |
| 006 | Keine Löcher im Spielfeld | angenommen | 1.3 |
| 007 | 0 % Hilfe als Schwierigkeitsstufe | angenommen | 1.3 |
| 008 | Tempo in Bildschirmeinheiten und einstellbar | angenommen | 1.2 / 1.3 |
| 009 | Zusammengefasste Wände und Zellenindex | angenommen | 1.1 |
| 010 | Bestzeiten je Stufe, Hilfe-Art und Tempo | angenommen | 1.3 |

---

## ADR-001: Native Android-Views und Canvas, ohne Bibliotheken

**Kontext:** Das Spiel zeichnet ein statisches Labyrinth und eine bewegte Kugel. Zur Wahl standen eine Game-Engine (z. B. libGDX), OpenGL ES, Jetpack Compose sowie klassische Views mit Canvas.

**Entscheidung:** Klassische Activities und Views. Das Spielfeld ist eine eigene `View`, die mit `Canvas` zeichnet. Außer der Kotlin-Standardbibliothek gibt es keine Abhängigkeiten, auch kein AndroidX.

**Konsequenzen:**
- ➕ Kleine APK, schneller Build, keine Versionskonflikte, vollständig nachvollziehbar
- ➕ Canvas mit Hardwarebeschleunigung reicht deutlich aus (1 Bitmap + 3 Kreise pro Frame)
- ➖ Kein Material-3-Design, kein Edge-to-Edge-Komfort von AndroidX; Insets per `fitsSystemWindows`
- ➖ Für aufwendige 3D-Effekte (rollende Kugel mit Textur) wäre OpenGL nötig

## ADR-002: Spiellogik ohne Android-Abhängigkeiten

**Kontext:** Die erste Version enthielt Physik, Zeichnen und Zustand in einer View. Ohne Gerät war sie nicht testbar, und in der Entwicklungsumgebung stand kein Android SDK zur Verfügung.

**Entscheidung:** Trennung in `GameEngine`, `Maze`, `Settings` und `Format` (reines Kotlin) einerseits und `GameView`, `TiltInput` sowie die Activities (Android) andererseits. Die Engine erhält die Neigung als Zahlen und liefert Positionen zurück.

**Konsequenzen:**
- ➕ JUnit-Tests auf der JVM in unter einer Sekunde, ohne Emulator
- ➕ Ein Test-Bot kann ganze Spiele simulieren (Lösbarkeit, Robustheit)
- ➖ Eine zusätzliche Schnittstelle (`TiltSource`, `Listener`)

## ADR-003: Fester Physik-Zeitschritt mit Interpolation

**Kontext:** Version 1.0 rechnete mit variablem `dt` direkt in `onDraw` (240 Hz Unterschritte, `System.nanoTime`). Das Verhalten hing von der Bildrate ab, und `postInvalidateOnAnimation` aus `onDraw` heraus ist kein sauberer Takt.

**Entscheidung:**
- Takt über `Choreographer.postFrameCallback` mit dem vsync-Zeitstempel
- Physik mit festem Schritt, Akkumulator und Begrenzung auf 50 ms pro Frame
- Darstellung der zwischen zwei Schritten interpolierten Position
- Schrittweite 1/480 s seit Version 1.3 (vorher 1/240 s), damit auch bei 200 % Tempo höchstens 0,117 Zellen pro Schritt zurückgelegt werden

**Konsequenzen:**
- ➕ Deterministisch und auf jedem Gerät gleich; ruckelfrei bei 60/90/120 Hz
- ➕ Spielzeit = Schritte × Schrittweite, also exakt und unabhängig von der Bildrate
- ➖ 8 Physik-Schritte pro 60-Hz-Frame; mit 0,86 µs pro Frame unkritisch

## ADR-004: Software-Hilfe als Lenkhilfe statt Zielführung

**Kontext:** In Version 1.0 zog die Software-Hilfe die Kugel entlang des kürzesten Wegs zum Ziel, sobald irgendwie gekippt wurde. Auf „Leicht“ löste sie damit das Labyrinth selbst, was dem Spielzweck widerspricht.

**Entscheidung:** Die Hilfe kennt den Lösungsweg nicht. Sie wählt unter den offenen Gängen der aktuellen Zelle den, der am besten zur **Kipprichtung des Spielers** passt. In diesen Gang lenkt sie einen Teil der Kippstärke um. Außerdem hält sie die Kugel in der Gangmitte und dämpft das Abprallen. Ohne Kippen wirkt nichts.

**Konsequenzen:**
- ➕ Die Hilfe erleichtert die Steuerung (Ecken, Wandkontakt), nicht die Navigation (FA-15)
- ➕ Die Sensorempfindlichkeit bleibt unverändert, wie gefordert (FA-07)
- ➖ An Kreuzungen kann die Hilfe bei diagonalem Kippen einen anderen Gang wählen, als der Spieler meint

**Verworfen:** Zielführung entlang des BFS-Distanzfelds (Version 1.0).

## ADR-005: Kalibrierung per Rotationsmatrix

**Kontext:** Version 1.0 zog beim Start den aktuellen Sensorwert als Offset ab. Bei schräg gehaltenem Gerät verfälscht das die Querachse (Faktor cos θ) und koppelt die Achsen.

**Entscheidung:** Die beim Antippen gemessene Schwerkraftrichtung wird per Rodrigues-Drehung auf die Hochachse gedreht ([Fachkonzept 3.2](03-fachkonzept.md#32-eingabe-vom-sensor-zur-neigung-tiltinput)). Zusätzlich wird `TYPE_GRAVITY` bevorzugt, die Achsen werden nach Display-Rotation umsortiert und der Tiefpass ist zeitbasiert.

**Konsequenzen:**
- ➕ Korrekte, gleich empfindliche Steuerung in jeder Haltung
- ➕ Funktioniert auf Geräten mit Querformat als natürlicher Lage
- ➖ Etwas mehr Mathematik (dokumentiert)

## ADR-006: Keine Löcher im Spielfeld

**Kontext:** Version 1.2 führte Löcher ein (Sturz → zurück zum Start), um das Spiel schwerer zu machen. Damit das Labyrinth lösbar bleibt, lagen sie nie auf dem Lösungsweg, bevorzugt aber an Abzweigungen.

**Entscheidung:** Löcher wurden in Version 1.3 vollständig entfernt (Code, Darstellung, Tests).

**Begründung:** Rückmeldung des Auftraggebers: Weil die Löcher nur abseits des Lösungswegs liegen, macht ihr Muster den Weg sichtbar. Das Labyrinth verliert damit seinen Charakter (NFA-03). Die Schwierigkeit kommt stattdessen aus Labyrinthgröße, Tempo und der 0-%-Stufe.

**Konsequenzen:**
- ➕ Das Spielfeld zeigt nur Start, Ziel und Wände
- ➖ Fehler werden nicht bestraft, nur die Zeit zählt

**Verworfene Alternative:** Löcher auch auf dem Lösungsweg, aber umfahrbar (außermittig). Das würde größere Zellen erfordern und das Spielprinzip deutlich ändern.

## ADR-007: 0 % Hilfe als Schwierigkeitsstufe

**Kontext:** Gefordert war ein Modus „komplett ohne Hilfsstärke, also 0 %“. Bis Version 1.2 gab es die Hilfe-Art „Keine Unterstützung“. Damit hätte es zwei Wege zu 0 % gegeben, und die Bedeutung von „Schwierigkeit“ (= Hilfestärke laut Auftrag) wäre unklar gewesen.

**Entscheidung:** Neue Stufe **Profi** mit Hilfestärke 0 und 14 Spalten. Die Hilfe-Art „Keine Unterstützung“ entfällt. Bei 0 % wird die Hilfe-Art im Menü ausgegraut. Gespeicherte Werte `NONE` fallen auf „Software-Hilfe“ zurück.

**Konsequenzen:**
- ➕ Klare Struktur: Stufe = *wie viel* Hilfe, Hilfe-Art = *wie*
- ➕ Getestet: Bei „Profi“ verhalten sich beide Hilfe-Arten exakt gleich
- ➖ Ein kleines Labyrinth ohne Hilfe ist nicht mehr wählbar

## ADR-008: Tempo in Bildschirmeinheiten und einstellbar

**Kontext:** Die Physik rechnet in Zellen. Größere Labyrinthe haben auf demselben Bildschirm kleinere Zellen, die Kugel wirkte dort langsamer. Außerdem empfand der Auftraggeber die Kugel als zu langsam und wünschte eine Einstellmöglichkeit.

**Entscheidung:** Beschleunigung, Höchstgeschwindigkeit und Zentrierkraft werden mit `Spalten / 9 × Tempo` skaliert. Das Tempo ist im Menü von 50 bis 200 % einstellbar. Die Grundwerte wurden in Version 1.2 deutlich erhöht (Beschleunigung ×5, Reibung ⅓).

**Konsequenzen:**
- ➕ In jeder Stufe gleiches Bildschirmtempo; die Stufen unterscheiden sich durch Ganggröße, nicht durch Trägheit
- ➖ Höheres Tempo erfordert einen kleineren Physik-Schritt (siehe ADR-003)

## ADR-009: Zusammengefasste Wände und Zellenindex

**Kontext:** Version 1.0 prüfte in jedem Physik-Schritt jede Einzelwand (bis zu 325 Rechtecke).

**Entscheidung:**
- Wandstücke entlang einer Rasterlinie werden zu langen Rechtecken zusammengefasst.
- Jeder Zelle wird die Liste der Rechtecke zugeordnet, die eine Kugel in dieser Zelle berühren kann (CSR-Index, Reichweite 0,36 ≥ Kugelradius).

**Konsequenzen:**
- ➕ Ø 3,7–3,8 Prüfungen pro Schritt statt 153–435; keine Nahtstellen
- ➕ Ein Test prüft, dass der Index **jede** berührbare Wand enthält
- ➖ Der Index muss angepasst werden, falls der Kugelradius über 0,36 wächst (durch `require` abgesichert)

## ADR-010: Bestzeiten je Stufe, Hilfe-Art und Tempo

**Kontext:** Mit dem Tempo-Regler sind Zeiten bei unterschiedlichem Tempo nicht vergleichbar.

**Entscheidung:** Der Schlüssel der Bestzeit enthält Stufe, Hilfe-Art und Tempo. Bei „Profi“ wird für die Hilfe-Art `NONE` eingesetzt, weil sie dort ohne Wirkung ist.

**Konsequenzen:**
- ➕ Faire Vergleiche
- ➖ Bestzeiten aus Version 1.2 und älter (Schlüssel ohne Tempo) werden nicht mehr angezeigt
