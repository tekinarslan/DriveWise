package com.drivewise.tracking

object DebugRoutes {
    // Berlin merkezde küçük bir tur (lat, lon)
    val berlinShort: List<Pair<Double, Double>> = listOf(
        52.520008, 13.404954,
        52.520450, 13.405600,
        52.520900, 13.406200,
        52.521350, 13.406900,
        52.521000, 13.407600,
        52.520500, 13.407100,
        52.520050, 13.406400
    ).chunked(2).map { it[0] to it[1] }
}
