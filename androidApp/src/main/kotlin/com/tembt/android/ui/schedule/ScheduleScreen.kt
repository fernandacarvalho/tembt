package com.tembt.android.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tembt.domain.model.ScheduleWindow
import com.tembt.domain.model.SlotPlayer
import com.tembt.domain.model.WindowSlot
import com.tembt.presentation.schedule.ScheduleUiState
import com.tembt.android.ui.theme.AlabasterGrey
import com.tembt.android.ui.theme.ApricotCream
import com.tembt.android.ui.theme.DuskBlue
import com.tembt.android.ui.theme.PumpkinSpice
import com.tembt.android.ui.theme.TembtBlack
import com.tembt.android.ui.theme.TembtWhite
import com.tembt.presentation.schedule.ScheduleViewModel
import org.koin.androidx.compose.koinViewModel

private val BgLight       = AlabasterGrey
private val CardWhite     = TembtWhite
private val OrangeAccent  = PumpkinSpice
private val TextPrimary   = TembtBlack
private val TextSecondary = DuskBlue
private val AvatarColors = listOf(
    Color(0xFF8B5E3C),
    Color(0xFF3A7BD5),
    Color(0xFF9B59B6),
    Color(0xFFE74C3C),
    Color(0xFF27AE60),
)

@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
    ) {
        when (val state = uiState) {
            is ScheduleUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = OrangeAccent
                )
            }

            is ScheduleUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Erro ao carregar", color = TextPrimary, fontSize = 18.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(state.message, color = TextSecondary, fontSize = 14.sp)
                }
            }

            is ScheduleUiState.Ready -> {
                val onCheckin = remember(viewModel) { { time: String -> viewModel.checkin(time) } }
                PollContent(
                    window = state.window,
                    checkedInSlotTime = state.checkedInSlotTime,
                    onCheckin = onCheckin
                )
            }
        }
    }
}

@Composable
private fun PollContent(
    window: ScheduleWindow,
    checkedInSlotTime: String?,
    onCheckin: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            PollHeader(window = window)
            Spacer(Modifier.height(4.dp))
        }
        items(window.slots, key = { it.time }) { slot ->
            val isCheckedIn by remember(slot.time, checkedInSlotTime) {
                derivedStateOf { checkedInSlotTime == slot.time }
            }
            val onTap = remember(slot.time, onCheckin) { { onCheckin(slot.time) } }
            PollSlotItem(
                slot = slot,
                isCheckedIn = isCheckedIn,
                onTap = onTap
            )
        }
    }
}

@Composable
private fun PollHeader(window: ScheduleWindow) {
    val formattedDate = remember(window.date) { formatPollDate(window.date) }
    Text(
        text = buildAnnotatedString {
            append("Bora para o play ")
            withStyle(SpanStyle(color = OrangeAccent)) { append(formattedDate) }
        },
        color = TextPrimary,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 28.sp
    )
    Spacer(Modifier.height(6.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(16.dp)
        )
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier
                .size(16.dp)
                .offset(x = (-5).dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = "Selecione uma ou mais opções",
            color = TextSecondary,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun PollSlotItem(
    slot: WindowSlot,
    isCheckedIn: Boolean,
    onTap: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardWhite)
            .clickable(onClick = onTap)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        CheckCircle(isChecked = isCheckedIn)

        Spacer(Modifier.width(14.dp))

        Text(
            text = slot.time,
            color = TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )

        if (slot.players.isNotEmpty()) {
            PlayerAvatarsWithCount(players = slot.players)
        }
    }
}

@Composable
private fun CheckCircle(isChecked: Boolean) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(if (isChecked) OrangeAccent else Color.Transparent)
            .border(
                width = 2.dp,
                color = if (isChecked) OrangeAccent else Color(0xFFCCCCCC),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isChecked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Confirmado",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun PlayerAvatarsWithCount(players: List<SlotPlayer>) {
    val visibleWithColors = remember(players) {
        players.takeLast(minOf(2, players.size)).map { player ->
            player to AvatarColors[player.name.hashCode().and(0x7FFFFFFF) % AvatarColors.size]
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        visibleWithColors.forEachIndexed { index, (player, color) ->
            Box(
                modifier = Modifier
                    .offset(x = (-6 * index).dp)
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(1.5.dp, CardWhite, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = player.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.width(6.dp))
        Text(
            text = players.size.toString(),
            color = TextSecondary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/** Formats ISO date "2026-03-14" → "14/3" */
private fun formatPollDate(date: String): String {
    return try {
        val parts = date.split("-")
        if (parts.size != 3) return date
        val month = parts[1].trimStart('0')
        val day = parts[2].trimStart('0')
        "$day/$month"
    } catch (e: Exception) {
        e.printStackTrace()
        date
    }
}
