# 4 Benutzerhandbuch

## 4.1 Was ist Kugel-Labyrinth?

Ein Geschicklichkeitsspiel nach dem Vorbild des Holz-Kugellabyrinths: Du kippst dein Handy, oder auf der Pixel Watch deinen Arm, und lenkst damit eine Kugel durch ein Labyrinth ins grüne Ziel. Jedes Spiel erzeugt ein neues Labyrinth. Gemessen wird die Zeit.

Die Kapitel 4.2 bis 4.7 beschreiben die Handy-App, Kapitel 4.8 die Uhr-App.

## 4.2 Voraussetzungen

- Android-Smartphone mit Android 7.0 oder neuer
- Lage- bzw. Beschleunigungssensor (in praktisch allen Smartphones vorhanden)
- Keine Berechtigungen, keine Internetverbindung

## 4.3 Installation

**Variante A – fertige APK (ohne Android Studio):**
1. Im GitHub-Repository unter **Actions** den neuesten erfolgreichen Lauf von „Android CI“ öffnen.
2. Unten unter **Artifacts** die Datei `kugel-labyrinth-handy-apk` herunterladen und entpacken.
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

## 4.8 Uhr-App (Pixel Watch 5)

### Voraussetzungen

- Wear-OS-Uhr mit Wear OS 3 oder neuer. Entwickelt für die Google Pixel Watch 5 (41 und 45 mm).
- Die Uhr-App läuft eigenständig, das Handy wird zum Spielen nicht gebraucht.

### Installation per WLAN

Bis zur Veröffentlichung im Play Store wird die App über WLAN-Debugging auf die Uhr übertragen. Uhr und Rechner müssen im selben WLAN sein.

1. **Entwickleroptionen auf der Uhr freischalten:** In den Einstellungen der Uhr unter *System → Info* (je nach Version *Über die Uhr → Versionen*) siebenmal auf **Build-Nummer** tippen.
2. **WLAN-Debugging einschalten:** *Einstellungen → Entwickleroptionen → Debugging über WLAN* aktivieren, dann **Neues Gerät koppeln** wählen. Die Uhr zeigt eine IP-Adresse mit Port und einen Kopplungscode.
3. **Am Rechner koppeln und verbinden** (Android SDK Platform-Tools):
   ```
   adb pair <IP>:<Kopplungsport>      # Kopplungscode eingeben
   adb connect <IP>:<Port>            # Port aus „Debugging über WLAN“, nicht der Kopplungsport
   ```
4. **Installieren:** Im GitHub-Repository unter *Actions → Android CI → Artifacts* die Datei `kugel-labyrinth-uhr-apk` laden, entpacken und `adb install wear-debug.apk` ausführen.

Alternativ in Android Studio die Uhr über *Device Manager → Pair Devices Using Wi-Fi* verbinden, die Run-Konfiguration **wear** wählen und **Run ▶** drücken.

Nach der Installation erscheint „Kugel-Labyrinth“ in der App-Liste der Uhr.

### Menü

| Element | Bedienung | Wirkung |
|---|---|---|
| **Stufe** | antippen, um durchzuschalten | Leicht (5 × 5, Hilfe 100 %) → Mittel (6 × 6, 60 %) → Schwer (7 × 7, 30 %) → Profi (8 × 8, 0 %) |
| **Hilfe-Art** | antippen, um zu wechseln | Software-Hilfe ↔ Empfindlichkeit; bei „Profi“ ausgegraut |
| **Tempo** | **−** / **+** | 50–200 % in 10-%-Schritten |
| **Start** | antippen | Spiel beginnt |

Das Menü scrollt mit der **Krone** oder per Wischen. Die Einstellungen werden gespeichert. Darunter stehen Labyrinthgröße und deine Bestzeit für genau diese Einstellung.

### Spielen

1. Nach **Start** erscheint das Labyrinth mit „Bereit?“.
2. Halte den Arm bequem, Display zu dir, und **tippe aufs Spielfeld**. Diese Haltung gilt jetzt als waagerecht, und die Zeit läuft (oben angezeigt).
3. Steuere die Kugel ins grüne Ziel. **Die Kugel rollt immer zur tieferen Seite des Displays:**

   | Bewegung (Uhr am linken Handgelenk) | Kugel rollt Richtung |
   |---|---|
   | Unterarm drehen, sodass das Display **von dir weg** kippt | 12 Uhr (oben) |
   | Unterarm drehen, sodass das Display **zu dir** kippt | 6 Uhr (unten) |
   | Hand senken | 3 Uhr (rechts, zur Hand) |
   | Hand heben | 9 Uhr (links, zum Ellbogen) |

   Am rechten Handgelenk sind die Armbewegungen gespiegelt. Am einfachsten merkst du dir: Die Seite des Displays, die nach unten zeigt, zieht die Kugel an.
4. Im Ziel zeigt das Spielfeld deine Zeit und Bestzeit. **Antippen** startet ein neues Labyrinth, **lang drücken** dasselbe Labyrinth noch einmal.

| Aktion | Geste |
|---|---|
| Neustart während des Spiels | Spielfeld **lang drücken** |
| Zurück zum Menü | **nach rechts wischen** |

### Tipps für die Uhr

- Kleine Drehungen des Unterarms reichen. Halte den Ellbogen ruhig, zum Beispiel aufgestützt.
- Starte mit „Leicht“ und Software-Hilfe. Die Zellen auf der Uhr sind klein, die Hilfe hält die Kugel in der Gangmitte.
- Zu träge oder zu hektisch? Tempo im Menü anpassen.

### Problemlösung Uhr

| Problem | Ursache / Lösung |
|---|---|
| Die Kugel läuft in die falsche Richtung. | Die Kugel folgt der Schwerkraft, nicht der Armbewegung an sich. Schau, welche Displayseite gerade unten ist. |
| Die Kugel rollt von allein los. | Beim Antippen war der Arm anders gehalten als beim Spielen. Lang drücken (Neustart) und in Spielhaltung erneut antippen. |
| Das Display wird dunkel, wenn ich den Arm drehe. | Das System kann das Display beim Wegdrehen des Handgelenks abdunkeln. Den Arm nicht ganz wegdrehen; das Spiel pausiert dabei und läuft danach weiter. |
| Die Krone scrollt das Menü nicht. | Einmal auf eine freie Stelle im Menü tippen oder per Wischen scrollen. |
| `adb connect` schlägt fehl. | Für `adb connect` den Port aus „Debugging über WLAN“ verwenden, nicht den Kopplungsport; Uhr und Rechner im selben WLAN. |

