# 3 Fachkonzept: Algorithmen und Physik

Dieses Dokument beschreibt die Verfahren hinter dem Spiel so, dass sie ohne Code nachvollziehbar sind. Alle Zahlenwerte entsprechen Version 1.4.0. Die Konstanten stehen im `companion object` von `GameEngine.kt`, `Maze.kt` bzw. `MazeLayout.kt` (Modul `core`). Handy- und Uhr-App verwenden dieselben Verfahren; die Besonderheiten der Uhr stehen in [3.9](#39-uhr-version-pixel-watch-5).

## 3.1 Labyrinth-Erzeugung (`Maze`)

### Raster

Das Spielfeld ist ein Raster aus `cols × rows` quadratischen Zellen. Auf dem **Handy** kommt die Spaltenzahl aus der Schwierigkeitsstufe (8 / 10 / 13 / 14), und die Zeilenzahl ergibt sich aus dem Seitenverhältnis der Spielfläche: `rows = max(5, ⌊Höhe / Zellgröße⌋)` mit `Zellgröße = Breite / cols`. Auf der **Uhr** ist das Labyrinth quadratisch (5 × 5 / 6 × 6 / 7 × 7 / 8 × 8), siehe [3.9.2](#392-labyrinth-im-runden-display). Die Berechnung steckt in `MazeLayout`; das Raster wird auf der Fläche zentriert.

Jede Zelle speichert in einer 4-Bit-Maske, in welche Richtungen sie offen ist (Bit 0 rechts, 1 unten, 2 links, 3 oben).

### Generierung: Recursive Backtracker (iterativ)

1. Start bei Zelle (0, 0), als besucht markieren, auf den Stapel legen.
2. Solange der Stapel nicht leer ist:
   - Unbesuchte Nachbarn der obersten Zelle bestimmen.
   - Gibt es keine, Zelle vom Stapel nehmen (Backtracking).
   - Sonst einen Nachbarn zufällig wählen, die Wand dazwischen öffnen (in beiden Zellen), Nachbarn als besucht markieren und auf den Stapel legen.

Das Ergebnis ist ein **perfektes Labyrinth**, also ein Spannbaum des Rasters: Jede Zelle ist erreichbar, und zwischen zwei Zellen gibt es genau einen Weg (n − 1 Durchgänge bei n Zellen). Das Verfahren erzeugt lange, gewundene Gänge mit wenigen Verzweigungen. Laufzeit und Speicher sind O(n). Der Stapel ist ein `IntArray`, es gibt keine Rekursion und damit keine Gefahr eines Stapelüberlaufs.

### Start und Ziel

Der Start liegt immer oben links. Das Ziel ist die Zelle mit der **größten Weglänge** vom Start, ermittelt per Breitensuche (BFS) über die offenen Durchgänge. So muss der Spieler stets den längsten Weg zurücklegen, den das Labyrinth bietet.

### Wände als Rechtecke

Wände haben die Dicke 0,14 Zellen. Für Zeichnen und Kollision werden geschlossene Kanten entlang jeder Rasterlinie zu **möglichst langen Rechtecken zusammengefasst**. Gegenüber Einzelsegmenten sind das rund 55–60 % weniger Rechtecke, und es entstehen keine Nahtstellen, an denen die Kugel hängen bleiben könnte.

### Wand-Index pro Zelle

Damit pro Physik-Schritt nicht alle Wände geprüft werden müssen, wird jeder Zelle die Liste der Wand-Rechtecke zugeordnet, die eine Kugel mit Mittelpunkt in dieser Zelle berühren kann. Dazu wird die Zelle um `INDEX_REACH = 0,36` (≥ Kugelradius 0,28) erweitert. Gespeichert ist der Index kompakt im CSR-Format: `cellWallStart` enthält die Startpositionen, `cellWalls` die Rechteck-Indizes.

Messwerte (Mittel über 20 Labyrinthe, Raster cols × 2·cols):

| Stufe | Raster | Wandsegmente einzeln | zusammengefasst | Ø geprüft pro Physik-Schritt |
|---|---|---|---|---|
| Leicht | 8 × 16 | 153 | 62 | 3,7 |
| Mittel | 10 × 20 | 231 | 97 | 3,7 |
| Schwer | 13 × 26 | 378 | 160 | 3,8 |
| Profi | 14 × 28 | 435 | 189 | 3,8 |

## 3.2 Eingabe: vom Sensor zur Neigung (`TiltInput`)

### Sensorwahl

Bevorzugt wird `TYPE_GRAVITY`, ein vom System fusionierter Schwerkraftvektor ohne Stöße und Handbewegungen. Ist er nicht vorhanden, wird `TYPE_ACCELEROMETER` verwendet. Die Abtastperiode beträgt 10 ms (100 Hz). Dafür ist keine Zusatzberechtigung nötig, die ist erst unter 5 ms erforderlich.

Android liefert den Vektor als **Gegenkraft** zur Schwerkraft: Liegt das Gerät flach, gilt g ≈ (0, 0, +9,81) m/s².

### Achsen auf den Bildschirm abbilden

Die App ist auf Hochformat fixiert. Bei Geräten, deren natürliche Lage Querformat ist (manche Tablets), entspricht Hochformat aber der Display-Rotation 90° oder 270°. Die Sensorachsen werden deshalb einmal pro Spielstart nach der Display-Rotation umsortiert:

| Rotation | x_Bildschirm | y_Bildschirm |
|---|---|---|
| 0° | x | y |
| 90° | −y | x |
| 180° | −x | −y |
| 270° | y | −x |

### Glättung

Ein Tiefpass erster Ordnung, **zeitbasiert** über die Sensor-Zeitstempel. Dadurch verhält er sich bei jeder tatsächlichen Abtastrate gleich:

$$\vec g \leftarrow \vec g + \alpha\,(\vec s - \vec g), \qquad \alpha = \frac{\Delta t}{\tau + \Delta t}, \quad \tau = 25\ \text{ms}$$

Die Grenzfrequenz liegt bei etwa 1/(2πτ) ≈ 6 Hz. Sensorrauschen wird gedämpft, bewusste Kippbewegungen kommen ohne spürbare Verzögerung durch.

### Kalibrierung (neutrale Haltung)

Beim Antippen des Spielfelds wird die aktuelle Schwerkraftrichtung $\hat n = \vec g / |\vec g| = (p, q, c)$ als „flach“ definiert. Gesucht ist die Drehung $R$, die $\hat n$ auf $\hat e_z = (0, 0, 1)$ abbildet. Nach der Rodrigues-Formel mit Drehachse $\vec v = \hat n \times \hat e_z = (q, -p, 0)$ gilt:

$$R = I + [\vec v]_\times + \frac{[\vec v]_\times^2}{1 + c}$$

Benötigt werden nur die ersten beiden Zeilen:

$$R_0 = \left(1 - \tfrac{p^2}{1+c},\ -\tfrac{pq}{1+c},\ -p\right), \qquad R_1 = \left(-\tfrac{pq}{1+c},\ 1 - \tfrac{q^2}{1+c},\ -q\right)$$

Die Neigung auf Bildschirmachsen ergibt sich dann aus

$$t_x = -R_0 \cdot \vec g, \qquad t_y = R_1 \cdot \vec g$$

**Vorzeichen:** Kippt die rechte Kante nach unten, wird $R_0 \cdot \vec g$ negativ, also ist $t_x > 0$ und die Kugel rollt nach rechts. Kippt die obere Kante nach unten, wird $t_y < 0$ und die Kugel rollt nach oben.

**Warum keine einfache Offset-Subtraktion?** Hält man das Gerät z. B. 40° schräg und zieht nur den Startwert ab, ist die seitliche Neigung um den Faktor cos 40° ≈ 0,77 zu schwach, und die Achsen koppeln. Die Drehung behandelt jede Ausgangshaltung korrekt ([ADR-005](06-entscheidungen.md#adr-005-kalibrierung-per-rotationsmatrix)).

**Sonderfälle:** Ohne Sensorwert, bei |g| < 1 m/s² oder bei nach unten zeigendem Display (c < −0,9) wird keine Drehung verwendet (R = I).

## 3.3 Physikmodell (`GameEngine`)

### Skalierung

Alle Größen sind in Zelleneinheiten angegeben. Damit die Kugel auf dem Bildschirm in jeder Stufe gleich schnell ist, wird mit der Spaltenzahl skaliert. Zusätzlich fließt der Tempo-Regler $f \in [0{,}5;\ 2]$ ein:

$$s = \frac{n_\text{Spalten}}{9} \cdot f$$

### Aufbereitung der Eingabe

Jede Achse wird einzeln behandelt. Die Totzone $d$ wird abgezogen und das Ergebnis auf 6 m/s² (≈ 37°) begrenzt:

$$\tilde t = \operatorname{sgn}(t)\cdot\min\!\big(\max(|t| - d,\ 0),\ 6\big)$$

### Bewegungsgleichung

$$\vec a = k\,\tilde{\vec t} + \vec a_\text{Hilfe}, \qquad k = (1 - 0{,}5\,h_E)\cdot 8 \cdot s \quad\left[\tfrac{\text{Zellen/s}^2}{\text{m/s}^2}\right]$$

$h_E$ ist die Hilfestärke, wenn die Hilfe-Art „Empfindlichkeit“ gewählt ist, sonst 0. Die Integration erfolgt **semi-implizit nach Euler** mit festem Schritt $\Delta t = 1/480$ s:

$$\vec v_{n+1} = \big(\vec v_n + \vec a\,\Delta t\big)\cdot e^{-0{,}5\,\Delta t}, \qquad |\vec v_{n+1}| \le v_\text{max}, \qquad \vec x_{n+1} = \vec x_n + \vec v_{n+1}\,\Delta t$$

$$v_\text{max} = \min(18\,s,\ 0{,}15/\Delta t = 72)\ \text{Zellen/s}$$

Danach folgt zweimal die Kollisionsauflösung (3.5) und der Zieltest: Das Ziel ist erreicht, wenn der Abstand des Kugelmittelpunkts zur Zielmitte kleiner als 0,30 Zellen ist.

### Zeitschritt und Darstellung

Die Bildrate (60/90/120 Hz) und der Physik-Takt (480 Hz) sind entkoppelt:

1. Jeder Frame addiert die vergangene Zeit (höchstens 50 ms) zu einem Akkumulator.
2. Solange der Akkumulator ≥ 1/480 s ist, wird ein Physik-Schritt ausgeführt.
3. Gezeichnet wird die Position, die zwischen dem vorletzten und dem letzten Schritt interpoliert ist: $\vec x_\text{render} = \vec x_\text{prev} + (\vec x - \vec x_\text{prev})\cdot\frac{\text{Akku}}{\Delta t}$

So läuft die Physik deterministisch und unabhängig vom Gerät, und die Darstellung bleibt trotzdem ruckelfrei.

## 3.4 Physikalische Einordnung

| Aspekt | Modell in der App | Reale Kugel | Bewertung |
|---|---|---|---|
| Antrieb | ∝ Schwerkraftkomponente in der Ebene (g · sin θ) | a = 5/7 · g · sin θ (rollende Vollkugel) | ✔ physikalisch korrekt, Maßstab umgerechnet |
| Trägheit | Geschwindigkeit bleibt erhalten, nur Reibung bremst | ebenso | ✔ |
| Wandstoß | Normalanteil mit Stoßzahl e = 0,35 umgekehrt, Tangentialanteil bleibt | e ≈ 0,3–0,6 (Stahl auf Holz), plus Reibung an der Wand | ✔ plausibel |
| Kalibrierung | Drehung des Schwerkraftvektors | – | ✔ korrekt für jede Haltung |
| Maßstab | Kugel verhält sich wie auf einem **0,80 m** breiten Brett (bei 100 %), auf den Bildschirm verkleinert | Handy ≈ 7 cm breit | bewusst: real wäre die Kugel unspielbar schnell |
| Reibung | ∝ Geschwindigkeit (exponentieller Abfall, 0,5 /s) | Rollreibung ≈ konstante, sehr kleine Kraft | ✘ vereinfacht: bremst bei hohem Tempo zu stark, rollt langsam zu lange aus |
| Höchstgeschwindigkeit | 2 Brettbreiten/s ≈ 1,6 m/s | kein Limit | ✘ Spielentscheidung |
| Neigungsgrenze | ab 37° keine weitere Beschleunigung | bis 90° | ✘ Spielentscheidung |
| Totzone | 0,15 m/s² (≈ 0,9°) auf der **Eingabe** | Haftreibung wirkt nur an der **ruhenden** Kugel | ✘ vereinfacht: eine rollende Kugel spürt minimale Neigung nicht |
| Drall | nur pauschal im Faktor 5/7 enthalten | Drall ändert sich bei Stößen und Wandkontakt | ✘ nicht modelliert |
| Handbewegungen | durch `TYPE_GRAVITY` ausgefiltert | ein ruckartig bewegtes Brett schiebt die Kugel an | ✘ bewusst: kein Steuern durch Schütteln |

**Brettbreite:** Die App beschleunigt mit 8/9 · f Bildschirmbreiten/s² pro m/s² Neigung. Eine echte rollende Kugel beschleunigt mit 5/7 m/s² pro m/s². Daraus folgt eine äquivalente Brettbreite von $W = \frac{5/7}{8/9 \cdot f} \approx \frac{0{,}80\ \text{m}}{f}$, also 1,6 m bei 50 %, 0,80 m bei 100 % und 0,40 m bei 200 %. 200 % entspricht damit etwa der Größe eines klassischen Holzlabyrinths.

**Hilfen:** Die Hilfe-Art „Empfindlichkeit“ bleibt physikalisch stimmig. Sie wirkt wie eine Untersetzung zwischen Handy- und Brettneigung, nur die vergrößerte Totzone ist künstlich. Die Software-Hilfe ist bewusst unphysikalisch (zusätzliche Kräfte und Dämpfung).

## 3.5 Kollision

Jede Wand ist ein achsenparalleles Rechteck. Geprüft werden nur die Rechtecke aus dem Index der Zelle, in der der Kugelmittelpunkt liegt:

1. **Nächster Punkt:** Den Kugelmittelpunkt auf das Rechteck klemmen. Ist der Abstand zu diesem Punkt ≥ Radius r = 0,28, gibt es keine Berührung.
2. **Durchdringung auflösen:** Die Kugel entlang der Normalen (Mittelpunkt minus nächster Punkt, normiert) um r − Abstand hinausschieben. Liegt der Mittelpunkt im Rechteck, wird über die nächstgelegene Kante hinausgeschoben.
3. **Stoß:** Bewegt sich die Kugel auf die Wand zu ($v_n = \vec v\cdot\hat n < 0$), gilt $\vec v \leftarrow \vec v - (1 + e)\,v_n\,\hat n$.

Zwei Durchläufe pro Schritt sorgen dafür, dass die Kugel in Ecken sauber an beiden Wänden anliegt.

**Kein Tunneln:** Im ungünstigsten Fall (Profi, 200 %) bewegt sich die Kugel höchstens 0,117 Zellen pro Schritt. Das ist weniger als der halbe Radius (0,14) und weniger als die Wanddicke. Der Mittelpunkt kann deshalb keine Wand überspringen und nie so tief eindringen, dass er auf der falschen Seite herausgeschoben würde.

## 3.6 Unterstützungsarten

Die **Hilfestärke** h ∈ {1,0; 0,6; 0,3; 0} kommt aus der Stufe. Die **Hilfe-Art** bestimmt, wie sie wirkt. Bei h = 0 (Profi) sind beide Hilfe-Arten wirkungslos und das Verhalten ist identisch (getestet).

### Empfindlichkeit

Nur die Eingabe wird verändert:

$$\text{Verstärkung} = 1 - 0{,}5\,h, \qquad d = 0{,}15 + 0{,}5\,h\ \ \text{m/s}^2$$

### Software-Hilfe

Die Verstärkung bleibt bei 1 und die Totzone bei 0,15 m/s², der Sensor ist also voll empfindlich. Solange der Spieler kippt ($\tilde{\vec t} \neq 0$), greift die Software in drei Schritten ein:

1. **Gangwahl:** Unter den offenen Richtungen der aktuellen Zelle wird die gewählt, die am besten zur Kipprichtung passt (größtes Skalarprodukt > 0). Der Lösungsweg ist der Hilfe nicht bekannt (FA-15).
2. **Umlenkung:** Ein Anteil der Kippstärke $m = |\tilde{\vec t}|$ wird in Richtung der Mitte der Nachbarzelle umgelenkt:
   $\vec a \mathrel{+}= h\cdot 0{,}9\cdot m\cdot k \cdot \widehat{(\vec p_\text{Nachbar} - \vec x)}$
3. **Gangmitte:** Mit $\gamma = \min(1,\ m / 2{,}5)$ wirkt quer zur Gangrichtung eine Federkraft $a_\perp \mathrel{+}= h\cdot 25\cdot s\cdot\gamma\cdot(c_\perp - x_\perp)$. Die Querbewegung wird gedämpft: $v_\perp \mathrel{\cdot}= 1 - h\cdot 8\cdot\gamma\cdot\Delta t$.

Zusätzlich ist der Wandstoß abgeschwächt: e = 0,35 · (1 − h).

Ohne Kippen wirkt keine dieser Kräfte, eine ruhende Kugel bleibt also liegen.

### Wirkung je Stufe

| Stufe | h | Empfindlichkeit: Verstärkung | Empfindlichkeit: Totzone | Software: Stoßzahl e | Software: Umlenkung (Anteil der Kippstärke) |
|---|---|---|---|---|---|
| Leicht | 1,0 | 50 % | 0,65 m/s² (≈ 3,8°) | 0 | 90 % |
| Mittel | 0,6 | 70 % | 0,45 m/s² (≈ 2,6°) | 0,14 | 54 % |
| Schwer | 0,3 | 85 % | 0,30 m/s² (≈ 1,8°) | 0,245 | 27 % |
| Profi | 0 | 100 % | 0,15 m/s² (≈ 0,9°) | 0,35 | 0 % |

## 3.7 Zeitmessung

Die Spielzeit ist exakt `Anzahl Physik-Schritte × 1/480 s`. Sie hängt also nur von der simulierten Zeit ab, nicht von der Bildrate. Pausen werden nicht mitgezählt. Angezeigt wird sie abgeschnitten auf Zehntelsekunden („12,3 s“). Ein Epsilon von 0,001 Zehnteln verhindert, dass Gleitkomma-Rundung z. B. 0,3 s als 0,2 s erscheinen lässt.

## 3.8 Parameterübersicht

| Konstante | Wert | Einheit | Bedeutung |
|---|---|---|---|
| `STEP` | 1/480 | s | Physik-Zeitschritt |
| `MAX_FRAME_DT` | 0,05 | s | größter simulierter Zeitsprung pro Frame |
| `REFERENCE_COLS` | 9 | – | Bezugsbreite für die Skalierung |
| `TILT_TO_ACCEL` | 8 | Zellen/s² pro m/s² | Verstärkung bei 9 Spalten, 100 % |
| `MAX_TILT` | 6 | m/s² | Neigungsgrenze (≈ 37°) |
| `BASE_DEAD_ZONE` | 0,15 | m/s² | Totzone ohne Hilfe (≈ 0,9°) |
| `BASE_DAMP` | 0,5 | 1/s | Rollreibung (geschwindigkeitsproportional) |
| `MAX_SPEED` | 18 | Zellen/s | Höchstgeschwindigkeit bei 9 Spalten, 100 % |
| `MAX_MOVE_PER_STEP` | 0,15 | Zellen | Obergrenze gegen Tunneln |
| `WALL_BOUNCE` | 0,35 | – | Stoßzahl ohne Software-Hilfe |
| `BALL_RADIUS` | 0,28 | Zellen | Kugelradius |
| `GOAL_RADIUS` | 0,30 | Zellen | Zielradius |
| `REDIRECT` | 0,9 | – | Umlenkanteil der Software-Hilfe bei h = 1 |
| `GATE_TILT` | 2,5 | m/s² | ab dieser Neigung volle Zentrier-Hilfe |
| `CENTER_ACCEL` | 25 | 1/s² | Federkonstante zur Gangmitte |
| `LATERAL_DAMP` | 8 | 1/s | Querdämpfung |
| `WALL_THICKNESS` | 0,14 | Zellen | Wanddicke |
| `INDEX_REACH` | 0,36 | Zellen | Reichweite des Zellenindex (≥ Kugelradius) |
| `FILTER_TAU` | 0,025 | s | Zeitkonstante des Sensor-Tiefpasses |
| `SAMPLING_PERIOD_US` | 10 000 | µs | Sensor-Abtastperiode (100 Hz) |
| `MazeLayout.MIN_ROWS` | 5 | – | Mindestzahl Zeilen auf dem Handy |
| `MazeLayout.CIRCLE_FILL` | 0,96 | – | Anteil des Inkreis-Quadrats, den das Uhr-Labyrinth nutzt |
| `Difficulty.watchCols` | 5 / 6 / 7 / 8 | Felder | Seitenlänge des Uhr-Labyrinths je Stufe |

## 3.9 Uhr-Version (Pixel Watch 5)

### 3.9.1 Steuerung mit dem Arm

Die Uhr nutzt **dieselbe Kette** aus Sensor, Kalibrierung und Physik wie das Handy (3.2 bis 3.6). Der Lage-Sensor misst die Richtung der Schwerkraft relativ zum Display. Die Kugel rollt deshalb immer zur **tieferen Seite des Displays**, ganz gleich, ob das Handy oder der Arm geneigt wird.

Für eine Uhr am linken Handgelenk, Display zum Gesicht, Krone Richtung Hand, verläuft die Unterarmachse entlang der Display-x-Achse (9 Uhr → 3 Uhr). Daraus ergibt sich:

| Bewegung | Drehachse | Wirkung auf die Kugel |
|---|---|---|
| **Unterarm drehen** (Pronation/Supination), Display kippt von dir weg bzw. zu dir | Unterarmachse = Display-x | Kugel rollt Richtung 12 Uhr bzw. 6 Uhr |
| **Hand heben/senken** (Unterarm im Ellbogen oder Handgelenk kippen) | quer zum Unterarm = Display-y | Kugel rollt Richtung 9 Uhr (Ellbogen) bzw. 3 Uhr (Hand) |
| Arm waagerecht schwenken (um die senkrechte Achse) | senkrecht | **keine**, weil sich die Schwerkraftrichtung zum Display nicht ändert |
| Ruckartige Armbewegung | – | keine: `TYPE_GRAVITY` filtert Bewegungsbeschleunigung heraus |

Am rechten Handgelenk kehren sich die Richtungen der Armbewegungen entsprechend um. Die Regel „Kugel rollt nach unten“ gilt unverändert. Meldet Wear OS bei gedrehter Display-Ausrichtung eine Display-Rotation, sortiert `TiltInput` die Achsen wie auf dem Handy um (3.2).

**Kalibrierung:** Beim Antippen wird die gerade gehaltene Armhaltung als „waagerecht“ übernommen, typischerweise mit dem Display schräg zum Gesicht. Dank der Rotationsmatrix (3.2) bleibt die Steuerung auch in dieser schrägen Haltung in beiden Achsen gleich empfindlich.

**Bewegungsumfang:** Bequem sind etwa ±30–40° Unterarmdrehung. Das entspricht einer Neigung von 4,9–6,3 m/s² und damit dem vollen Steuerbereich bis zur Neigungsgrenze `MAX_TILT` = 6 m/s² (≈ 37°).

### 3.9.2 Labyrinth im runden Display

Das größte Rechteck in einem Kreis ist das Quadrat mit der Seitenlänge Durchmesser/√2. `MazeLayout.squareInCircle` nutzt davon 96 %, damit die Wände nicht direkt am Displayrand liegen:

$$a = \frac{D}{\sqrt 2}\cdot 0{,}96, \qquad \text{Zelle} = \frac{a}{n}, \qquad \text{Rand oben/unten} = \frac{D - a}{2}$$

Das Labyrinth belegt so 59 % der Displayfläche. In den freien Kreissegmenten oben und unten stehen die Zeit bzw. Stufe und Tempo.

| Stufe | Felder | 41 mm (408 px): Zelle | Kugel ⌀ | 45 mm (456 px): Zelle | Kugel ⌀ |
|---|---|---|---|---|---|
| Leicht | 5 × 5 | 55 px (4,4 mm) | 2,5 mm | 62 px (4,9 mm) | 2,8 mm |
| Mittel | 6 × 6 | 46 px (3,7 mm) | 2,1 mm | 52 px (4,1 mm) | 2,3 mm |
| Schwer | 7 × 7 | 40 px (3,1 mm) | 1,8 mm | 44 px (3,5 mm) | 2,0 mm |
| Profi | 8 × 8 | 35 px (2,7 mm) | 1,5 mm | 39 px (3,1 mm) | 1,7 mm |

Bei 320 ppi. Das Labyrinth ist 22,0 mm (41 mm) bzw. 24,6 mm (45 mm) breit, der Rand oben und unten jeweils 65 bzw. 73 px.

**Verworfene Alternativen** ([ADR-013](06-entscheidungen.md#adr-013-quadratisches-labyrinth-im-runden-display)): ein kreisförmiges Labyrinth (Theta-Labyrinth) würde die Fläche besser nutzen, braucht aber eine völlig andere Kollision (Bögen statt Rechtecke). Ein größeres Labyrinth mit mitlaufendem Ausschnitt würde den Überblick nehmen.

### 3.9.3 Tempo auf der Uhr

Die Skalierung aus 3.3 gilt unverändert: Die Kugel bewegt sich relativ zur **Labyrinthbreite** so schnell wie auf dem Handy. Weil das Uhr-Labyrinth nur etwa 22 mm breit ist, ist sie in Millimetern pro Sekunde entsprechend langsamer. Das ist gewollt, weil die Zellen auf der Uhr sehr klein sind. Für mehr Tempo gibt es den Regler (bis 200 %).

### 3.9.4 Bedienung und Energie

| Aktion | Uhr |
|---|---|
| Start + Kalibrierung | Spielfeld antippen |
| Neustart (gleiches Labyrinth) | Spielfeld lang drücken (mit Haptik-Rückmeldung des Systems) |
| Nach dem Ziel: neues Labyrinth | Antippen |
| Zurück zum Menü | Nach rechts wischen |
| Menü scrollen | Krone drehen oder wischen |

Zum Energiesparen ist der Hintergrund schwarz (AMOLED-Pixel bleiben aus). Sensor und Spielschleife laufen nur, solange der Spielbildschirm sichtbar ist. Das Display (bis 60 Hz) bleibt während des Spiels per `keepScreenOn` an.
