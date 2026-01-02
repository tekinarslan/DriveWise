package com.drivewise.matching

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

class OrsSnapClient(
    private val http: HttpClient,
    private val apiKey: String,
    private val baseUrl: String = "https://api.openrouteservice.org"
) {
    /**
     * ORS coordinate order: [lon, lat]
     * radius: 20..35m iyi başlangıç
     */
    suspend fun snap(
        profile: String,
        points: List<LatLon>,
        radiusMeters: Int = 35
    ): List<LatLon> {
        if (points.isEmpty()) return emptyList()

        val req = OrsSnapRequest(
            locations = points.map { listOf(it.lon, it.lat) },
            radius = radiusMeters
        )

        val resp: OrsSnapGeoJson = http.post("$baseUrl/v2/snap/$profile/geojson") {
            header("Authorization", apiKey)
            contentType(ContentType.Application.Json)
            setBody(req)
        }.body()

        // Response: FeatureCollection of snapped points
        // Her feature geometry.coordinates = [lon, lat]
        val snapped = resp.features
            .mapNotNull { f ->
                val c = f.geometry?.coordinates ?: return@mapNotNull null
                if (c.size < 2) return@mapNotNull null
                LatLon(lat = c[1], lon = c[0])
            }

        return snapped.ifEmpty { points }
    }
}

@Serializable
data class OrsSnapRequest(
    val locations: List<List<Double>>,
    val radius: Int? = null
)

@Serializable
data class OrsSnapGeoJson(
    val type: String? = null,
    val features: List<OrsFeature> = emptyList()
)

@Serializable
data class OrsFeature(
    val type: String? = null,
    val geometry: OrsGeometry? = null
)

@Serializable
data class OrsGeometry(
    val type: String? = null,
    val coordinates: List<Double>? = null
)
