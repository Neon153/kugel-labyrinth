# 8 Glossar

| Begriff | Erklärung |
|---|---|
| **ADB / WLAN-Debugging** | *Android Debug Bridge*: Kommandozeilenwerkzeug zum Installieren und Debuggen von Apps; auf der Uhr über WLAN statt USB |
| **ADR** | *Architecture Decision Record*: kurze Dokumentation einer Architekturentscheidung mit Kontext, Entscheidung und Konsequenzen |
| **Akkumulator** | Sammelt die seit dem letzten Frame vergangene Zeit; daraus werden so viele feste Physik-Schritte gerechnet, wie hineinpassen |
| **AMOLED** | Display, bei dem jedes Pixel selbst leuchtet; schwarze Pixel sind aus und verbrauchen keinen Strom |
| **arc42** | Verbreitete Gliederungsvorlage für Architekturdokumentation |
| **BFS** | Breitensuche; bestimmt die kürzeste Weglänge von einer Zelle zu allen anderen |
| **Choreographer** | Android-Klasse, die Callbacks synchron zum Bildtakt (vsync) des Displays aufruft |
| **CI** | *Continuous Integration*: automatischer Build und Test bei jedem Push (hier GitHub Actions) |
| **CSR** | *Compressed Sparse Row*: kompakte Speicherung von Listen pro Zelle in zwei Arrays (Startindex + Inhalte) |
| **dp / sp** | Dichteunabhängige Pixel (Layout) bzw. skalierbare Pixel (Schrift, berücksichtigt die Systemschriftgröße) |
| **Gradle-Modul** | Eigenständig gebauter Teil des Projekts (hier `core`, `app`, `wear`) mit eigener `build.gradle.kts` |
| **Hilfe-Art** | *Wie* unterstützt wird: Empfindlichkeit oder Software-Hilfe |
| **Hilfestärke** | *Wie stark* unterstützt wird (100 / 60 / 30 / 0 %); kommt aus der Schwierigkeitsstufe |
| **Inkreis-Quadrat** | Größtes Quadrat, das in einen Kreis passt; Seitenlänge = Durchmesser / √2 |
| **Interpolation** | Zeichnen einer Zwischenposition zwischen zwei Physik-Schritten, damit die Bewegung bei jeder Bildrate glatt aussieht |
| **Kalibrierung** | Festlegen der beim Start gehaltenen Gerätelage als „waagerecht“ |
| **Krone (Rotary-Eingabe)** | Drehbare Taste an der Uhr; liefert Scroll-Ereignisse an die fokussierte View |
| **Neigung** | Hier: Komponente der Erdbeschleunigung in der Bildschirmebene in m/s² (≈ 9,81 · sin θ) |
| **nonTransitiveRClass** | Gradle-Einstellung: Die `R`-Klasse eines Moduls enthält nur dessen eigene Ressourcen; Ressourcen anderer Module werden über deren `R` angesprochen |
| **Perfektes Labyrinth** | Labyrinth ohne Zyklen und ohne unerreichbare Bereiche; zwischen zwei Zellen gibt es genau einen Weg |
| **Physik-Schritt** | Ein Rechenschritt der Simulation mit fester Dauer (1/480 s) |
| **Pronation / Supination** | Einwärts- bzw. Auswärtsdrehung des Unterarms um seine Längsachse; neigt das Uhrdisplay um die Unterarmachse |
| **Recursive Backtracker** | Labyrinth-Algorithmus auf Basis einer zufälligen Tiefensuche |
| **Rodrigues-Formel** | Formel für die Rotationsmatrix um eine beliebige Achse; hier zur Kalibrierung |
| **Standalone-App** | Wear-OS-App, die ohne Begleit-App auf dem Handy funktioniert |
| **Stoßzahl (Restitution)** | Anteil der Geschwindigkeit senkrecht zur Wand, der nach dem Aufprall erhalten bleibt (0 = kein Abprallen, 1 = voll elastisch) |
| **Tiefpass** | Filter, der schnelle Schwankungen (Rauschen) dämpft und langsame Änderungen durchlässt |
| **Totzone** | Neigungsbereich um die Neutrallage, der ignoriert wird |
| **Tunneln** | Fehler, bei dem ein schnelles Objekt in einem Rechenschritt durch eine dünne Wand springt |
| **vsync** | Vertikale Synchronisation; Zeitpunkt, zu dem das Display ein neues Bild übernimmt |
| **Wear OS** | Android-Variante für Smartwatches; auf der Pixel Watch 5 in Version 7 |
| **Zelle / Zelleneinheit** | Ein Feld des Labyrinth-Rasters; Längeneinheit der Physik (1 Zelle = Kantenlänge eines Feldes) |
