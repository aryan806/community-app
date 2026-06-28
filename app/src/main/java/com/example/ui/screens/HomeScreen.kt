package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.ElectricBolt
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.clickable
import com.example.data.Issue
import com.example.data.issueCategories
import com.example.ui.theme.IconBg
import com.example.ui.theme.StatBlueBg
import com.example.ui.theme.StatBlueText
import com.example.ui.theme.StatGreenBg
import com.example.ui.theme.StatGreenText
import com.example.ui.theme.StatOrangeBg
import com.example.ui.theme.StatOrangeText
import com.example.ui.theme.StatPurpleBg
import com.example.ui.theme.StatPurpleText
import com.example.ui.theme.HealthScoreGood

import com.example.data.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    currentUser: User?,
    onNavigateToReport: () -> Unit,
    onNavigateToIssue: (String) -> Unit,
    onNavigateToMap: () -> Unit
) {
    val issues by viewModel.recentIssues.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToReport,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Report Issue", modifier = Modifier.size(32.dp))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            item {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "COMMUNITY HERO AI",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val greeting = if (currentUser?.name?.isNotBlank() == true) "Hello, ${currentUser.name.split(" ").first()}" else "Hello, Hero"
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Stars, contentDescription = "Level", modifier = Modifier.size(16.dp), tint = StatOrangeText)
                            Spacer(modifier = Modifier.width(4.dp))
                            val levelStr = if (currentUser != null) "Lvl ${currentUser.level} • ${currentUser.rank} • ${currentUser.points} pts" else "Lvl 1 • Novice • 0 pts"
                            Text(
                                text = levelStr,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = StatOrangeText
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            item {
                // Search
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search issues or locations") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Health Score Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "COMMUNITY HEALTH",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            text = stats.healthScore.toString(),
                                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                                            color = HealthScoreGood
                                        )
                                        Text(
                                            text = "/100",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(StatGreenBg, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Stars, contentDescription = null, tint = StatGreenText, modifier = Modifier.size(32.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                HealthStatItem("Critical", stats.criticalIssues.toString(), MaterialTheme.colorScheme.error)
                                HealthStatItem("High Priority", stats.highPriorityIssues.toString(), StatOrangeText)
                                HealthStatItem("Resolved (Wk)", stats.resolvedThisWeek.toString(), HealthScoreGood)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // AI Insights
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = StatPurpleBg),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Stars, contentDescription = "AI", tint = StatPurpleText, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AI INSIGHTS",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
                                    color = StatPurpleText
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            AiInsightRow("Most Reported", stats.mostReportedCategory)
                            Spacer(modifier = Modifier.height(8.dp))
                            AiInsightRow("Fastest Dept", stats.fastestDepartment)
                            Spacer(modifier = Modifier.height(8.dp))
                            AiInsightRow("Needs Attention", stats.areaNeedingAttention)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CATEGORIES",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val context = LocalContext.current
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { android.widget.Toast.makeText(context, "Categories coming soon", android.widget.Toast.LENGTH_SHORT).show() }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(issueCategories.take(4)) { category ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Card(
                                onClick = { /* TODO */ },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = IconBg),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(56.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val icon = when {
                                        category.contains("Water", ignoreCase = true) -> Icons.Default.WaterDrop
                                        category.contains("Light", ignoreCase = true) -> Icons.Default.ElectricBolt
                                        category.contains("Road", ignoreCase = true) || category.contains("Pothole", ignoreCase = true) -> Icons.Default.Build
                                        else -> Icons.Default.Warning
                                    }
                                    Icon(
                                        icon,
                                        contentDescription = category,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = category.take(10), // Limit length for display
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                
                // Mini Map Preview
                Card(
                    onClick = onNavigateToMap,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(160.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val cameraPositionState = com.google.maps.android.compose.rememberCameraPositionState()
                        
                        LaunchedEffect(issues) {
                            if (issues.isNotEmpty()) {
                                val first = issues.first()
                                cameraPositionState.position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
                                    com.google.android.gms.maps.model.LatLng(first.locationLat, first.locationLng), 
                                    11f
                                )
                            }
                        }

                        com.google.maps.android.compose.GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            uiSettings = com.google.maps.android.compose.MapUiSettings(zoomControlsEnabled = false, scrollGesturesEnabled = false, zoomGesturesEnabled = false)
                        ) {
                            issues.take(5).forEach { issue ->
                                val hue = when {
                                    issue.status == "Resolved" -> com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_GREEN
                                    issue.severity == "Critical" -> com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED
                                    issue.severity == "High" -> com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_ORANGE
                                    issue.severity == "Medium" -> com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_YELLOW
                                    else -> com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_AZURE
                                }
                                com.google.maps.android.compose.Marker(
                                    state = com.google.maps.android.compose.MarkerState(position = com.google.android.gms.maps.model.LatLng(issue.locationLat, issue.locationLng)),
                                    title = issue.title,
                                    icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(hue)
                                )
                            }
                        }
                        
                        Card(
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                        ) {
                            Text(
                                text = "View Full Map",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "RECENT REPORTS",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            val filteredIssues = if (searchQuery.isBlank()) {
                issues
            } else {
                issues.filter {
                    it.title.contains(searchQuery, ignoreCase = true) ||
                    it.locationName.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true)
                }
            }
            
            items(filteredIssues) { issue ->
                IssueCard(issue = issue, onNavigateToIssue = onNavigateToIssue)
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun HealthStatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(text = value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = color)
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun AiInsightRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = StatPurpleText.copy(alpha = 0.8f))
        Text(text = value, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = StatPurpleText)
    }
}

@Composable
fun IssueCard(issue: Issue, onNavigateToIssue: (String) -> Unit) {
    val isResolved = issue.status == "Resolved"
    val isCritical = issue.severity == "Critical"
    val iconBg = if (isResolved) StatGreenBg else if (isCritical) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.tertiaryContainer
    val iconColor = if (isResolved) StatGreenText else if (isCritical) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onTertiaryContainer
    val iconText = if (isResolved) "✓" else "!"

    Card(
        onClick = { onNavigateToIssue(issue.id) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(iconBg, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(iconText, color = iconColor, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = issue.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        if (isCritical) {
                            Badge(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError) {
                                Text("CRITICAL", modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Reported  •  ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val statusBg = when (issue.status) {
                            "Resolved" -> StatGreenBg
                            "In Progress" -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.secondaryContainer
                        }
                        val statusColor = when (issue.status) {
                            "Resolved" -> StatGreenText
                            "In Progress" -> MaterialTheme.colorScheme.onTertiaryContainer
                            else -> MaterialTheme.colorScheme.onSecondaryContainer
                        }
                        Surface(
                            color = statusBg,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = issue.status.uppercase(),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                color = statusColor
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Stars, contentDescription = "Upvotes", tint = StatOrangeText, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${issue.upvotes} Upvotes", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (!isResolved) {
                    Text(
                        text = "${issue.daysUnresolved} days unresolved",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
