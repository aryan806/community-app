package com.communityheroai.ui.screens // FIXED: 19

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.annotation.SuppressLint
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    viewModel: HomeViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToIssue: (String) -> Unit,
    onNavigateToReport: () -> Unit
) {
    val issues by viewModel.recentIssues.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationPermissionState = rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)
    val cameraPositionState = rememberCameraPositionState()
    var hasSetInitialCamera by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(locationPermissionState.status) {
        if (!locationPermissionState.status.isGranted) {
            if (locationPermissionState.status.shouldShowRationale) {
                val result = snackbarHostState.showSnackbar("Location permission is needed to show your current location on the map.", actionLabel = "Retry")
                if (result == SnackbarResult.ActionPerformed) locationPermissionState.launchPermissionRequest()
            } else {
                locationPermissionState.launchPermissionRequest()
            }
        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    scope.launch { cameraPositionState.animate(update = CameraUpdateFactory.newLatLngZoom(userLatLng, 14f)) }
                    hasSetInitialCamera = true
                }
            }
        }
    }

    LaunchedEffect(issues) {
        if (!hasSetInitialCamera && issues.isNotEmpty()) {
            val first = issues.first()
            cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(first.locationLat, first.locationLng), 12f)
            hasSetInitialCamera = true
        }
    }

    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Critical", "High", "Medium", "Resolved")

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Issue Map", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                actions = { IconButton(onClick = { }) { Icon(Icons.Default.Search, contentDescription = "Search") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.9f))
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(myLocationButtonEnabled = false, zoomControlsEnabled = true, compassEnabled = true),
                properties = MapProperties(isMyLocationEnabled = locationPermissionState.status.isGranted),
                onMapLongClick = { onNavigateToReport() }
            ) {
                issues.forEach { issue ->
                    val showMarker = when (selectedFilter) {
                        "All" -> true
                        "Resolved" -> issue.status == "Resolved"
                        else -> issue.severity == selectedFilter && issue.status != "Resolved"
                    }
                    if (showMarker) {
                        val hue = when {
                            issue.status == "Resolved" -> BitmapDescriptorFactory.HUE_GREEN
                            issue.severity == "Critical" -> BitmapDescriptorFactory.HUE_RED
                            issue.severity == "High" -> BitmapDescriptorFactory.HUE_ORANGE
                            issue.severity == "Medium" -> BitmapDescriptorFactory.HUE_YELLOW
                            else -> BitmapDescriptorFactory.HUE_AZURE
                        }
                        MarkerInfoWindow(state = MarkerState(position = LatLng(issue.locationLat, issue.locationLng)), title = issue.title, snippet = "${issue.category} • ${issue.status}", icon = BitmapDescriptorFactory.defaultMarker(hue), onInfoWindowClick = { onNavigateToIssue(issue.id) }) { marker ->
                            Card(modifier = Modifier.padding(8.dp).width(200.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(marker.title ?: "", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(marker.snippet ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Tap for details", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                filters.forEach { filter ->
                    FilterChip(selected = selectedFilter == filter, onClick = { selectedFilter = filter }, label = { Text(filter) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer))
                }
            }

            FloatingActionButton(onClick = {
                if (locationPermissionState.status.isGranted) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            val userLatLng = LatLng(location.latitude, location.longitude)
                            scope.launch { cameraPositionState.animate(update = CameraUpdateFactory.newLatLngZoom(userLatLng, 14f)) }
                        }
                    }
                } else { locationPermissionState.launchPermissionRequest() }
            }, modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp), containerColor = MaterialTheme.colorScheme.primaryContainer) {
                Icon(Icons.Default.MyLocation, contentDescription = "Recenter")
            }
        }
    }
}
