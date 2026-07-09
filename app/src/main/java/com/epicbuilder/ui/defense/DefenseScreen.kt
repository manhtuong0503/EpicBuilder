package com.epicbuilder.ui.defense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.epicbuilder.data.model.Element
import com.epicbuilder.engine.BattleMode
import com.epicbuilder.ui.components.SearchAndFilterBar
import com.epicbuilder.ui.components.SuggestionCard
import com.epicbuilder.ui.components.TeamSlotsRow
import com.epicbuilder.ui.components.matchesFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefenseScreen(
    onBack: () -> Unit,
    viewModel: DefenseViewModel = viewModel()
) {
    var query by rememberSaveable { mutableStateOf("") }
    var elementFilter by rememberSaveable { mutableStateOf<Element?>(null) }

    val team = viewModel.team
    val mode = viewModel.mode
    val teamFull = team.size >= mode.teamSize

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Xây Team Defense") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.reset() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Làm lại")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BattleMode.entries.forEach { m ->
                    FilterChip(
                        selected = mode == m,
                        onClick = { viewModel.changeMode(m) },
                        label = { Text("${m.labelVi} (${m.teamSize})") }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            TeamSlotsRow(
                team = team,
                teamSize = mode.teamSize,
                onRemove = { viewModel.removeHero(it) }
            )
            Spacer(Modifier.height(12.dp))

            if (teamFull) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "Team defense đã đủ ${mode.teamSize} hero. " +
                            "Bấm X trên một hero để thay đổi, hoặc nút làm lại để xây team khác.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {
                Text(
                    text = if (team.isEmpty())
                        "Chọn hero trụ cột đầu tiên (xếp theo độ mạnh defense trong meta):"
                    else
                        "Gợi ý hero phù hợp tiếp theo cho team defense:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                SearchAndFilterBar(
                    query = query,
                    onQueryChange = { query = it },
                    selectedElement = elementFilter,
                    onElementChange = { elementFilter = it }
                )
                Spacer(Modifier.height(8.dp))

                val visible = viewModel.suggestions.filter {
                    it.hero.matchesFilter(query, elementFilter)
                }
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(visible, key = { it.hero.id }) { suggestion ->
                        SuggestionCard(
                            hero = suggestion.hero,
                            score = suggestion.score,
                            reasons = suggestion.reasons,
                            onAdd = {
                                viewModel.addHero(suggestion.hero)
                                query = ""
                            }
                        )
                    }
                }
            }
        }
    }
}
