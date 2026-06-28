package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.data.Issue
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.text.SimpleDateFormat
import java.util.*

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueDetailsScreen(
    issueId: String,
    onNavigateBack: () -> Unit,
    viewModel: IssueDetailViewModel = viewModel(factory = IssueDetailViewModel.Factory)
) {
    val issue by viewModel.issue.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    LaunchedEffect(issueId) {
        viewModel.loadIssue(issueId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Issue Details", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val context = LocalContext.current
                    IconButton(onClick = { android.widget.Toast.makeText(context, "Share coming soon", android.widget.Toast.LENGTH_SHORT).show() }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { android.widget.Toast.makeText(context, "More coming soon", android.widget.Toast.LENGTH_SHORT).show() }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            if (issue != null) {
                BottomAppBar(containerColor = MaterialTheme.colorScheme.surface) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val context = LocalContext.current
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { android.widget.Toast.makeText(context, "Upvote coming soon", android.widget.Toast.LENGTH_SHORT).show() }) {
                                Icon(Icons.Default.ThumbUp, contentDescription = "Upvote", tint = MaterialTheme.colorScheme.primary)
                            }
                            Text("${issue!!.upvotes}", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(16.dp))
                            IconButton(onClick = { android.widget.Toast.makeText(context, "Comments coming soon", android.widget.Toast.LENGTH_SHORT).show() }) {
                                Icon(Icons.AutoMirrored.Filled.Comment, contentDescription = "Comment")
                            }
                            Text("12")
                        }
                        
                        Button(onClick = { android.widget.Toast.makeText(context, "Verify Resolution coming soon", android.widget.Toast.LENGTH_SHORT).show() }) {
                            Text("Verify Resolution")
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(text = error ?: "Unknown error", color = MaterialTheme.colorScheme.error)
            }
        } else if (issue == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(text = "Issue not found")
            }
        } else {
            val mockIssue = issue!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
            // Media Gallery Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color.DarkGray)
            ) {
                // Placeholder for actual image
                coil.compose.AsyncImage(
                    model = "https://images.unsplash.com/photo-1515162816999-a0c47dc192f7?auto=format&fit=crop&q=80&w=1000",
                    contentDescription = "Issue Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                
                // Gradient overlay
                Box(modifier = Modifier.fillMaxSize().background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                        startY = 300f
                    )
                ))
                
                // Badges
                Row(
                    modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                        Text(mockIssue.category, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
                    }
                    Badge(containerColor = if(mockIssue.severity == "Critical" || mockIssue.severity == "High") MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer) {
                        Text(mockIssue.severity.uppercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = mockIssue.title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Info Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Reported by Citizen", style = MaterialTheme.typography.labelMedium)
                            Text(formatDate(mockIssue.timestamp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Text("AI Concern Score", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Stars, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("85/100", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFFE65100))
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text("Description", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))
                Text(mockIssue.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Timeline
                Text("Status Timeline", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(16.dp))
                IssueStatusTimeline()
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Map
                Text("Location", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))
                Text(mockIssue.locationName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                
                val location = LatLng(mockIssue.locationLat, mockIssue.locationLng)
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(location, 15f)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        uiSettings = com.google.maps.android.compose.MapUiSettings(zoomControlsEnabled = false)
                    ) {
                        val hue = when {
                            mockIssue.status == "Resolved" -> com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_GREEN
                            mockIssue.severity == "Critical" -> com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED
                            mockIssue.severity == "High" -> com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_ORANGE
                            mockIssue.severity == "Medium" -> com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_YELLOW
                            else -> com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_AZURE
                        }
                        Marker(
                            state = MarkerState(position = location),
                            title = mockIssue.title,
                            icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(hue)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Meta info
                Text("Issue ID: #${mockIssue.id}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
}

@Composable
fun IssueStatusTimeline() {
    Column {
        TimelineItem("Reported", "Oct 12, 10:30 AM", true)
        TimelineItem("Assigned to Public Works", "Oct 12, 02:15 PM", true)
        TimelineItem("In Progress", "Oct 13, 09:00 AM", true, isCurrent = true)
        TimelineItem("Verification", "Pending", false)
        TimelineItem("Resolved", "Pending", false, isLast = true)
    }
}

@Composable
fun TimelineItem(title: String, subtitle: String, isCompleted: Boolean, isCurrent: Boolean = false, isLast: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(24.dp)) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(
                        if (isCurrent) MaterialTheme.colorScheme.primary 
                        else if (isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) 
                        else MaterialTheme.colorScheme.surfaceVariant, 
                        CircleShape
                    )
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(40.dp)
                        .background(
                            if (isCompleted && !isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) 
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.padding(bottom = if (!isLast) 16.dp else 0.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal), color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
