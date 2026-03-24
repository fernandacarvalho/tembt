package com.tembt.ui.schedule

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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tembt.shared.generated.resources.Res
import com.tembt.shared.generated.resources.schedule_bg
import org.jetbrains.compose.resources.painterResource
import com.tembt.domain.model.ScheduleWindow
import com.tembt.domain.model.SlotPlayer
import com.tembt.domain.model.WindowSlot
import com.tembt.presentation.schedule.ScheduleUiState
import com.tembt.presentation.schedule.ScheduleViewModel
import com.tembt.ui.theme.AlabasterGrey
import com.tembt.ui.theme.Amaranth
import com.tembt.ui.theme.DeepMocha
import com.tembt.ui.theme.PitchBlack
import com.tembt.ui.theme.TembtWhite
import com.tembt.ui.theme.condensedBlackFontFamily
import com.tembt.ui.theme.condensedBoldFontFamily
import com.tembt.ui.theme.condensedFontFamily
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel

private val BG_LIGHT        = AlabasterGrey
private val CARD_WHITE      = TembtWhite
private val BRAND_RED       = Amaranth
private val TEXT_PRIMARY    = PitchBlack
private val TEXT_SECONDARY  = DeepMocha
private val CHECKED_GREEN   = Color(0xFF27AE60)
// Avatar circles alternate appPrimary → appSecondary → appPrimary …
private val AVATAR_COLORS = listOf(
    Amaranth,              // appPrimary  #CE4257
    Color(0xFFDB5316),     // appSecondary #DB5316 (SpicyPaprika)
)

@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(BG_LIGHT)) {
        // SVG painters on iOS ignore ContentScale and stretch to fill bounds.
        // We manually compute the proportional size (equivalent to ContentScale.Crop)
        // so that FillBounds draws the correct aspect ratio.
        val svgW = 375f
        val svgH = 667f
        val scale = maxOf(maxWidth.value / svgW, maxHeight.value / svgH)
        val imgW = (svgW * scale).dp
        val imgH = (svgH * scale).dp

        Image(
            painter = painterResource(Res.drawable.schedule_bg),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .size(imgW, imgH)
                .align(Alignment.TopCenter)
        )

        when (val state = uiState) {
            is ScheduleUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = BRAND_RED
                )
            }

            is ScheduleUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Erro ao carregar", color = TEXT_PRIMARY, fontSize = 18.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(state.message, color = TEXT_SECONDARY, fontSize = 14.sp)
                }
            }

            is ScheduleUiState.Ready -> {
                val onCheckin = remember(viewModel) { viewModel::checkin }
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
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            PollHeader(
                window = window,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 12.dp)
            )
        }

        items(window.slots, key = { it.time }) { slot ->
            val isCheckedIn by remember(slot.time, checkedInSlotTime) {
                derivedStateOf { checkedInSlotTime == slot.time }
            }
            val onTap = remember(slot.time, onCheckin) { { onCheckin(slot.time) } }
            PollSlotItem(slot = slot, isCheckedIn = isCheckedIn, onTap = onTap)
        }
    }
}

@Composable
private fun PollHeader(window: ScheduleWindow, modifier: Modifier = Modifier) {
    val formattedDate = remember(window.date) { formatPollDate(window.date) }
    // Each Text gets its own single-font FontFamily so CMP resolves the correct
    // file without ambiguity (multi-weight FontFamily resolution is unreliable on iOS).
    val boldFont  = condensedBoldFontFamily()
    val blackFont = condensedBlackFontFamily()

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "Bora pro ",
                    fontFamily = boldFont,
                    color = BRAND_RED,
                    fontSize = 32.sp,
                    lineHeight = 38.sp,
                    modifier = Modifier.alignByBaseline()
                )
                Text(
                    text = "play",
                    fontFamily = blackFont,
                    color = BRAND_RED,
                    fontSize = 33.sp,
                    lineHeight = 38.sp,
                    modifier = Modifier.alignByBaseline()
                )
                 Text(
                    text = "?",
                    fontFamily = boldFont,
                    color = BRAND_RED,
                    fontSize = 32.sp,
                    lineHeight = 38.sp,
                    modifier = Modifier.alignByBaseline()
                )
            }

            IconButton(
                onClick = {},
                modifier = Modifier
                    .size(44.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(BRAND_RED)
            ) {
                Icon(
                    imageVector = Icons.Outlined.IosShare,
                    contentDescription = "Compartilhar",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = buildAnnotatedString {
                append("Lista de presença ")
                withStyle(SpanStyle(fontWeight = FontWeight.Medium)) { append(formattedDate) }
                append("\nMarque o horário que pretende chegar\nna rede.")
            },
            color = TEXT_PRIMARY,
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 22.sp
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
            .clip(RoundedCornerShape(14.dp))
            .background(CARD_WHITE)
            .clickable(onClick = onTap)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        CheckCircle(isChecked = isCheckedIn)
        Spacer(Modifier.width(14.dp))
        Text(
            text = slot.time,
            color = TEXT_PRIMARY,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        PlayerAvatarsWithCount(players = slot.players)
    }
}

@Composable
private fun CheckCircle(isChecked: Boolean) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(if (isChecked) CHECKED_GREEN else Color.Transparent)
            .border(
                width = 2.dp,
                color = if (isChecked) CHECKED_GREEN else Color(0xFFCCCCCC),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isChecked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun PlayerAvatarsWithCount(players: List<SlotPlayer>) {
    val visible = remember(players) {
        players.take(minOf(3, players.size)).mapIndexed { index, player ->
            player to AVATAR_COLORS[index % AVATAR_COLORS.size]
        }
    }
    val avatarSize = 30
    val overlap = 8
    val step = avatarSize - overlap
    val clusterWidth = if (visible.isEmpty()) 0 else avatarSize + step * (visible.size - 1)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(width = clusterWidth.dp, height = avatarSize.dp)) {
            visible.forEachIndexed { index, (player, color) ->
                val initials = playerInitials(player.name)
                Box(
                    modifier = Modifier
                        .offset(x = (step * index).dp)
                        .size(avatarSize.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(2.dp, CARD_WHITE, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        color = Color.White,
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 13.sp,
                            lineHeightStyle = LineHeightStyle(
                                alignment = LineHeightStyle.Alignment.Center,
                                trim = LineHeightStyle.Trim.Both
                            )
                        )
                    )
                }
            }
        }
        Spacer(Modifier.width(6.dp))
        Text(
            text = players.size.toString(),
            color = TEXT_SECONDARY,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun playerInitials(name: String): String {
    val first = name.trim().firstOrNull()?.uppercaseChar()
    return first?.toString() ?: "?"
}

private fun formatPollDate(date: String): String {
    return try {
        val local = LocalDate.parse(date)
        val day = local.dayOfMonth
        val month = local.monthNumber
        val dayName = when (local.dayOfWeek) {
            DayOfWeek.MONDAY    -> "Segunda"
            DayOfWeek.TUESDAY   -> "Terça"
            DayOfWeek.WEDNESDAY -> "Quarta"
            DayOfWeek.THURSDAY  -> "Quinta"
            DayOfWeek.FRIDAY    -> "Sexta"
            DayOfWeek.SATURDAY  -> "Sábado"
            DayOfWeek.SUNDAY    -> "Domingo"
        }
        "$dayName ${day.toString().padStart(2, '0')}/${month.toString().padStart(2, '0')}"
    } catch (e: Exception) {
        // Return original date string if parsing fails
        date
    }
}
