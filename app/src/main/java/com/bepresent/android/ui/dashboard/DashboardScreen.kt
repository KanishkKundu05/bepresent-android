package com.bepresent.android.ui.dashboard

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bepresent.android.R
import com.bepresent.android.data.db.AppIntention
import com.bepresent.android.data.db.PresentSession
import com.bepresent.android.ui.components.IntentionRow
import com.bepresent.android.ui.components.formatDuration
import com.bepresent.android.ui.intention.IntentionConfigSheet
import com.bepresent.android.ui.picker.AppPickerSheet
import com.bepresent.android.ui.picker.InstalledApp
import com.bepresent.android.ui.session.SessionConfigSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private val HomeBluePrimary = Color(0xFF003BFF)
private val HomeBlue200 = Color(0xFF55B7FF)
private val HomeBlue300 = Color(0xFFABDDFF)
private val HomeNeutral100 = Color(0xFFF9F9F9)
private val HomeNeutral200 = Color(0xFFE6E6E6)
private val HomeNeutral400 = Color(0xFFCBCBCB)
private val HomeNeutral800 = Color(0xFF777777)
private val HomeBlack = Color(0xFF000000)
private val HomeGreen = Color(0xFF32BC00)
private val HomeBrand100 = Color(0xFFD9EEFE)
private val HomeOrangeFill = Color(0xFFFFEDE5)
private val HomeYellowFill = Color(0xFFFFF9E5)
private val HomeButtonShadow = Color(0xFF00249B)

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onProfileClick: () -> Unit = {},
    onLeaderboardClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    var showAppPicker by remember { mutableStateOf(false) }
    var showIntentionConfig by remember { mutableStateOf(false) }
    var selectedAppForIntention by remember { mutableStateOf<InstalledApp?>(null) }
    var editingIntention by remember { mutableStateOf<AppIntention?>(null) }
    var showSessionConfig by remember { mutableStateOf(false) }
    val sessionSelectedApps = remember { mutableStateListOf<InstalledApp>() }
    var showSessionAppPicker by remember { mutableStateOf(false) }
    var lastSessionDurationMinutes by rememberSaveable { mutableIntStateOf(30) }

    Box(modifier = Modifier.fillMaxSize()) {
        HomeV2Background()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            HomeHeader(
                streak = uiState.maxStreak,
                xp = uiState.totalXp,
                onProfileClick = onProfileClick,
                onXpClick = onLeaderboardClick,
                onStreakClick = onLeaderboardClick
            )

            if (!uiState.permissionsOk) {
                PermissionBanner()
            }

            if (uiState.activeSession == null) {
                WeekCalendar(days = uiState.weekCalendar)
            }

            HomeMainCard {
                if (uiState.activeSession == null) {
                    BlockedTimeCardContent(
                        blockedTodaySeconds = uiState.blockedTodaySeconds,
                        dailyRecordSeconds = uiState.dailyRecordSeconds,
                        sessionModeText = if (sessionSelectedApps.isEmpty()) {
                            "Specific apps"
                        } else {
                            "${sessionSelectedApps.size} apps"
                        },
                        sessionDurationText = formatSessionDuration(lastSessionDurationMinutes),
                        onSessionModeClick = { showSessionConfig = true },
                        onSessionDurationClick = { showSessionConfig = true },
                        onBlockNowClick = { showSessionConfig = true }
                    )
                } else {
                    ActiveSessionCardContent(
                        session = uiState.activeSession,
                        onGiveUp = { viewModel.giveUpSession() },
                        onComplete = { viewModel.completeSession() },
                        onCancel = { viewModel.cancelSession() }
                    )
                }
            }

            IntentionRow(
                intentions = uiState.intentions,
                onAddClick = { showAppPicker = true },
                onIntentionClick = { intention -> editingIntention = intention }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showAppPicker) {
        val existingPackages = uiState.intentions.map { it.packageName }.toSet()
        AppPickerSheet(
            multiSelect = false,
            excludePackages = existingPackages,
            onDismiss = { showAppPicker = false },
            onAppsSelected = { apps ->
                showAppPicker = false
                if (apps.isNotEmpty()) {
                    selectedAppForIntention = apps.first()
                    showIntentionConfig = true
                }
            }
        )
    }

    if (showIntentionConfig && selectedAppForIntention != null) {
        IntentionConfigSheet(
            appName = selectedAppForIntention!!.label,
            onDismiss = {
                showIntentionConfig = false
                selectedAppForIntention = null
            },
            onSave = { opens, time ->
                viewModel.createIntention(
                    packageName = selectedAppForIntention!!.packageName,
                    appName = selectedAppForIntention!!.label,
                    allowedOpensPerDay = opens,
                    timePerOpenMinutes = time
                )
                showIntentionConfig = false
                selectedAppForIntention = null
            }
        )
    }

    if (editingIntention != null) {
        IntentionConfigSheet(
            appName = editingIntention!!.appName,
            existingIntention = editingIntention,
            onDismiss = { editingIntention = null },
            onSave = { opens, time ->
                viewModel.updateIntention(
                    editingIntention!!.copy(
                        allowedOpensPerDay = opens,
                        timePerOpenMinutes = time
                    )
                )
                editingIntention = null
            },
            onDelete = {
                viewModel.deleteIntention(editingIntention!!)
                editingIntention = null
            }
        )
    }

    if (showSessionConfig) {
        SessionConfigSheet(
            onDismiss = {
                showSessionConfig = false
            },
            onOpenAppPicker = { showSessionAppPicker = true },
            selectedApps = sessionSelectedApps,
            onStart = { name, duration, beastMode ->
                viewModel.startSession(
                    name = name,
                    durationMinutes = duration,
                    blockedPackages = sessionSelectedApps.map { it.packageName },
                    beastMode = beastMode
                )
                lastSessionDurationMinutes = duration
                showSessionConfig = false
            }
        )
    }

    if (showSessionAppPicker) {
        AppPickerSheet(
            multiSelect = true,
            onDismiss = { showSessionAppPicker = false },
            onAppsSelected = { apps ->
                sessionSelectedApps.clear()
                sessionSelectedApps.addAll(apps)
                showSessionAppPicker = false
            }
        )
    }
}

@Composable
private fun HomeV2Background() {
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, -size.height / 2f)
            val radius = size.height
            drawRect(color = Color.White)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(HomeBlue300, HomeBlue200, HomeBluePrimary),
                    center = center,
                    radius = radius
                ),
                center = center,
                radius = radius
            )
        }
        androidx.compose.foundation.Image(
            painter = painterResource(id = R.drawable.home_v2_cloud_bg),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(0.25f)
                .align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun HomeHeader(
    streak: Int,
    xp: Int,
    onProfileClick: () -> Unit,
    onStreakClick: () -> Unit,
    onXpClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.9f))
                .clickable(onClick = onProfileClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = HomeBluePrimary
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        MetricChip(
            emoji = "🔥",
            value = streak.toString(),
            background = HomeOrangeFill,
            onClick = onStreakClick
        )

        Spacer(modifier = Modifier.width(8.dp))

        MetricChip(
            emoji = "⚡",
            value = "$xp XP",
            background = HomeYellowFill,
            onClick = onXpClick
        )
    }
}

@Composable
private fun MetricChip(
    emoji: String,
    value: String,
    background: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = emoji, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = value,
            color = HomeBlack,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium)
        )
    }
}

@Composable
private fun PermissionBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEDE5))
    ) {
        Text(
            text = "Enable all permissions so BePresent can block apps reliably.",
            color = Color(0xFFAA0000),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(14.dp)
        )
    }
}

@Composable
private fun WeekCalendar(days: List<HomeCalendarDay>) {
    val paddings = listOf(6, 5, 2, 2, 2, 5, 6)
    val offsets = listOf(15, -10, -25, -25, -25, -10, 15)
    val rotations = listOf(-18f, -15f, -10f, 0f, 10f, 15f, 18f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        days.forEachIndexed { index, day ->
            Box(
                modifier = Modifier
                    .padding(horizontal = paddings.getOrElse(index) { 0 }.dp)
                    .offset(y = offsets.getOrElse(index) { 0 }.dp)
                    .rotate(rotations.getOrElse(index) { 0f })
            ) {
                CalendarDayChip(day = day)
            }
        }
    }
}

@Composable
private fun CalendarDayChip(day: HomeCalendarDay) {
    val checkmarkSize = if (day.isCurrentDay) 60.dp else 48.dp
    val containerColor = when {
        !day.isEnabled -> Color.White.copy(alpha = 0.15f)
        day.isCurrentDay -> Color.White
        else -> Color.White.copy(alpha = 0.28f)
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(containerColor)
            .padding(top = 10.dp, bottom = 8.dp, start = 8.dp, end = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = day.weekDay,
            color = if (day.isCurrentDay) HomeBluePrimary else Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = day.dayNumber,
            color = if (day.isCurrentDay) HomeBluePrimary else Color.White,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .size(checkmarkSize)
                .clip(CircleShape)
                .background(
                    when {
                        !day.isEnabled -> Color.Transparent
                        day.isChecked -> HomeGreen
                        else -> HomeNeutral200
                    }
                )
                .graphicsLayer {
                    shadowElevation = 4.dp.toPx()
                    shape = CircleShape
                    clip = true
                },
            contentAlignment = Alignment.Center
        ) {
            when {
                !day.isEnabled -> {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(CircleShape)
                            .background(Color.Transparent)
                            .padding(1.dp)
                            .background(Color.White.copy(alpha = 0.18f), CircleShape)
                    )
                }
                day.isChecked -> {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = HomeNeutral800.copy(alpha = 0.5f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeMainCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = 2.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(HomeBlack.copy(alpha = 0.1f))
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = HomeNeutral100,
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 10.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 24.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun BlockedTimeCardContent(
    blockedTodaySeconds: Long,
    dailyRecordSeconds: Long,
    sessionModeText: String,
    sessionDurationText: String,
    onSessionModeClick: () -> Unit,
    onSessionDurationClick: () -> Unit,
    onBlockNowClick: () -> Unit
) {
    val (hours, minutes, seconds) = toHms(blockedTodaySeconds)
    val hasRecord = dailyRecordSeconds > 0L

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Time Blocked today",
            color = HomeBlack,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium)
        )

        Spacer(modifier = Modifier.height(10.dp))

        RecordPill(
            text = if (hasRecord) {
                "Your daily record: ${formatRecord(dailyRecordSeconds)}"
            } else {
                "No record set yet"
            },
            highlighted = hasRecord
        )

        Spacer(modifier = Modifier.height(28.dp))

        TimeDigits(hours = hours, minutes = minutes, seconds = seconds)

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SessionMetaChip(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.GridView,
                title = sessionModeText,
                onClick = onSessionModeClick
            )
            SessionMetaChip(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Schedule,
                title = sessionDurationText,
                onClick = onSessionDurationClick
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        HomePrimaryButton(
            title = "Block Now",
            onClick = onBlockNowClick
        )
    }
}

@Composable
private fun RecordPill(text: String, highlighted: Boolean) {
    Text(
        text = text,
        color = if (highlighted) HomeBluePrimary else HomeNeutral800,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier
            .clip(CircleShape)
            .background(if (highlighted) HomeBrand100 else HomeNeutral200)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    )
}

@Composable
private fun TimeDigits(hours: Int, minutes: Int, seconds: Int) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.Center
    ) {
        DigitColumn(label = "Hours", value = hours)
        Text(
            text = ":",
            color = HomeBlack.copy(alpha = 0.22f),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
        DigitColumn(label = "Minutes", value = minutes)
        Text(
            text = ":",
            color = HomeBlack.copy(alpha = 0.22f),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
        DigitColumn(label = "Seconds", value = seconds)
    }
}

@Composable
private fun DigitColumn(label: String, value: Int) {
    val animatedValue by animateIntAsState(targetValue = value, label = "digit-$label")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 5.dp)
                .graphicsLayer {
                    shadowElevation = 6.dp.toPx()
                    shape = RoundedCornerShape(12.dp)
                    clip = true
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = animatedValue.toString().padStart(2, '0'),
                color = if (animatedValue == 0) HomeBlack.copy(alpha = 0.28f) else HomeBlack,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
            )
        }
        Text(
            text = label,
            color = HomeNeutral400,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun SessionMetaChip(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.8f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = HomeNeutral800,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = HomeNeutral800,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = HomeNeutral400,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun HomePrimaryButton(
    title: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = 4.dp)
                .clip(CircleShape)
                .background(HomeButtonShadow)
        )
        Button(
            onClick = onClick,
            shape = CircleShape,
            modifier = Modifier.matchParentSize(),
            colors = ButtonDefaults.buttonColors(
                containerColor = HomeBluePrimary,
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
}

@Composable
private fun ActiveSessionCardContent(
    session: PresentSession,
    onGiveUp: () -> Unit,
    onComplete: () -> Unit,
    onCancel: () -> Unit
) {
    val isGoalReached = session.state == PresentSession.STATE_GOAL_REACHED
    var now by remember(session.id) { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(session.id, session.state) {
        while (isActive && session.state == PresentSession.STATE_ACTIVE) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }

    val elapsed = now - (session.startedAt ?: now)
    val canCancel = elapsed <= 10_000

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isGoalReached) "Session complete!" else "Session in progress",
            color = HomeBlack,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isGoalReached) {
                "Claim your XP or keep blocking."
            } else {
                "${session.name} • ${formatDuration(elapsed)}"
            },
            color = HomeNeutral800,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (isGoalReached) {
            HomePrimaryButton(
                title = "Complete Session",
                onClick = onComplete
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (canCancel) {
                    ActiveActionButton(
                        modifier = Modifier.weight(1f),
                        title = "Cancel",
                        color = HomeNeutral200,
                        onClick = onCancel
                    )
                }
                if (!session.beastMode) {
                    ActiveActionButton(
                        modifier = Modifier.weight(1f),
                        title = "Give Up",
                        color = Color(0xFFFFF4F4),
                        textColor = Color(0xFFAA0000),
                        onClick = onGiveUp
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveActionButton(
    modifier: Modifier = Modifier,
    title: String,
    color: Color,
    textColor: Color = HomeBlack,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = textColor,
            style = MaterialTheme.typography.titleSmall
        )
    }
}

private fun toHms(totalSeconds: Long): Triple<Int, Int, Int> {
    val hours = (totalSeconds / 3600L).toInt()
    val minutes = ((totalSeconds % 3600L) / 60L).toInt()
    val seconds = (totalSeconds % 60L).toInt()
    return Triple(hours, minutes, seconds)
}

private fun formatRecord(seconds: Long): String {
    val (h, m, s) = toHms(seconds)
    return "${h}h ${m}m ${s}s"
}

private fun formatSessionDuration(minutes: Int): String {
    return if (minutes >= 60) {
        val h = minutes / 60
        val m = minutes % 60
        if (m == 0) "${h}h" else "${h}h ${m}m"
    } else {
        "${minutes}m"
    }
}
