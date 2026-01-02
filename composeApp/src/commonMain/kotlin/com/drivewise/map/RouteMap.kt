package com.drivewise.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.drivewise.matching.LatLon

@Composable
expect fun RouteMap(points: List<LatLon>, modifier: Modifier = Modifier)