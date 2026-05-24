package ai.openclaw.app.state

data class AppState(
  val isForeground: Boolean = false,
  val sleepPrevented: Boolean = false,
)

data class SessionState(
  val isConnected: Boolean = false,
  val statusText: String = "Offline",
  val serverName: String? = null,
  val remoteAddress: String? = null,
  val activeRole: String = "operator",
)

data class DeviceState(
  val nodesOnline: Int = 0,
  val nodesTotal: Int = 0,
  val pairedDevices: Int = 0,
  val cameraEnabled: Boolean = false,
  val micEnabled: Boolean = false,
)

data class StreamState(
  val canvasHydrated: Boolean = false,
  val canvasUrl: String? = null,
  val isListening: Boolean = false,
)

data class TaskState(
  val pendingRunCount: Int = 0,
  val activeAgents: Int = 0,
)

data class MemoryState(
  val isDreaming: Boolean = false,
  val dreamCount: Int = 0,
)
