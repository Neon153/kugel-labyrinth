# 8 Glossar

| Begriff | Erklärung |
|---|---|
| **ADR** | *Architecture Decision Record*: kurze Dokumentation einer Architekturentscheidung mit Kontext, Entscheidung und Konsequenzen |
| **Akkumulator** | Sammelt die seit dem letzten Frame vergangene Zeit; daraus werden so viele feste Physik-Schritte gerechnet, wie hineinpassen |
| **arc42** | Verbreitete Gliederungsvorlage für Architekturdokumentation |
| **BFS** | Breitensuche; bestimmt die kürzeste Weglänge von einer Zelle zu allen anderen |
| **Choreographer** | Android-Klasse, die Callbacks synchron zum Bildtakt (vsync) des Displays aufruft |
| **CI** | *Continuous Integration*: automatischer Build und Test bei jedem Push (hier GitHub Actions) |
| **CSR** | *Compressed Sparse Row*: kompakte Speicherung von Listen pro Zelle in zwei Arrays (Startindex + Inhalte) |
| **dp / sp** | Dichteunabhängige Pixel (Layout) bzw. skalierbare Pixel (Schrift, berücksichtigt die Systemschriftgröße) |
| **Hilfe-Art** | *Wie* unterstützt wird: Empfindlichkeit oder Software-Hilfe |
| **Hilfestärke** | *Wie stark* unterstützt wird (100 / 60 / 30 / 0 %); kommt aus der Schwierigkeitsstufe |
| **Interpolation** | Zeichnen einer Zwischenposition zwischen zwei Physik-Schritten, damit die Bewegung bei jeder Bildrate glatt aussieht |
| **Kalibrierung** | Festlegen der beim Start gehaltenen Gerätelage als „waagerecht“ |
| **Neigung** | Hier: Komponente der Erdbeschleunigung in der Bildschirmebene in m/s² (≈ 9,81 · sin θ) |
| **Perfektes Labyrinth** | Labyrinth ohne Zyklen und ohne unerreichbare Bereiche; zwischen zwei Zellen gibt es genau einen Weg |
| **Physik-Schritt** | Ein Rechenschritt der Simulation mit fester Dauer (1/480 s) |
| **Recursive Backtracker** | Labyrinth-Algorithmus auf Basis einer zufälligen Tiefensuche |
| **Rodrigues-Formel** | Formel für die Rotationsmatrix um eine beliebige Achse; hier zur Kalibrierung |
| **Stoßzahl (Restitution)** | Anteil der Geschwindigkeit senkrecht zur Wand, der nach dem Aufprall erhalten bleibt (0 = kein Abprallen, 1 = voll elastisch) |
| **Tiefpass** | Filter, der schnelle Schwankungen (Rauschen) dämpft und langsame Änderungen durchlässt |
| **Totzone** | Neigungsbereich um die Neutrallage, der ignoriert wird |
| **Tunneln** | Fehler, bei dem ein schnelles Objekt in einem Rechenschritt durch eine dünne Wand springt |
| **vsync** | Vertikale Synchronisation; Zeitpunkt, zu dem das Display ein neues Bild übernimmt |
| **Zelle / Zelleneinheit** | Ein Feld des Labyrinth-Rasters; Längeneinheit der Physik (1 Zelle = Kantenlänge eines Feldes) |
