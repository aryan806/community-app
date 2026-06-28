package com.communityheroai.ui.screens // FIXED: 19

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.communityheroai.data.Issue
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueDetailsScreen(
    viewModel: IssueDetailsViewModel, // FIXED: 4
    onNavigateBack: () -> Unit,
    onViewOnMap: (Double, Double, String) -> Unit // FIXED: 10
) {
    val issue by viewModel.issue.collectAsStateWithLifecycle() // FIXED: 4
    val comments by viewModel.comments.collectAsStateWithLifecycle() // FIXED: 9
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) } // FIXED: 10
    var showDialog by remember { mutableStateOf(false) } // FIXED: 11
    var newComment by remember { mutableStateOf("") } // FIXED: 9

    if (issue == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    val currentIssue = issue!! // FIXED: 4

    if (showDialog) { // FIXED: 11
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = { TextButton(onClick = { viewModel.verifyResolution(); showDialog = false }) { Text("Verify") } },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } },
            title = { Text("Verify Resolution") },
            text = { Text("Are you sure you want to verify this issue as resolved?") }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Issue Details", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                actions = {
                    IconButton(onClick = { 
                        val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply { // FIXED: 10
                            putExtra(android.content.Intent.EXTRA_TEXT, "Check out this issue: ${currentIssue.title} at ${currentIssue.locationName}")
                            type = "text/plain"
                        }
                        context.startActivity(android.content.Intent.createChooser(sendIntent, null))
                    }) { Icon(Icons.Default.Share, contentDescription = "Share") }
                    
                    Box {
                        IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, contentDescription = "More") } // FIXED: 10
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(text = { Text("Flag as Spam") }, onClick = { showMenu = false; android.widget.Toast.makeText(context, "Flagged as Spam", android.widget.Toast.LENGTH_SHORT).show() })
                            DropdownMenuItem(text = { Text("View on Map") }, onClick = { showMenu = false; onViewOnMap(currentIssue.locationLat, currentIssue.locationLng, currentIssue.title) })
                            DropdownMenuItem(text = { Text("Copy Link") }, onClick = {
                                showMenu = false
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Deep Link", "communityhero://issue/${currentIssue.id}"))
                                android.widget.Toast.makeText(context, "Link Copied", android.widget.Toast.LENGTH_SHORT).show()
                            })
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = MaterialTheme.colorScheme.surface) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.upvote() }) { Icon(Icons.Default.ThumbUp, contentDescription = "Upvote", tint = MaterialTheme.colorScheme.primary) } // FIXED: 8
                        Text("${currentIssue.upvotes}", fontWeight = FontWeight.Bold) // FIXED: 8
                        Spacer(modifier = Modifier.width(16.dp))
                        IconButton(onClick = { /* Scroll to comments */ }) { Icon(Icons.AutoMirrored.Filled.Comment, contentDescription = "Comment") }
                        Text("${comments.size}") // FIXED: 13
                    }
                    Button(onClick = { showDialog = true }, enabled = currentIssue.status != "Resolved") { Text("Verify Resolution") } // FIXED: 11
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            Box(modifier = Modifier.fillMaxWidth().height(300.dp).background(Color.DarkGray)) {
                coil.compose.AsyncImage(model = currentIssue.imageUrl.ifEmpty { "https://images.unsplash.com/photo-1515162816999-a0c47dc192f7?auto=format&fit=crop&q=80&w=1000" }, contentDescription = "Issue Image", modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                Box(modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)), startY = 300f)))
                Row(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) { Text(currentIssue.category, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall) }
                    Badge(containerColor = if (currentIssue.severity == "Critical" || currentIssue.severity == "High") MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer) { Text(currentIssue.severity.uppercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall) }
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = currentIssue.title, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Reported by Citizen", style = MaterialTheme.typography.labelMedium)
                            Text(formatDate(currentIssue.timestamp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                Text(currentIssue.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(24.dp))
                Text("Status Timeline", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(16.dp))
                Timeline(currentIssue.status) // FIXED: 4
                Spacer(modifier = Modifier.height(24.dp))
                Text("Location", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))
                Text(currentIssue.locationName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                val location = LatLng(currentIssue.locationLat, currentIssue.locationLng)
                val cameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(location, 15f) }
                Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp))) {
                    GoogleMap(modifier = Modifier.fillMaxSize(), cameraPositionState = cameraPositionState, uiSettings = com.google.maps.android.compose.MapUiSettings(zoomControlsEnabled = false)) {
                        val hue = when {
                            currentIssue.status == "Resolved" -> com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_GREEN
                            currentIssue.severity == "Critical" -> com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED
                            currentIssue.severity == "High" -> com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_ORANGE
                            currentIssue.severity == "Medium" -> com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_YELLOW
                            else -> com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_AZURE
                        }
                        Marker(state = MarkerState(position = location), title = currentIssue.title, icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(hue))
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Text("Issue ID: #${currentIssue.id}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                Spacer(modifier = Modifier.height(32.dp))
                Text("Comments (${comments.size})", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) // FIXED: 9
                Spacer(modifier = Modifier.height(8.dp))
                comments.forEach { comment -> // FIXED: 9
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(comment.authorName, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(comment.text, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) { // FIXED: 9
                    OutlinedTextField(value = newComment, onValueChange = { newComment = it }, modifier = Modifier.weight(1f), placeholder = { Text("Add a comment...") })
                    IconButton(onClick = { viewModel.submitComment(newComment, "Citizen"); newComment = "" }) { Icon(Icons.Default.Send, "Send") }
                }
            }
        }
    }
}

@Composable
fun Timeline(status: String) { // FIXED: 4
    val isResolved = status == "Resolved"
    Column {
        TimelineItem("Reported", "Recent", true)
        TimelineItem("Assigned", "Processing", status == "In Progress" || isResolved)
        TimelineItem("In Progress", "Working", status == "In Progress" || isResolved, isCurrent = status == "In Progress")
        TimelineItem("Resolved", "Done", isResolved, isCurrent = isResolved, isLast = true)
    }
}

@Composable
fun TimelineItem(title: String, subtitle: String, isCompleted: Boolean, isCurrent: Boolean = false, isLast: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(24.dp)) {
            Box(modifier = Modifier.size(16.dp).background(if (isCurrent) MaterialTheme.colorScheme.primary else if (isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant, CircleShape))
            if (!isLast) { Box(modifier = Modifier.width(2.dp).height(40.dp).background(if (isCompleted && !isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant)) }
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
