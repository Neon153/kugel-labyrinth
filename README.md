# Kugel-Labyrinth

[![Android CI](https://github.com/Neon153/kugel-labyrinth/actions/workflows/android.yml/badge.svg)](https://github.com/Neon153/kugel-labyrinth/actions/workflows/android.yml)

Geschicklichkeitsspiel nach dem Vorbild des Holz-Kugellabyrinths, für **Android-Handy** und **Google Pixel Watch 5**. Auf dem Handy kippst du das Gerät, auf der Uhr neigst und drehst du den Arm, und steuerst so eine Kugel durch ein zufällig erzeugtes Labyrinth ins Ziel.

Vier Schwierigkeitsstufen unterscheiden sich darin, wie stark die Steuerung unterstützt wird. Für das *Wie* gibt es zwei Ansätze: einen weniger empfindlichen Sensor oder aktive Software-Hilfe bei voller Empfindlichkeit.

| | |
|---|---|
| **Version** | 1.4.0, siehe [CHANGELOG](CHANGELOG.md) |
| **Handy-App** | Android 7.0 – 15 (API 24 – 35), Hochformat |
| **Uhr-App** | Wear OS 3+ (API 30 – 35), entwickelt für die Pixel Watch 5 (Wear OS 7, 41/45 mm), eigenständig ohne Handy |
| **Sprache / Technik** | Kotlin 2.0.21 · Android-Views + Canvas · keine Bibliotheken |
| **Status** | Funktional vollständig; Unit-Tests, Lint und beide Android-Builds in der CI bestanden; **Gerätetests auf Handy und Uhr noch offen** (siehe [Tests 7.4](docs/07-tests.md#74-manuelle-gerätetests)) |

## Funktionen

- **Zufällige, perfekte Labyrinthe:** genau ein Weg zum Ziel, das Ziel liegt auf dem vom Start entferntesten Feld
- **Steuerung per Neigung:** Kalibrierung auf die beim Start gehaltene Lage, 100-Hz-Sensor, physikalisch begründete Bewegung mit Trägheit, Reibung und Wandstoß
- **Uhr: Steuerung mit dem Arm.** Unterarm drehen bewegt die Kugel Richtung 12/6 Uhr, Hand heben/senken Richtung 9/3 Uhr. Die Kugel rollt immer zur tieferen Displayseite.
- **Schwierigkeitsstufen**

  | Stufe | Handy | Uhr | Hilfe |
  |---|---|---|---|
  | Leicht | 8 Spalten | 5 × 5 | 100 % |
  | Mittel | 10 Spalten | 6 × 6 | 60 % |
  | Schwer | 13 Spalten | 7 × 7 | 30 % |
  | Profi | 14 Spalten | 8 × 8 | 0 % |

- **Hilfe-Arten**
  - *Empfindlichkeit:* Der Sensor reagiert schwächer (geringere Verstärkung, größere Totzone).
  - *Software-Hilfe:* Der Sensor bleibt unverändert. Die Software lenkt das Kippen in den passenden offenen Gang, hält die Kugel in der Gangmitte und dämpft das Abprallen. Sie kennt den Lösungsweg nicht.
- **Kugelgeschwindigkeit** einstellbar (50–200 %)
- **Zeitmessung und Bestzeiten** je Kombination aus Stufe, Hilfe-Art und Tempo

## Schnellstart

```bash
git clone https://github.com/Neon153/kugel-labyrinth.git
cd kugel-labyrinth
./gradlew testDebugUnitTest     # Unit-Tests
./gradlew :app:installDebug     # Handy-App auf angeschlossenes Handy
./gradlew :wear:installDebug    # Uhr-App auf per WLAN-Debugging verbundene Uhr
```

Oder in Android Studio über **File → Open** öffnen, die Run-Konfiguration **app** oder **wear** wählen und **Run ▶** drücken. Fertige Debug-APKs für Handy und Uhr erzeugt jeder CI-Lauf unter *Actions → Android CI → Artifacts*. Die Installation auf der Uhr ist im [Benutzerhandbuch 4.8](docs/04-benutzerhandbuch.md#48-uhr-app-pixel-watch-5) beschrieben.

## Dokumentation

| Nr. | Dokument | Inhalt |
|---|---|---|
| 1 | [Anforderungen](docs/01-anforderungen.md) | Ziel, funktionale und nicht-funktionale Anforderungen für Handy und Uhr mit Abnahmekriterien, Abgrenzung |
| 2 | [Architektur](docs/02-architektur.md) | arc42: Kontext, Module und Bausteine, Laufzeit, Verteilung, Konzepte, Risiken |
| 3 | [Fachkonzept](docs/03-fachkonzept.md) | Labyrinth-Algorithmus, Sensor und Kalibrierung, Physikmodell, Kollision, Hilfen, Parameter, Uhr-Steuerung und runde Darstellung |
| 4 | [Benutzerhandbuch](docs/04-benutzerhandbuch.md) | Installation, Menü, Spielen, Problemlösung, für Handy und Uhr |
| 5 | [Entwicklerhandbuch](docs/05-entwicklerhandbuch.md) | Build, Module, CI, Konventionen, Release, Uhr-Entwicklung |
| 6 | [Architekturentscheidungen](docs/06-entscheidungen.md) | ADR-001 … ADR-015, inkl. verworfener Ansätze |
| 7 | [Tests](docs/07-tests.md) | Teststrategie, 27 Testfälle, Ergebnisse, Gerätetestpläne Handy und Uhr, Abdeckungsmatrix |
| 8 | [Glossar](docs/08-glossar.md) | Begriffe |

## Projektstruktur (Kurzfassung)

```
core/   Gemeinsam: Spiellogik (ohne Android, JVM-getestet), Sensor, Spielfeld, Einstellungen
        └ src/test/…  27 JUnit-Tests
app/    Handy-App: Menü, Spielbildschirm
wear/   Uhr-App (Wear OS): Menü, Spielbildschirm
docs/   Dokumentation
```

## Autor und Lizenz

David ([@Neon153](https://github.com/Neon153)). Privates Projekt; es ist keine Lizenz festgelegt. Ohne Lizenz liegen alle Rechte beim Urheber.
