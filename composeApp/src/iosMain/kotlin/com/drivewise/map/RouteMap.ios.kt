package com.drivewise.map

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.drivewise.matching.LatLon

@Composable
actual fun RouteMap(points: List<LatLon>, modifier: Modifier) {
    Surface(modifier = modifier) {
        if (points.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No route points")
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        "Route preview (iOS placeholder) • ${points.size} pts",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                itemsIndexed(points.take(30)) { idx, p ->
                    Text("$idx  lat=${p.lat}  lon=${p.lon}", style = MaterialTheme.typography.bodySmall)
                }
                if (points.size > 30) {
                    item { Text("… +${points.size - 30} more") }
                }
            }
        }
    }
}
