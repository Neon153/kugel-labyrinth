# Kugel-Labyrinth

[![Android CI](https://github.com/Neon153/kugel-labyrinth/actions/workflows/android.yml/badge.svg)](https://github.com/Neon153/kugel-labyrinth/actions/workflows/android.yml)

Android-Spiel nach dem Vorbild des Holz-Kugellabyrinths: **Kippe dein Handy** und steuere eine Kugel durch ein zufällig erzeugtes Labyrinth ins Ziel. Vier Schwierigkeitsstufen unterscheiden sich darin, wie stark die Steuerung unterstützt wird. Für das *Wie* gibt es zwei Ansätze: einen weniger empfindlichen Sensor oder aktive Software-Hilfe bei voller Empfindlichkeit.

| | |
|---|---|
| **Version** | 1.3.0 (versionCode 4), siehe [CHANGELOG](CHANGELOG.md) |
| **Plattform** | Android 7.0 – 15 (API 24 – 35), Hochformat |
| **Sprache / Technik** | Kotlin 2.0.21 · Android-Views + Canvas · keine Bibliotheken |
| **Status** | Funktional vollständig; Unit-Tests, Lint und Android-Build in der CI bestanden; **Gerätetest noch offen** (siehe [Tests 7.4](docs/07-tests.md#74-manuelle-gerätetests)) |

## Funktionen

- **Zufällige, perfekte Labyrinthe:** genau ein Weg zum Ziel, das Ziel liegt auf dem vom Start entferntesten Feld
- **Steuerung per Neigung:** Kalibrierung auf die beim Start gehaltene Lage, 100-Hz-Sensor, physikalisch begründete Bewegung mit Trägheit, Reibung und Wandstoß
- **Schwierigkeitsstufen**

  | Stufe | Spalten | Hilfe |
  |---|---|---|
  | Leicht | 8 | 100 % |
  | Mittel | 10 | 60 % |
  | Schwer | 13 | 30 % |
  | Profi | 14 | 0 % |

- **Hilfe-Arten**
  - *Empfindlichkeit:* Der Sensor reagiert schwächer (geringere Verstärkung, größere Totzone).
  - *Software-Hilfe:* Der Sensor bleibt unverändert. Die Software lenkt das Kippen in den passenden offenen Gang, hält die Kugel in der Gangmitte und dämpft das Abprallen. Sie kennt den Lösungsweg nicht.
- **Kugelgeschwindigkeit** im Menü einstellbar (50–200 %)
- **Zeitmessung und Bestzeiten** je Kombination aus Stufe, Hilfe-Art und Tempo

## Schnellstart

```bash
git clone https://github.com/Neon153/kugel-labyrinth.git
cd kugel-labyrinth
./gradlew testDebugUnitTest   # Unit-Tests
./gradlew installDebug        # auf angeschlossenes Handy installieren
```

Oder in Android Studio über **File → Open** öffnen und **Run ▶** drücken. Eine fertige Debug-APK erzeugt jeder CI-Lauf unter *Actions → Android CI → Artifacts*.

## Dokumentation

| Nr. | Dokument | Inhalt |
|---|---|---|
| 1 | [Anforderungen](docs/01-anforderungen.md) | Ziel, funktionale und nicht-funktionale Anforderungen mit Abnahmekriterien, Abgrenzung |
| 2 | [Architektur](docs/02-architektur.md) | arc42: Kontext, Bausteine, Laufzeit, Verteilung, Konzepte, Risiken |
| 3 | [Fachkonzept](docs/03-fachkonzept.md) | Labyrinth-Algorithmus, Sensor und Kalibrierung, Physikmodell, Kollision, Hilfen, Parameter |
| 4 | [Benutzerhandbuch](docs/04-benutzerhandbuch.md) | Installation, Menü, Spielen, Problemlösung |
| 5 | [Entwicklerhandbuch](docs/05-entwicklerhandbuch.md) | Build, Struktur, CI, Konventionen, Release, Erweiterungen |
| 6 | [Architekturentscheidungen](docs/06-entscheidungen.md) | ADR-001 … ADR-010, inkl. verworfener Ansätze |
| 7 | [Tests](docs/07-tests.md) | Teststrategie, 22 Testfälle, Ergebnisse, Gerätetestplan, Abdeckungsmatrix |
| 8 | [Glossar](docs/08-glossar.md) | Begriffe |

## Projektstruktur (Kurzfassung)

```
app/src/main/java/com/example/kugellabyrinth/
  Settings.kt  Maze.kt  GameEngine.kt  Format.kt     Spiellogik (ohne Android, JVM-getestet)
  GameView.kt  TiltInput.kt                          Darstellung, Spielschleife, Sensor
  MenuActivity.kt  GameActivity.kt  GamePrefs.kt     Bildschirme, Einstellungen
app/src/test/…                                       22 JUnit-Tests
docs/                                                Dokumentation
```

## Autor und Lizenz

David ([@Neon153](https://github.com/Neon153)). Privates Projekt; es ist keine Lizenz festgelegt. Ohne Lizenz liegen alle Rechte beim Urheber.
