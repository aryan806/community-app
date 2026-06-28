package com.communityheroai.ui.screens // FIXED: 19

import android.Manifest
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.communityheroai.data.Issue
import com.communityheroai.data.issueCategories
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapProperties
import android.annotation.SuppressLint
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: ReportViewModel,
    onNavigateBack: () -> Unit
) {
    var currentStep by remember { mutableStateOf(1) }
    val totalSteps = 4

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(issueCategories.first()) }
    var severity by remember { mutableStateOf("Medium") }
    var locationName by remember { mutableStateOf("") }
    var landmark by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf(LatLng(0.0, 0.0)) }
    var imageUri by remember { mutableStateOf<android.net.Uri?>(null) } // FIXED: 5

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isSuccess by viewModel.isSuccess.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle() // FIXED: 21
    val aiSuggestion by viewModel.aiSuggestion.collectAsStateWithLifecycle() // FIXED: 6

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(isSuccess) {
        if (isSuccess) onNavigateBack()
    }
    
    LaunchedEffect(error) { // FIXED: 21
        error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }, // FIXED: 21
        topBar = {
            TopAppBar(
                title = { Text("Report Issue", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { if (currentStep > 1) currentStep-- else onNavigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { 
                        viewModel.saveDraft(title, description, category, severity, locationName, selectedLocation.latitude, selectedLocation.longitude) // FIXED: 7
                        android.widget.Toast.makeText(context, "Draft Saved", android.widget.Toast.LENGTH_SHORT).show()
                    }) {
                        Text("Save Draft", color = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = MaterialTheme.colorScheme.surface) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    if (currentStep > 1) {
                        OutlinedButton(onClick = { currentStep-- }) { Text("Back") }
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Button(
                        onClick = {
                            if (currentStep < totalSteps) {
                                currentStep++
                            } else {
                                viewModel.submitIssue(
                                    Issue(title = title, description = description, category = category, locationName = locationName, severity = severity, locationLat = selectedLocation.latitude, locationLng = selectedLocation.longitude),
                                    imageUri // FIXED: 5
                                )
                            }
                        },
                        enabled = !isLoading
                    ) {
                        if (isLoading && currentStep == totalSteps) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text(if (currentStep == totalSteps) "Submit Report" else "Next Step")
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LinearProgressIndicator(progress = { currentStep.toFloat() / totalSteps }, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceVariant)
            Text(text = "Step $currentStep of $totalSteps", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    slideInHorizontally(initialOffsetX = { if (targetState > initialState) it else -it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { if (targetState > initialState) -it else it })
                }, label = "wizard_step"
            ) { step ->
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp)) {
                    when (step) {
                        1 -> Step1Media(onImageSelected = { imageUri = it }) // FIXED: 5
                        2 -> Step2Details(title, { title = it }, description, { description = it }, category, { category = it }, severity, { severity = it }, aiSuggestion, { viewModel.suggestCategoryAndSeverity(description) }) // FIXED: 6
                        3 -> Step3Location(locationName, { locationName = it }, landmark, { landmark = it }, selectedLocation, { selectedLocation = it })
                        4 -> Step4Preview(title, description, category, severity, locationName)
                    }
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Step1Media(onImageSelected: (android.net.Uri?) -> Unit) { // FIXED: 5
    var selectedImages by remember { mutableStateOf<List<android.net.Uri>>(emptyList()) }
    val galleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedImages = (selectedImages + uris).take(4)
        onImageSelected(selectedImages.firstOrNull()) // FIXED: 5
    }

    val context = LocalContext.current
    val cameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val uri = saveBitmapToTempFile(context, bitmap)
            if (uri != null) {
                selectedImages = (selectedImages + uri).take(4)
                onImageSelected(selectedImages.firstOrNull())
            }
        }
    }
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    Column {
        Text("Upload Media Evidence", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(8.dp))
        Text("Photos and videos help authorities assess the issue faster.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))

        if (selectedImages.isNotEmpty()) {
            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                items(selectedImages.size) { index ->
                    Box(modifier = Modifier.size(100.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                        coil.compose.AsyncImage(model = selectedImages[index], contentDescription = "Selected image", modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                        IconButton(onClick = {
                            val newList = selectedImages.toMutableList()
                            newList.removeAt(index)
                            selectedImages = newList
                            onImageSelected(newList.firstOrNull())
                        }, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape).padding(4.dp))
                        }
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            MediaBox(Icons.Default.CameraAlt, "Take Photo", Modifier.weight(1f)) {
                if (cameraPermissionState.status.isGranted) {
                    cameraLauncher.launch(null)
                } else {
                    cameraPermissionState.launchPermissionRequest()
                }
            }
            MediaBox(Icons.Default.Videocam, "Record Video", Modifier.weight(1f)) {}
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            MediaBox(Icons.Default.Image, "Upload Gallery", Modifier.weight(1f)) { galleryLauncher.launch("image/*") }
            MediaBox(Icons.Default.Mic, "Voice Note", Modifier.weight(1f)) {}
        }
        Spacer(modifier = Modifier.height(32.dp))
        Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp)).clickable { galleryLauncher.launch("image/*") }, contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Tap to browse files", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

fun saveBitmapToTempFile(context: android.content.Context, bitmap: android.graphics.Bitmap): android.net.Uri? {
    return try {
        val file = java.io.File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        val out = java.io.FileOutputStream(file)
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, out)
        out.flush()
        out.close()
        android.net.Uri.fromFile(file)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun MediaBox(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = modifier.height(100.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step2Details(
    title: String, onTitleChange: (String) -> Unit,
    description: String, onDescChange: (String) -> Unit,
    category: String, onCatChange: (String) -> Unit,
    severity: String, onSevChange: (String) -> Unit,
    aiSuggestion: AiSuggestion?, onAiAssist: () -> Unit // FIXED: 6
) {
    var isCatExpanded by remember { mutableStateOf(false) }
    var isSevExpanded by remember { mutableStateOf(false) }
    
    if (aiSuggestion != null) { // FIXED: 6
        LaunchedEffect(aiSuggestion) {
            onCatChange(aiSuggestion.category)
            onSevChange(aiSuggestion.severity)
        }
    }

    Column {
        Text("Issue Details", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(value = title, onValueChange = onTitleChange, label = { Text("Issue Title") }, placeholder = { Text("e.g., Large pothole on Main St") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(16.dp))
        ExposedDropdownMenuBox(expanded = isCatExpanded, onExpandedChange = { isCatExpanded = !isCatExpanded }) {
            OutlinedTextField(value = category, onValueChange = {}, readOnly = true, label = { Text("Category") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCatExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true), shape = RoundedCornerShape(12.dp))
            ExposedDropdownMenu(expanded = isCatExpanded, onDismissRequest = { isCatExpanded = false }) {
                issueCategories.forEach { cat -> DropdownMenuItem(text = { Text(cat) }, onClick = { onCatChange(cat); isCatExpanded = false }) }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        ExposedDropdownMenuBox(expanded = isSevExpanded, onExpandedChange = { isSevExpanded = !isSevExpanded }) {
            OutlinedTextField(value = severity, onValueChange = {}, readOnly = true, label = { Text("Severity") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isSevExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true), shape = RoundedCornerShape(12.dp))
            ExposedDropdownMenu(expanded = isSevExpanded, onDismissRequest = { isSevExpanded = false }) {
                listOf("Low", "Medium", "High", "Critical").forEach { sev -> DropdownMenuItem(text = { Text(sev) }, onClick = { onSevChange(sev); isSevExpanded = false }) }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = description, onValueChange = onDescChange, label = { Text("Description") }, modifier = Modifier.fillMaxWidth().height(120.dp), shape = RoundedCornerShape(12.dp), maxLines = 5)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onAiAssist) { // FIXED: 6
                Icon(Icons.Default.Stars, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("AI Assist Description")
            }
        }
        
        if (aiSuggestion != null) { // FIXED: 6
            Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                Text(aiSuggestion.summary, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
            }
        }
    }
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Step3Location(
    locationName: String, onLocChange: (String) -> Unit,
    landmark: String, onLandmarkChange: (String) -> Unit,
    selectedLocation: LatLng, onLatLngChange: (LatLng) -> Unit
) {
    val cameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(selectedLocation, 15f) }
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val scope = rememberCoroutineScope()
    val locationPermissionState = rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)

    LaunchedEffect(locationPermissionState.status) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        } else if (selectedLocation.latitude == 0.0 && selectedLocation.longitude == 0.0) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    onLatLngChange(userLatLng)
                    scope.launch { cameraPositionState.animate(update = CameraUpdateFactory.newLatLngZoom(userLatLng, 15f)) }
                }
            }
        }
    }

    Column {
        Text("Pinpoint Location", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp)).background(Color.Gray)) {
            GoogleMap(modifier = Modifier.fillMaxSize(), cameraPositionState = cameraPositionState, uiSettings = MapUiSettings(myLocationButtonEnabled = false), properties = MapProperties(isMyLocationEnabled = locationPermissionState.status.isGranted), onMapClick = { onLatLngChange(it) }) {
                Marker(state = MarkerState(position = selectedLocation), title = "Issue Location", icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED))
            }
            FloatingActionButton(onClick = {
                if (locationPermissionState.status.isGranted) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            val userLatLng = LatLng(location.latitude, location.longitude)
                            onLatLngChange(userLatLng)
                            scope.launch { cameraPositionState.animate(update = CameraUpdateFactory.newLatLngZoom(userLatLng, 15f)) }
                        }
                    }
                } else { locationPermissionState.launchPermissionRequest() }
            }, modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).size(40.dp), containerColor = MaterialTheme.colorScheme.surface) {
                Icon(Icons.Default.MyLocation, contentDescription = "My Location")
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(value = locationName, onValueChange = onLocChange, label = { Text("Address (Auto-filled)") }, leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = landmark, onValueChange = onLandmarkChange, label = { Text("Nearby Landmark (Optional)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
    }
}

@Composable
fun Step4Preview(title: String, description: String, category: String, severity: String, locationName: String) {
    Column {
        Text("Review & Submit", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(8.dp))
        Text("Please review the details before submitting your report to the authorities.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(title.ifEmpty { "Untitled Issue" }, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Category", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(category, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Severity", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(severity, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (severity == "Critical") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Location", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(locationName.ifEmpty { "Unknown location" }, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Description", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(description.ifEmpty { "No description provided." }, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
