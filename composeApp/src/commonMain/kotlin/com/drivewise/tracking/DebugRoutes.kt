package com.drivewise.tracking

object DebugRoutes {

    /**
     * Berlin Charlottenburg ~10-12km loop
     * Start: Richard-Wagner-Platz
     *
     * Format: (lat, lon)
     */
    val berlinCharlottenburg10k: List<Pair<Double, Double>> = listOf(
        // Richard-Wagner-Platz (start)
        52.51690, 13.30790,

        // Wilmersdorfer Str -> S Charlottenburg
        52.51640, 13.30490,
        52.51570, 13.30230,
        52.51490, 13.30010,

        // Around Schloss Charlottenburg area
        52.52070, 13.29580,
        52.52030, 13.29310,
        52.51910, 13.29180,
        52.51740, 13.29190,

        // Spandauer Damm -> Westend
        52.51610, 13.29290,
        52.51560, 13.29570,
        52.51510, 13.29830,

        // Towards Theodor-Heuss-Platz
        52.51090, 13.30010,
        52.50830, 13.30340,
        52.50670, 13.30980, // near Theodor-Heuss-Platz

        // Kaiserdamm -> Sophie-Charlotte-Platz
        52.50760, 13.31460,
        52.50960, 13.31960,
        52.51160, 13.32370, // Sophie-Charlotte-Platz area

        // Ernst-Reuter-Platz
        52.51270, 13.32670,
        52.51360, 13.32890,
        52.51390, 13.32310, // Ernst-Reuter-Platz-ish

        // Toward Savignyplatz / Kantstr
        52.50590, 13.32150,
        52.50420, 13.32050,
        52.50310, 13.31940,
        52.50170, 13.31840, // Savignyplatz-ish

        // Ku’damm-ish (north of)
        52.50190, 13.31380,
        52.50240, 13.30960,
        52.50340, 13.30610,
        52.50520, 13.30420,

        // Back north via Wilmersdorfer
        52.50810, 13.30490,
        52.51070, 13.30560,
        52.51290, 13.30620,
        52.51500, 13.30690,

        // Return to Richard-Wagner-Platz
        52.51640, 13.30760,
        52.51690, 13.30790
    ).chunked(2).map { it[0] to it[1] }
}

