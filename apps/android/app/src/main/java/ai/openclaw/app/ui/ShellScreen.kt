package ai.openclaw.app.ui

import ai.openclaw.app.BuildConfig
import ai.openclaw.app.GatewayChannelsSummary
import ai.openclaw.app.GatewayDreamingSummary
import ai.openclaw.app.GatewayNodesDevicesSummary
import ai.openclaw.app.GatewaySkillSummary
import ai.openclaw.app.HomeDestination
import ai.openclaw.app.MainViewModel
import ai.openclaw.app.NodeRuntime
import ai.openclaw.app.ui.chat.ChatScreen
import ai.openclaw.app.ui.design.ClawDesignTheme
import ai.openclaw.app.ui.design.ClawPanel
import ai.openclaw.app.ui.design.ClawScaffold
import ai.openclaw.app.ui.design.ClawTheme
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ScreenShare
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.MicNone
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private enum class Tab(
  val key: String,
  val label: String,
  val icon: ImageVector,
) {
  Chat(key = "chat", label = "Chat", icon = Icons.Outlined.ChatBubbleOutline),
  Devices(key = "devices", label = "Devices", icon = Icons.Outlined.Inventory2),
  Live(key = "live", label = "Live", icon = Icons.AutoMirrored.Filled.ScreenShare),
  Tasks(key = "tasks", label = "Tasks", icon = Icons.Outlined.Settings),
  Memory(key = "memory", label = "Memory", icon = Icons.Default.Storage),
  Settings(key = "settings", label = "Settings", icon = Icons.Outlined.Settings),
  Voice(key = "voice", label = "Voice", icon = Icons.Outlined.MicNone),
  Sessions(key = "sessions", label = "Sessions", icon = Icons.Outlined.AccessTime),
  ProvidersModels(key = "providers-models", label = "Providers", icon = Icons.Default.Storage),
}

@Composable
fun ShellScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier,
) {
  ClawDesignTheme {
    var activeTab by rememberSaveable { mutableStateOf(Tab.Chat) }
    var settingsRoute by rememberSaveable { mutableStateOf(SettingsRoute.Home) }
    var returnToChatFromSettings by rememberSaveable { mutableStateOf(false) }
    var commandOpen by rememberSaveable { mutableStateOf(false) }
    val requestedHomeDestination by viewModel.requestedHomeDestination.collectAsState()
    val pendingTrust by viewModel.pendingGatewayTrust.collectAsState()

    LaunchedEffect(requestedHomeDestination) {
      val destination = requestedHomeDestination ?: return@LaunchedEffect
      activeTab =
        when (destination) {
          HomeDestination.Connect -> Tab.Chat
          HomeDestination.Chat -> Tab.Chat
          HomeDestination.Voice -> Tab.Voice
          HomeDestination.Screen -> Tab.Live
          HomeDestination.Settings -> Tab.Settings
        }
      if (destination == HomeDestination.Settings) {
        settingsRoute = SettingsRoute.Home
        returnToChatFromSettings = false
      }
      viewModel.clearRequestedHomeDestination()
    }

    LaunchedEffect(activeTab) {
      viewModel.setVoiceScreenActive(activeTab == Tab.Voice)
    }

    BackHandler(enabled = activeTab != Tab.Chat) {
      activeTab = Tab.Chat
    }

    BackHandler(enabled = commandOpen) {
      commandOpen = false
    }

    androidx.compose.material3.Scaffold(
      modifier = modifier.fillMaxSize(),
      containerColor = Color.Transparent,
      contentWindowInsets =
        androidx.compose.foundation.layout
          .WindowInsets(0, 0, 0, 0),
      bottomBar = {
        val hideBottomBar = activeTab == Tab.Voice || activeTab == Tab.Sessions || activeTab == Tab.ProvidersModels
        if (!hideBottomBar) {
          val safeInsets =
            androidx.compose.foundation.layout.WindowInsets.navigationBars
              .only(androidx.compose.foundation.layout.WindowInsetsSides.Bottom + androidx.compose.foundation.layout.WindowInsetsSides.Horizontal)
          val mainTabs = listOf(Tab.Chat, Tab.Devices, Tab.Live, Tab.Tasks, Tab.Memory, Tab.Settings)
          Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
              modifier = Modifier.fillMaxWidth(),
              color = ClawTheme.colors.surfaceRaised,
              shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
              border = BorderStroke(1.dp, ClawTheme.colors.border),
              shadowElevation = 6.dp,
            ) {
              Row(
                modifier =
                  Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(safeInsets)
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                mainTabs.forEach { tab ->
                  val active = tab == activeTab
                  Surface(
                    onClick = { activeTab = tab },
                    modifier = Modifier.weight(1f).heightIn(min = 58.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = if (active) ClawTheme.colors.primary else Color.Transparent,
                    shadowElevation = 0.dp,
                  ) {
                    Column(
                      modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 7.dp),
                      horizontalAlignment = Alignment.CenterHorizontally,
                      verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                      Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = if (active) ClawTheme.colors.primaryText else ClawTheme.colors.textMuted,
                      )
                      Text(
                        text = tab.label,
                        color = if (active) ClawTheme.colors.primaryText else ClawTheme.colors.textMuted,
                        style = ClawTheme.type.caption.copy(fontWeight = if (active) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium),
                      )
                    }
                  }
                }
              }
            }
          }
        }
      },
    ) { innerPadding ->
      Box(modifier = Modifier.fillMaxSize().padding(innerPadding).consumeWindowInsets(innerPadding)) {
        when (activeTab) {
          Tab.Chat ->
            ChatScreen(viewModel = viewModel, onBack = { activeTab = Tab.Chat }, onVoice = { activeTab = Tab.Voice })
          Tab.Devices ->
            DevicesScreen(viewModel = viewModel)
          Tab.Live ->
            LiveScreen(viewModel = viewModel)
          Tab.Tasks ->
            TasksScreen(viewModel = viewModel)
          Tab.Memory ->
            MemoryScreen(viewModel = viewModel)
          Tab.Settings ->
            SettingsShellScreen(
              viewModel = viewModel,
              route = settingsRoute,
              onRouteChange = {
                settingsRoute = it
                returnToChatFromSettings = false
              },
              onRouteBack = {
                settingsRoute = SettingsRoute.Home
                if (returnToChatFromSettings) {
                  returnToChatFromSettings = false
                  activeTab = Tab.Chat
                }
              },
              onOpenCommand = { commandOpen = true },
            )
          Tab.Voice ->
            VoiceShellScreen(
              viewModel = viewModel,
              onOpenCommand = { commandOpen = true },
              onOpenVoiceSettings = {
                settingsRoute = SettingsRoute.Voice
                returnToChatFromSettings = false
                activeTab = Tab.Settings
              },
            )
          Tab.ProvidersModels ->
            ProvidersModelsScreen(
              viewModel = viewModel,
              onBack = { activeTab = Tab.Chat },
              onAddProvider = {
                settingsRoute = SettingsRoute.Gateway
                returnToChatFromSettings = false
                activeTab = Tab.Settings
              },
            )
          Tab.Sessions ->
            SessionsScreen(
              viewModel = viewModel,
              onOpenCommand = { commandOpen = true },
              onOpenChat = { activeTab = Tab.Chat },
            )
        }

        if (commandOpen) {
          CommandPalette(
            viewModel = viewModel,
            onDismiss = { commandOpen = false },
            onOpenChat = {
              activeTab = Tab.Chat
              commandOpen = false
            },
            onOpenVoice = {
              activeTab = Tab.Voice
              commandOpen = false
            },
            onOpenSessions = {
              activeTab = Tab.Sessions
              commandOpen = false
            },
            onOpenProviders = {
              activeTab = Tab.ProvidersModels
              commandOpen = false
            },
            onOpenSettings = {
              settingsRoute = SettingsRoute.Home
              returnToChatFromSettings = false
              activeTab = Tab.Settings
              commandOpen = false
            },
            onOpenSession = { sessionKey ->
              viewModel.switchChatSession(sessionKey)
              activeTab = Tab.Chat
              commandOpen = false
            },
          )
        }
      }

      pendingTrust?.let { prompt ->
        GatewayTrustDialog(
          prompt = prompt,
          onAccept = viewModel::acceptGatewayTrustPrompt,
          onDecline = viewModel::declineGatewayTrustPrompt,
        )
      }
    }
  }
}

@Composable
private fun GatewayTrustDialog(
  prompt: NodeRuntime.GatewayTrustPrompt,
  onAccept: () -> Unit,
  onDecline: () -> Unit,
) {
  val message =
    if (prompt.previousFingerprintSha256.isNullOrBlank()) {
      "Verify the certificate fingerprint before trusting this gateway.\n\n${prompt.fingerprintSha256}"
    } else {
      "The gateway certificate changed. Continue only if you expected this.\n\nOld SHA-256:\n${prompt.previousFingerprintSha256}\n\nNew SHA-256:\n${prompt.fingerprintSha256}"
    }

  AlertDialog(
    onDismissRequest = onDecline,
    containerColor = ClawTheme.colors.surfaceRaised,
    title = { Text("Trust this gateway?", style = ClawTheme.type.section, color = ClawTheme.colors.text) },
    text = { Text(message, style = ClawTheme.type.body, color = ClawTheme.colors.textMuted) },
    confirmButton = {
      TextButton(onClick = onAccept) {
        Text("Trust")
      }
    },
    dismissButton = {
      TextButton(onClick = onDecline) {
        Text("Cancel")
      }
    },
  )
}

@Composable
private fun ChatShellScreen(
  viewModel: MainViewModel,
  onBack: () -> Unit,
  onVoice: () -> Unit,
) {
  ClawScaffold(contentPadding = PaddingValues(start = 0.dp, top = 8.dp, end = 0.dp, bottom = 8.dp)) {
    ChatScreen(viewModel = viewModel, onBack = onBack, onVoice = onVoice)
  }
}

@Composable
private fun VoiceShellScreen(
  viewModel: MainViewModel,
  onOpenCommand: () -> Unit,
  onOpenVoiceSettings: () -> Unit,
) {
  ClawScaffold(contentPadding = PaddingValues(start = 0.dp, top = 8.dp, end = 0.dp, bottom = 8.dp)) {
    VoiceScreen(viewModel = viewModel, onOpenCommand = onOpenCommand, onOpenVoiceSettings = onOpenVoiceSettings)
  }
}

@Composable
private fun SettingsShellScreen(
  viewModel: MainViewModel,
  route: SettingsRoute,
  onRouteChange: (SettingsRoute) -> Unit,
  onRouteBack: () -> Unit,
  onOpenCommand: () -> Unit,
) {
  val displayName by viewModel.displayName.collectAsState()
  val isConnected by viewModel.isConnected.collectAsState()
  val statusText by viewModel.statusText.collectAsState()
  val cameraEnabled by viewModel.cameraEnabled.collectAsState()
  val notificationForwardingEnabled by viewModel.notificationForwardingEnabled.collectAsState()
  val speakerEnabled by viewModel.speakerEnabled.collectAsState()
  val agents by viewModel.gatewayAgents.collectAsState()
  val pendingToolCalls by viewModel.chatPendingToolCalls.collectAsState()
  val cronStatus by viewModel.cronStatus.collectAsState()
  val usageSummary by viewModel.usageSummary.collectAsState()
  val skillsSummary by viewModel.skillsSummary.collectAsState()
  val nodesDevicesSummary by viewModel.nodesDevicesSummary.collectAsState()
  val channelsSummary by viewModel.channelsSummary.collectAsState()
  val dreamingSummary by viewModel.dreamingSummary.collectAsState()

  LaunchedEffect(isConnected) {
    if (isConnected) {
      viewModel.refreshAgents()
      viewModel.refreshCronJobs()
      viewModel.refreshUsage()
      viewModel.refreshSkills()
      viewModel.refreshNodesDevices()
      viewModel.refreshChannels()
      viewModel.refreshDreaming()
    }
  }

  BackHandler(enabled = route != SettingsRoute.Home) {
    onRouteBack()
  }

  if (route != SettingsRoute.Home) {
    SettingsDetailScreen(viewModel = viewModel, route = route, onBack = onRouteBack)
    return
  }

  ClawScaffold(contentPadding = PaddingValues(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 20.dp)) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(13.dp)) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
          Text(text = "Settings", style = ClawTheme.type.title.copy(fontSize = 16.sp, lineHeight = 20.sp), color = ClawTheme.colors.text, modifier = Modifier.weight(1f))
          SettingsSearchButton(onClick = onOpenCommand)
        }
      }

      item {
        ProfilePanel(displayName = displayName.ifBlank { "OpenClaw" }, onClick = { onRouteChange(SettingsRoute.Profile) })
      }

      item {
        SettingsGroup(
          rows =
            listOf(
              SettingsRow("Profile", displayName.ifBlank { "Local device" }, Icons.Default.Person, route = SettingsRoute.Profile),
              SettingsRow("Voice", if (speakerEnabled) "Speaker on" else "Speaker muted", Icons.Default.Mic, route = SettingsRoute.Voice),
              SettingsRow("Agents", if (agents.isEmpty()) "Load from gateway" else "${agents.size} available", Icons.Default.Person, status = agents.isNotEmpty(), route = SettingsRoute.Agents),
              SettingsRow("Approvals", approvalsSummary(pendingToolCalls.size), Icons.Default.Lock, status = approvalsStatus(pendingToolCalls.size), route = SettingsRoute.Approvals),
              SettingsRow("Cron Jobs", cronJobsSummary(cronStatus.jobs), Icons.Outlined.AccessTime, status = if (cronStatus.jobs > 0) cronStatus.enabled else null, route = SettingsRoute.CronJobs),
              SettingsRow("Usage", usageSummaryText(usageSummary.providers.size), Icons.Default.Storage, status = if (usageSummary.providers.isNotEmpty()) true else null, route = SettingsRoute.Usage),
              SettingsRow("Skills", skillsSummaryText(skillsSummary.skills), Icons.Default.Settings, status = skillsStatus(skillsSummary.skills), route = SettingsRoute.Skills),
              SettingsRow("Nodes & Devices", nodesDevicesSummaryText(nodesDevicesSummary), Icons.Default.Cloud, status = nodesDevicesStatus(nodesDevicesSummary), route = SettingsRoute.NodesDevices),
              SettingsRow("Channels", channelsSummaryText(channelsSummary), Icons.Default.Notifications, status = channelsStatus(channelsSummary), route = SettingsRoute.Channels),
              SettingsRow("Dreaming", dreamingSummaryText(dreamingSummary), Icons.Default.Storage, status = dreamingStatus(dreamingSummary), route = SettingsRoute.Dreaming),
              SettingsRow("Canvas", "Screen surface", Icons.AutoMirrored.Filled.ScreenShare, status = isConnected, route = SettingsRoute.Canvas),
              SettingsRow("Notifications", if (notificationForwardingEnabled) "Smart delivery" else "Off", Icons.Default.Notifications, route = SettingsRoute.Notifications),
              SettingsRow("Phone Capabilities", if (cameraEnabled) "Camera enabled" else "Locked", Icons.Default.Lock, status = !cameraEnabled, route = SettingsRoute.PhoneCapabilities),
              SettingsRow("Gateway", gatewaySummary(statusText, isConnected), Icons.Default.Cloud, status = isConnected, route = SettingsRoute.Gateway),
              SettingsRow("Appearance", "Dark", Icons.Default.Palette, route = SettingsRoute.Appearance),
              SettingsRow("Health", "Diagnostics", Icons.Default.Settings, status = isConnected, route = SettingsRoute.Health),
              SettingsRow("About", "Version and update", Icons.Default.Storage, route = SettingsRoute.About),
            ),
          onOpen = onRouteChange,
        )
      }

      item {
        SettingsGroup(
          rows = listOf(SettingsRow("Sign Out", "Disconnect", Icons.AutoMirrored.Filled.ExitToApp)),
          onOpen = { },
          onAction = { viewModel.disconnect() },
        )
      }

      item {
        Column(
          modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
          Text(text = "OpenClaw ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})", style = ClawTheme.type.caption.copy(fontSize = 12.5.sp, lineHeight = 16.sp), color = ClawTheme.colors.textMuted)
          Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = if (isConnected) "All systems operational" else "Gateway not connected",
              style = ClawTheme.type.caption.copy(fontSize = 12.5.sp, lineHeight = 16.sp),
              color = ClawTheme.colors.textSubtle,
            )
            Box(modifier = Modifier.size(4.5.dp).clip(CircleShape).background(if (isConnected) ClawTheme.colors.success else ClawTheme.colors.textSubtle))
          }
        }
      }
    }
  }
}

private fun approvalsSummary(count: Int): String =
  when (count) {
    0 -> "No pending approvals"
    1 -> "1 pending"
    else -> "$count pending"
  }

private fun approvalsStatus(count: Int): Boolean? = if (count > 0) true else null

private fun cronJobsSummary(count: Int): String =
  when (count) {
    0 -> "No scheduled jobs"
    1 -> "1 scheduled"
    else -> "$count scheduled"
  }

private fun usageSummaryText(count: Int): String =
  when (count) {
    0 -> "No provider usage"
    1 -> "1 provider"
    else -> "$count providers"
  }

private fun skillsSummaryText(skills: List<GatewaySkillSummary>): String {
  val ready = skills.count { !it.disabled && it.eligible && it.missingCount == 0 }
  return if (skills.isEmpty()) "No skills" else "$ready/${skills.size} ready"
}

private fun skillsStatus(skills: List<GatewaySkillSummary>): Boolean? =
  when {
    skills.isEmpty() -> null
    skills.any { it.blockedByAllowlist || (!it.disabled && (!it.eligible || it.missingCount > 0)) } -> false
    else -> true
  }

private fun nodesDevicesSummaryText(summary: GatewayNodesDevicesSummary): String {
  val online = summary.nodes.count { it.connected }
  val devices = summary.pairedDevices.size
  return when {
    summary.pendingDevices.isNotEmpty() -> "${summary.pendingDevices.size} pending"
    summary.nodes.isNotEmpty() -> "$online/${summary.nodes.size} online"
    devices > 0 -> "$devices paired"
    else -> "No devices"
  }
}

private fun nodesDevicesStatus(summary: GatewayNodesDevicesSummary): Boolean? =
  when {
    summary.pendingDevices.isNotEmpty() -> false
    summary.nodes.any { it.connected } -> true
    summary.pairedDevices.isNotEmpty() -> true
    else -> null
  }

private fun channelsSummaryText(summary: GatewayChannelsSummary): String {
  val connected = summary.channels.count { it.connected }
  return when {
    summary.channels.any { it.error != null } -> "${summary.channels.count { it.error != null }} issue"
    summary.channels.isNotEmpty() -> "$connected/${summary.channels.size} connected"
    else -> "No channels"
  }
}

private fun channelsStatus(summary: GatewayChannelsSummary): Boolean? =
  when {
    summary.channels.any { it.error != null } -> false
    summary.channels.any { it.connected || it.running } -> true
    summary.channels.any { it.configured || it.linked } -> true
    else -> null
  }

private fun dreamingSummaryText(summary: GatewayDreamingSummary): String =
  when {
    !summary.storeHealthy || !summary.phaseSignalHealthy -> "Needs attention"
    summary.enabled -> "${summary.shortTermCount} waiting"
    else -> "Off"
  }

private fun dreamingStatus(summary: GatewayDreamingSummary): Boolean? =
  when {
    !summary.storeHealthy || !summary.phaseSignalHealthy -> false
    summary.enabled -> true
    else -> null
  }

private data class SettingsRow(
  val title: String,
  val value: String,
  val icon: ImageVector,
  val status: Boolean? = null,
  val route: SettingsRoute? = null,
)

@Composable
private fun ProfilePanel(
  displayName: String,
  onClick: () -> Unit,
) {
  ClawPanel(contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)) {
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(ClawTheme.radii.row))
          .clickable(onClick = onClick),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Surface(
        modifier = Modifier.size(32.dp),
        shape = CircleShape,
        color = ClawTheme.colors.surfacePressed,
        border = BorderStroke(1.dp, ClawTheme.colors.borderStrong),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Text(
            text = displayName.firstOrNull()?.uppercase() ?: "O",
            style = ClawTheme.type.title.copy(fontSize = 14.sp, lineHeight = 17.sp),
            color = ClawTheme.colors.text,
            textAlign = TextAlign.Center,
          )
        }
      }
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = displayName, style = ClawTheme.type.section, color = ClawTheme.colors.text, maxLines = 1)
        Text(text = "OpenClaw mobile", style = ClawTheme.type.caption.copy(fontSize = 12.5.sp, lineHeight = 16.sp), color = ClawTheme.colors.textMuted, maxLines = 1)
      }
      Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = "Open profile",
        modifier = Modifier.size(15.dp),
        tint = ClawTheme.colors.text,
      )
    }
  }
}

@Composable
private fun SettingsGroup(
  rows: List<SettingsRow>,
  onOpen: (SettingsRoute) -> Unit,
  onAction: (() -> Unit)? = null,
) {
  ClawPanel(contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)) {
    Column {
      rows.forEachIndexed { index, row ->
        SettingsListRow(
          row = row,
          onClick = {
            val rowRoute = row.route
            if (rowRoute == null) {
              onAction?.invoke()
            } else {
              onOpen(rowRoute)
            }
          },
        )
        if (index != rows.lastIndex) {
          HorizontalDivider(color = ClawTheme.colors.border, thickness = 1.dp)
        }
      }
    }
  }
}

@Composable
private fun SettingsListRow(
  row: SettingsRow,
  onClick: () -> Unit,
) {
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .heightIn(min = 52.dp)
        .clip(RoundedCornerShape(ClawTheme.radii.row))
        .clickable(onClick = onClick)
        .padding(horizontal = 10.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Icon(imageVector = row.icon, contentDescription = null, modifier = Modifier.size(19.dp), tint = ClawTheme.colors.text)
    Text(text = row.title, style = ClawTheme.type.body, color = ClawTheme.colors.text, modifier = Modifier.weight(1f), maxLines = 1)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
      Text(text = row.value, style = ClawTheme.type.caption.copy(fontSize = 12.5.sp, lineHeight = 16.sp), color = ClawTheme.colors.textMuted, maxLines = 1)
      row.status?.let { active ->
        Box(modifier = Modifier.size(4.5.dp).clip(CircleShape).background(if (active) ClawTheme.colors.success else ClawTheme.colors.textSubtle))
      }
      if (row.route != null) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
          contentDescription = "Open ${row.title}",
          modifier = Modifier.size(17.dp),
          tint = ClawTheme.colors.text,
        )
      }
    }
  }
}

@Composable
private fun SettingsSearchButton(onClick: () -> Unit) {
  Surface(onClick = onClick, modifier = Modifier.size(ClawTheme.spacing.touchTarget), shape = CircleShape, color = Color.Transparent, contentColor = ClawTheme.colors.text) {
    Box(contentAlignment = Alignment.Center) {
      Icon(imageVector = Icons.Default.Search, contentDescription = "Search settings", modifier = Modifier.size(18.dp))
    }
  }
}

@Composable
private fun PlainIconButton(
  icon: ImageVector,
  contentDescription: String,
  onClick: () -> Unit,
) {
  Surface(onClick = onClick, modifier = Modifier.size(ClawTheme.spacing.touchTarget), shape = CircleShape, color = Color.Transparent, contentColor = ClawTheme.colors.text) {
    Box(contentAlignment = Alignment.Center) {
      Icon(imageVector = icon, contentDescription = contentDescription, modifier = Modifier.size(18.dp))
    }
  }
}

private fun relativeSessionTime(updatedAtMs: Long): String {
  val deltaMs = (System.currentTimeMillis() - updatedAtMs).coerceAtLeast(0L)
  val minutes = deltaMs / 60_000L
  if (minutes < 1) return "now"
  if (minutes < 60) return "${minutes}m"
  val hours = minutes / 60
  if (hours < 24) return "${hours}h"
  return "${hours / 24}d"
}

private fun displaySessionTitle(displayName: String?): String = displayName?.takeIf { it.isNotBlank() } ?: "Main session"

private fun statusDotColor(status: String): Color {
  val normalized = status.trim().lowercase()
  return when {
    normalized.contains("offline") || normalized.contains("not connected") -> Color(0xFFFF6B6B)
    normalized.contains("ready") || normalized.contains("active") || normalized.contains("online") -> Color(0xFF3EDB82)
    else -> Color(0xFF707070)
  }
}

private fun gatewaySummary(
  statusText: String,
  isConnected: Boolean,
): String {
  if (isConnected) return "Online and ready"
  val status = statusText.trim().lowercase()
  return when {
    status.contains("connecting") || status.contains("reconnecting") -> "Connecting..."
    status.contains("pairing") -> "Waiting for pairing"
    status.contains("auth") -> "Authentication needed"
    status.contains("certificate") || status.contains("tls") -> "Certificate review needed"
    else -> "Not connected"
  }
}
