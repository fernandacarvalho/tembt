package com.tembt.ui.tournament

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tembt.shared.generated.resources.Res
import com.tembt.shared.generated.resources.tournament_coming_soon
import org.jetbrains.compose.resources.stringResource

@Composable
fun TournamentScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(stringResource(Res.string.tournament_coming_soon))
    }
}
