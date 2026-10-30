# 1 Anforderungsspezifikation

| | |
|---|---|
| **Projekt** | Kugel-Labyrinth (Android-App) |
| **Dokumentversion** | 1.3 – gilt für App-Version 1.3.0 (versionCode 4) |
| **Stand** | 30.10.2026 |
| **Auftraggeber / Product Owner** | David (GitHub: Neon153) |

Dieses Dokument beschreibt, **was** die App leisten muss. Wie das umgesetzt ist, steht in der [Architektur](02-architektur.md) und im [Fachkonzept](03-fachkonzept.md). Der Nachweis, dass die Anforderungen erfüllt sind, steht in der [Testdokumentation](07-tests.md).

## 1.1 Ausgangslage und Ziel

Gesucht ist eine Smartphone-App nach dem Vorbild des klassischen Holz-Kugellabyrinths: Durch Kippen des Handys rollt eine digitale Kugel durch ein Labyrinth ins Ziel. Die App soll mehrere Schwierigkeitsstufen bieten. Diese unterscheiden sich darin, **wie stark** die Steuerung unterstützt wird. Für die **Art** der Unterstützung gibt es zwei Ansätze:

1. **Empfindlichkeit:** Die App reagiert weniger empfindlich auf das Kippen.
2. **Software-Hilfe:** Die Software unterstützt aktiv, **ohne** die Empfindlichkeit des Sensors zu verändern.

Schwierigkeit und Art der Unterstützung werden in einem Menü verwaltet.

## 1.2 Herkunft der Anforderungen

Die Anforderungen stammen aus dem ursprünglichen Auftrag und aus drei Feedback-Runden nach dem Ausprobieren:

| Runde | Aussage des Auftraggebers (sinngemäß) | Führte zu |
|---|---|---|
| Auftrag | Kugel per Neigung durch ein Labyrinth steuern; mehrere Schwierigkeiten über Empfindlichkeit **oder** aktive Software-Hilfe; Verwaltung im Menü | FA-01 … FA-09 |
| Feedback 1 | App korrigieren, auf Fehler prüfen, performanter machen | NFA-01, NFA-06, NFA-09 |
| Feedback 2 | Kugel ist zu langsam, Bewegung muss flüssig sein; ohne Unterstützung zu einfach | NFA-02, NFA-04 |
| Feedback 3 | Löcher entfernen (Lösungsweg zu leicht sichtbar); Kugelgeschwindigkeit im Menü einstellbar; Modus mit 0 % Hilfe | NFA-03, FA-10, FA-08 |
| Rückfrage | Verhält sich die Kugel physikalisch? | NFA-05 |

**Kennzeichnung der Quelle:** **A** = vom Auftraggeber gefordert, **E** = von der Entwicklung ergänzt (zur Vervollständigung oder als Voraussetzung für eine A-Anforderung).

**Priorität:** **MUSS** = zwingend für die Abnahme, **SOLL** = erwartet, Abweichung nur begründet, **KANN** = wünschenswert.

## 1.3 Funktionale Anforderungen

| ID | Anforderung | Quelle | Prio | Abnahmekriterium |
|---|---|---|---|---|
| **FA-01** | Die Kugel wird ausschließlich durch Neigen des Geräts gesteuert. | A | MUSS | Kippt die rechte Geräteseite nach unten, rollt die Kugel nach rechts; kippt die Oberseite nach unten, rollt sie nach oben (analog für die Gegenrichtungen). Stärkere Neigung führt zu stärkerer Beschleunigung. |
| **FA-02** | Für jedes Spiel wird ein zufälliges Labyrinth erzeugt. Es hat genau einen Weg zwischen je zwei Feldern („perfektes Labyrinth“). Der Start liegt oben links, das Ziel auf dem Feld mit dem längsten Weg vom Start. | A/E | MUSS | Jedes Feld ist erreichbar, es gibt keine Zyklen, und das Ziel ist das vom Start am weitesten entfernte Feld. |
| **FA-03** | Die Kugel kann keine Wand durchdringen und das Spielfeld nicht verlassen. | A | MUSS | Unter keinen Eingaben, Geschwindigkeiten oder Bildraten liegt der Kugelmittelpunkt näher als der Kugelradius an einer Wand. |
| **FA-04** | Erreicht die Kugel das Ziel, endet der Durchgang, und die Zeit wird angezeigt. | A/E | MUSS | Ergebnisdialog mit Zeit und Bestzeit; danach keine weitere Bewegung. |
| **FA-05** | Es gibt mehrere Schwierigkeitsstufen. Sie unterscheiden sich in der **Stärke** der Unterstützung und zusätzlich in der Labyrinthgröße. | A | MUSS | Vier Stufen: Leicht 100 % / 8 Spalten, Mittel 60 % / 10, Schwer 30 % / 13, Profi 0 % / 14. |
| **FA-06** | Hilfe-Art **Empfindlichkeit**: Die App reagiert schwächer auf Neigung (geringere Verstärkung, größere Totzone). Die Software greift nicht in die Bewegung ein. | A | MUSS | Bei gleicher Neigung beschleunigt die Kugel umso schwächer, je höher die Hilfestärke ist. Ohne Neigung bleibt die Kugel liegen. |
| **FA-07** | Hilfe-Art **Software-Hilfe**: Der Sensor bleibt voll empfindlich. Die Software unterstützt aktiv beim Steuern. | A | MUSS | Die Beschleunigung durch die Neigung entspricht der ohne Hilfe. Zusätzlich lenkt die Software das Kippen in einen offenen Gang, hält die Kugel in der Gangmitte und dämpft das Abprallen. |
| **FA-08** | Es gibt eine Stufe mit **0 % Hilfe**. | A | MUSS | In der Stufe „Profi“ verhält sich die Kugel in beiden Hilfe-Arten exakt identisch und ungestützt. |
| **FA-09** | Schwierigkeit und Hilfe-Art werden in einem Menü gewählt. | A | MUSS | Menü mit beiden Auswahlgruppen; Erklärungstext zur aktuellen Wahl; bei 0 % ist die Hilfe-Art ausgegraut. |
| **FA-10** | Die Kugelgeschwindigkeit ist im Menü einstellbar. | A | MUSS | Regler 50–200 % in 10-%-Schritten, Standard 100 %. 200 % ergibt die doppelte Beschleunigung von 100 %. |
| **FA-11** | Die Haltung beim Spielstart gilt als neutral (Kalibrierung). | E | SOLL | Nach dem Antippen bleibt die Kugel in der aktuellen Haltung liegen, auch wenn das Gerät schräg gehalten wird. |
| **FA-12** | Die Spielzeit wird gemessen und angezeigt; die Bestzeit wird je Einstellung (Stufe, Hilfe-Art, Geschwindigkeit) gespeichert. | E | KANN | Laufende Anzeige mit Zehntelsekunden; neuer Rekord wird gemeldet. |
| **FA-13** | Während und nach dem Spiel: Neustart (gleiches Labyrinth), neues Labyrinth, zurück zum Menü. | E | SOLL | Schaltflächen „Neustart“ und „Menü“ im Spiel; „Neues Labyrinth“, „Nochmal“, „Menü“ im Ergebnisdialog. |
| **FA-14** | Gewählte Einstellungen und Bestzeiten bleiben nach dem Schließen der App erhalten. | E | SOLL | Nach Neustart der App ist die letzte Auswahl vorgewählt. |
| **FA-15** | Die Software-Hilfe darf das Labyrinth nicht selbst lösen. | A (abgeleitet) | MUSS | Ohne Neigung bewegt sich die Kugel in keinem Modus. Die Hilfe kennt den Lösungsweg nicht und lenkt nur in Richtung der Spielereingabe. |

## 1.4 Nicht-funktionale Anforderungen

| ID | Kategorie | Anforderung | Quelle | Prio | Abnahmekriterium / Messgröße |
|---|---|---|---|---|---|
| **NFA-01** | Performance | Flüssige Darstellung auf 60-, 90- und 120-Hz-Displays | A | MUSS | Spielschleife synchron zum Bildtakt; Interpolation zwischen Physik-Schritten; keine Objekt-Allokationen pro Frame; Physik < 0,1 ms pro Frame (gemessen: 0,86 µs). |
| **NFA-02** | Spielgefühl | Die Kugel reagiert zügig auf Neigung. | A | MUSS | Bei 100 % Tempo und ca. 12° Neigung ist die Kugel nach 0,15 s schneller als 2 Zellen/s. |
| **NFA-03** | Spielgefühl | Das Spielfeld darf den Lösungsweg nicht verraten. | A | MUSS | Außer Start, Ziel und Wänden gibt es keine Markierungen auf dem Spielfeld. |
| **NFA-04** | Spielgefühl | Ohne Hilfe muss das Spiel fordernd sein. | A | MUSS | Stufe mit 0 % Hilfe und größtem Labyrinth; Tempo bis 200 % erhöhbar. Bewertung durch den Auftraggeber im Gerätetest. |
| **NFA-05** | Realismus | Die Bewegung folgt einem physikalisch plausiblen Modell. | A | SOLL | Beschleunigung proportional zur Schwerkraftkomponente in der Ebene; Trägheit; Reibung; unvollständig elastischer Wandstoß. Bewusste Abweichungen sind in [Fachkonzept 3.4](03-fachkonzept.md#34-physikalische-einordnung) dokumentiert. |
| **NFA-06** | Robustheit | Keine Wanddurchdringung („Tunneln“), auch bei 200 % Tempo und Rucklern bis 200 ms | E | MUSS | Bewegung pro Physik-Schritt ≤ 0,15 Zellen (< halber Kugelradius); Zeitsprünge auf 50 ms begrenzt. |
| **NFA-07** | Plattform | Android 7.0 (API 24) bis Android 15 (API 35); Hochformat; Beschleunigungssensor erforderlich | E | MUSS | `minSdk 24`, `targetSdk 35`, `screenOrientation="portrait"`, `uses-feature accelerometer required`. |
| **NFA-08** | Wartbarkeit | Keine Fremdbibliotheken in der App | E | SOLL | Nur Android-Framework und Kotlin-Standardbibliothek. |
| **NFA-09** | Testbarkeit | Spiellogik ohne Android-Gerät testbar | E | SOLL | `GameEngine`, `Maze`, `Settings`, `Format` ohne Android-Importe; automatisierte JUnit-Tests. |
| **NFA-10** | Bedienbarkeit | Bedienung auch mit Hilfstechnik möglich; Display bleibt während des Spiels an | E | SOLL | Auswahlfelder ≥ 48 dp hoch; Spielfeld mit Inhaltsbeschreibung und `performClick`; `keepScreenOn`. |
| **NFA-11** | Lokalisierung | Deutsche Oberfläche, alle Texte in Ressourcen | E | SOLL | Keine fest kodierten Texte im Code oder in Layouts. |
| **NFA-12** | Datenschutz | Keine Berechtigungen, kein Netzwerk; Daten nur lokal | E | MUSS | Manifest ohne `uses-permission`; Speicherung nur in privaten SharedPreferences. |
| **NFA-13** | Energie | Beim Verlassen des Spiels ruhen Sensor und Spielschleife; die Zeit steht. | E | MUSS | `onPause` meldet den Sensor ab und stoppt die Frame-Callbacks. |

## 1.5 Abgrenzung (nicht Teil des Umfangs)

- Keine Löcher oder Fallen im Spielfeld (bewusst entfernt, siehe [ADR-006](06-entscheidungen.md#adr-006-keine-löcher-im-spielfeld))
- Kein Querformat, keine Tablet-optimierte Oberfläche
- Keine Online-Funktionen, Ranglisten, Konten oder Werbung
- Keine Sound- oder Vibrationsausgabe
- Keine weiteren Sprachen außer Deutsch
- Spielstand wird nicht über eine Neuerstellung der Activity hinweg gesichert (siehe [Architektur 11](02-architektur.md#11-risiken-und-technische-schulden))

## 1.6 Rückverfolgbarkeit

Welche Komponente eine Anforderung umsetzt und welcher Test sie nachweist, steht in der Abdeckungsmatrix der [Testdokumentation](07-tests.md#75-abdeckungsmatrix).
