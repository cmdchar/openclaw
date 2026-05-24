package ai.openclaw.app.gateway

import ai.openclaw.app.SecurePrefs
import ai.openclaw.app.state.SessionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class RuntimeEvent {
  data class GatewayConnected(
    val serverName: String?,
    val remoteAddress: String?,
  ) : RuntimeEvent()

  data class GatewayDisconnected(
    val reason: String?,
  ) : RuntimeEvent()

  data class StreamStarted(
    val streamId: String,
  ) : RuntimeEvent()

  data class StreamStopped(
    val streamId: String,
  ) : RuntimeEvent()
}

/**
 * Single source of truth for Gateway Session lifecycle and auth management.
 */
class GatewaySessionManager(
  private val prefs: SecurePrefs,
) {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  private val _sessionState = MutableStateFlow(SessionState())
  val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

  private val _events = MutableSharedFlow<RuntimeEvent>(extraBufferCapacity = 64)
  val events: SharedFlow<RuntimeEvent> = _events.asSharedFlow()

  fun updateConnectionState(
    connected: Boolean,
    status: String,
    serverName: String?,
    remoteAddress: String?,
  ) {
    _sessionState.value =
      _sessionState.value.copy(
        isConnected = connected,
        statusText = status,
        serverName = serverName,
        remoteAddress = remoteAddress,
      )
  }

  fun dispatchEvent(event: RuntimeEvent) {
    _events.tryEmit(event)
  }
}
