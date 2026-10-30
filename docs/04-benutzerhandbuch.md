# 4 Benutzerhandbuch

## 4.1 Was ist Kugel-Labyrinth?

Ein Geschicklichkeitsspiel nach dem Vorbild des Holz-Kugellabyrinths: Du kippst dein Handy und lenkst damit eine Kugel durch ein Labyrinth ins grüne Ziel. Jedes Spiel erzeugt ein neues Labyrinth. Gemessen wird die Zeit.

## 4.2 Voraussetzungen

- Android-Smartphone mit Android 7.0 oder neuer
- Lage- bzw. Beschleunigungssensor (in praktisch allen Smartphones vorhanden)
- Keine Berechtigungen, keine Internetverbindung

## 4.3 Installation

**Variante A – fertige APK (ohne Android Studio):**
1. Im GitHub-Repository unter **Actions** den neuesten erfolgreichen Lauf von „Android CI“ öffnen.
2. Unten unter **Artifacts** die Datei `kugel-labyrinth-debug-apk` herunterladen und entpacken.
3. Die `.apk` aufs Handy kopieren und öffnen. Beim ersten Mal fragt Android, ob Installationen aus dieser Quelle erlaubt sind. Das muss für die verwendete App (z. B. Dateien oder Browser) erlaubt werden.

**Variante B – mit Android Studio:** Siehe [Entwicklerhandbuch](05-entwicklerhandbuch.md#52-projekt-öffnen-und-starten).

## 4.4 Das Menü

| Bereich | Auswahl | Wirkung |
|---|---|---|
| **Schwierigkeit** | Leicht · Mittel · Schwer · Profi | Labyrinthgröße (8 / 10 / 13 / 14 Spalten) und Stärke der Hilfe (100 / 60 / 30 / 0 %) |
| **Art der Unterstützung** | Empfindlichkeit · Software-Hilfe | *Wie* geholfen wird (siehe unten). Bei „Profi“ ausgegraut, weil es dort keine Hilfe gibt. |
| **Kugelgeschwindigkeit** | Regler 50–200 % | Wie schnell die Kugel beschleunigt und wie schnell sie maximal wird. Standard: 100 %. |

Unter den Auswahlfeldern erklärt ein Text, was die aktuelle Kombination bewirkt, und zeigt deine **Bestzeit** für genau diese Einstellung. Alle Einstellungen werden automatisch gespeichert.

### Die beiden Hilfe-Arten

- **Empfindlichkeit:** Das Handy reagiert weniger stark auf Kippen. Kleine Wackler werden ignoriert, und die Kugel beschleunigt langsamer. Das hilft, wenn du zu hektisch steuerst.
- **Software-Hilfe:** Das Handy reagiert voll empfindlich. Die App hilft beim Lenken:
  - Kippst du schräg, lenkt sie die Kugel in den offenen Gang, der am besten passt. So kommst du leichter um Ecken.
  - Sie hält die Kugel in der Mitte des Gangs.
  - Die Kugel prallt weniger (bei „Leicht“ gar nicht) von Wänden ab.

  Ohne Kippen bewegt sich die Kugel nicht, und den Weg musst du selbst finden.

## 4.5 Spielen

1. **„Spiel starten“** tippen.
2. Das Labyrinth erscheint mit dem Hinweis **„Bereit?“**. Halte das Handy so, wie es für dich bequem ist. Es muss nicht flach liegen.
3. **Aufs Spielfeld tippen.** Diese Haltung gilt ab jetzt als „waagerecht“, und die Zeit läuft.
4. Kippe das Handy, um die blaue Kugel vom Start (oben links) ins **grüne Ziel** zu steuern.
5. Im Ziel erscheinen deine Zeit und deine Bestzeit. Danach hast du drei Möglichkeiten:
   - **Neues Labyrinth:** ein anderes Labyrinth mit denselben Einstellungen
   - **Nochmal:** dasselbe Labyrinth noch einmal
   - **Menü:** zurück zum Menü

Oben im Spiel siehst du die laufende Zeit sowie Stufe, Hilfe-Art und Tempo. **Neustart** setzt die Kugel auf den Start zurück (gleiches Labyrinth, Zeit von vorn). **Menü** bricht das Spiel ab.

Wechselst du während des Spiels in eine andere App, hält die Zeit an. Beim Zurückkehren geht es sofort weiter.

## 4.6 Tipps

- Kleine, ruhige Bewegungen sind schneller als große. Die Kugel hat Schwung und rollt nach.
- Vor Kurven rechtzeitig gegenkippen, um abzubremsen.
- Für den Einstieg: „Leicht“ mit Software-Hilfe und 100 % Tempo. Für Profis: „Profi“ mit 150–200 %.
- Bestzeiten werden für jede Kombination aus Stufe, Hilfe-Art und Tempo getrennt gespeichert.

## 4.7 Problemlösung

| Problem | Ursache / Lösung |
|---|---|
| Die Kugel rollt von allein in eine Richtung. | Die Haltung beim Antippen war nicht die, in der du spielst. **Neustart** tippen, Handy bequem halten und erneut aufs Spielfeld tippen. |
| Die Kugel reagiert gar nicht. | Wurde aufs Spielfeld getippt („Bereit?“ verschwunden)? Erscheint „Dieses Gerät hat keinen Lage-Sensor“, ist das Gerät nicht geeignet. |
| Die Kugel reagiert zu träge oder zu nervös. | Im Menü die Kugelgeschwindigkeit anpassen oder die Hilfe-Art „Empfindlichkeit“ wählen. |
| Der Bildschirm dreht sich nicht mit. | Gewollt: Beim Kippen darf sich das Bild nicht drehen. Die App läuft immer im Hochformat. |
| Das Labyrinth ist nach dem Ändern der Systemsprache/Schriftgröße neu. | Android startet den Bildschirm dabei neu. Das laufende Spiel wird nicht gesichert. |
| Bestzeiten sind weg. | Bestzeiten liegen nur auf dem Gerät (und im Android-Backup). „Daten löschen“ in den App-Einstellungen entfernt sie. |
