# Änderungsprotokoll

Alle nennenswerten Änderungen am Projekt. Format angelehnt an [Keep a Changelog](https://keepachangelog.com/de/1.1.0/), Versionierung nach [Semantic Versioning](https://semver.org/lang/de/).

## [1.3.0] – 2026-10-30 (versionCode 4)

### Hinzugefügt
- Regler **Kugelgeschwindigkeit** im Menü (50–200 % in 10-%-Schritten, Standard 100 %).
- Schwierigkeitsstufe **Profi** mit 0 % Hilfe (14 Spalten). Die Hilfe-Art wird bei dieser Stufe ausgegraut.
- Bestzeiten werden zusätzlich nach Geschwindigkeit getrennt gespeichert.

### Geändert
- Physik-Takt von 240 Hz auf 480 Hz erhöht, damit auch bei 200 % keine Kugel durch Wände rutscht.
- Software-Hilfe dämpft das Abprallen jetzt anteilig zu ihrer Stärke (vorher immer vollständig).
- Menütexte zeigen die Hilfestärke in Prozent.

### Entfernt
- Löcher im Spielfeld. Sie lagen nie auf dem Lösungsweg und haben ihn dadurch sichtbar gemacht ([ADR-006](docs/06-entscheidungen.md#adr-006-keine-löcher-im-spielfeld)).
- Hilfe-Art „Keine Unterstützung“. Sie ist durch die Stufe „Profi“ (0 %) ersetzt ([ADR-007](docs/06-entscheidungen.md#adr-007-0--hilfe-als-schwierigkeitsstufe)).

## [1.2.0] – 2026-10-30 (versionCode 3)

### Geändert
- Kugel deutlich schneller: Beschleunigung ×5, Rollreibung ⅓, Höchstgeschwindigkeit 7 → 18 Zellen/s.
- Tempo skaliert mit der Labyrinthbreite (gleiche Bildschirmgeschwindigkeit in jeder Stufe).
- Sensor wird mit 100 Hz gelesen, Glättung 25 ms.
- Labyrinthgrößen 8 / 10 / 13 Spalten.

### Hinzugefügt
- Löcher als zusätzliche Schwierigkeit (in 1.3.0 wieder entfernt).

## [1.1.0] – 2026-10-30 (versionCode 2)

### Behoben
- Software-Hilfe hat das Labyrinth selbst gelöst (Zug zum Ziel). Jetzt lenkt sie nur das Kippen des Spielers.
- Kalibrierung per Offset verfälschte die Querneigung bei schräger Haltung. Jetzt per Rotationsmatrix.
- Möglicher Absturz durch `Bitmap.recycle()` während des Zeichnens.
- Ergebnisdialog konnte nach dem Schließen der Activity angezeigt werden (Fenster-Leck).
- Achsen bei Tablets mit Querformat als natürlicher Lage vertauscht.
- Zeit lief im Hintergrund weiter.
- Rundungsfehler in der Zeitanzeige (0,3 s wurde als 0,2 s angezeigt).

### Geändert
- Spiellogik (`GameEngine`, `Maze`) von Android getrennt und mit JUnit getestet.
- Kollision nur gegen vorindizierte Wände der aktuellen Zelle (≈ 4 statt bis zu 325 Prüfungen).
- Spielschleife über `Choreographer`, Interpolation zwischen Physik-Schritten, keine Allokationen pro Frame.
- `TYPE_GRAVITY` bevorzugt, zeitbasierter Tiefpass.
- Alle Texte in `strings.xml`, adaptives App-Icon, Backup-Regeln, targetSdk 35.

## [1.0.0] – 2026-10-30 (versionCode 1)

### Hinzugefügt
- Erste Version: zufälliges Labyrinth, Steuerung per Neigung, drei Schwierigkeitsstufen, Hilfe-Arten Empfindlichkeit / Software / keine, Menü, Zeitmessung und Bestzeiten.
