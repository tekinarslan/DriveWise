package com.drivewise.matching

object SnapPipeline {

    suspend fun snapWithChunking(
        client: OrsSnapClient,
        profile: String,
        points: List<LatLon>,
        radiusMeters: Int = 35,
        chunkSize: Int = 200
    ): List<LatLon> {
        if (points.size <= chunkSize) return client.snap(profile, points, radiusMeters)

        val out = ArrayList<LatLon>(points.size)
        var i = 0
        while (i < points.size) {
            val end = (i + chunkSize).coerceAtMost(points.size)
            val chunk = points.subList(i, end)

            // overlap: önceki chunk’ın son noktasını başa ekle
            val chunkWithOverlap =
                if (out.isNotEmpty() && chunk.isNotEmpty()) listOf(out.last()) + chunk
                else chunk

            val snapped = client.snap(profile, chunkWithOverlap, radiusMeters)

            // overlap’ı tekrar eklememek için ilk noktayı at (out doluysa)
            if (out.isNotEmpty() && snapped.isNotEmpty()) {
                out.addAll(snapped.drop(1))
            } else {
                out.addAll(snapped)
            }

            i = end
        }
        return out
    }
}
