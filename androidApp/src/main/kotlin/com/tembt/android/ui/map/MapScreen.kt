package com.tembt.android.ui.map

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tembt.ui.permission.PermissionMode
import com.tembt.ui.permission.PermissionScreen
import com.tembt.presentation.map.MapUiEvent
import com.tembt.presentation.map.MapUiState
import com.tembt.presentation.map.MapViewModel
import com.tembt.ui.theme.AlabasterGrey
import com.tembt.ui.theme.DeepMocha
import com.tembt.ui.theme.PitchBlack
import com.tembt.ui.theme.SpicyPaprika
import org.koin.androidx.compose.koinViewModel

@Composable
fun MapScreen(viewModel: MapViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    // Re-check permission on every app resume (e.g., user returns from system Settings).
    // Stop polling on pause to avoid background network calls.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.onResume()
                Lifecycle.Event.ON_PAUSE  -> viewModel.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Handle one-shot events emitted by the ViewModel
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                MapUiEvent.OpenAppSettings -> {
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null)
                    )
                    context.startActivity(intent)
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.onPermissionResult()
    }

    when (val state = uiState) {
        is MapUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SpicyPaprika)
            }
        }

        is MapUiState.PermissionRequired -> {
            PermissionScreen(
                mode = PermissionMode.ForegroundRequired,
                onRequestPermission = {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            )
        }

        is MapUiState.BackgroundPermissionRequired -> {
            PermissionScreen(
                mode = PermissionMode.BackgroundRequired,
                onRequestPermission = { viewModel.onOpenSettingsRequested() }
            )
        }

        is MapUiState.MapReady -> {
            var recenterTrigger by remember { mutableIntStateOf(0) }
            Box(Modifier.fillMaxSize()) {
                MapLibreView(center = state.center, players = state.players, recenterTrigger = recenterTrigger)

                // ── Info card ─────────────────────────────────────────────
                Card(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 16.dp, end = 16.dp, top = 56.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(50),
                    colors = CardDefaults.cardColors(containerColor = AlabasterGrey),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(start = 14.dp, top = 10.dp, bottom = 10.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            if (state.courtName.isNotEmpty()) {
                                Text(
                                    text = state.courtName,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = PitchBlack
                                )
                            }
                            Text(
                                text = "${state.players.size} jogadores no local agora",
                                style = MaterialTheme.typography.bodyMedium,
                                color = PitchBlack
                            )
                            if (state.lastUpdatedAt != null) {
                                Text(
                                    text = "Última atualização ${state.lastUpdatedAt}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PitchBlack.copy(alpha = 0.6f)
                                )
                            }
                        }
                        IconButton(onClick = { viewModel.refreshPlayers() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = SpicyPaprika
                            )
                        }
                    }
                }

                // ── Re-center button ──────────────────────────────────────
                FloatingActionButton(
                    onClick = { recenterTrigger++ },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 120.dp),
                    shape = CircleShape,
                    containerColor = AlabasterGrey,
                    contentColor = DeepMocha
                ) {
                    Icon(
                        imageVector = Icons.Default.NearMe,
                        contentDescription = null
                    )
                }
            }
        }

        is MapUiState.Error -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Erro ao carregar", style = MaterialTheme.typography.titleMedium)
                Text(
                    state.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(onClick = { viewModel.checkPermission() }) { Text("Tentar novamente") }
            }
        }
    }
}
