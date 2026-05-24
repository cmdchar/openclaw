package ai.openclaw.app.ui

import ai.openclaw.app.MainViewModel
import ai.openclaw.app.ui.design.ClawScaffold
import ai.openclaw.app.ui.design.ClawTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TasksScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier,
) {
  ClawScaffold(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)) {
    Box(modifier = modifier.fillMaxSize()) {
      LazyColumn(contentPadding = PaddingValues(bottom = 82.dp)) {
        item {
          Text(text = "Tasks", style = ClawTheme.type.title, color = ClawTheme.colors.text)
        }
      }
    }
  }
}
