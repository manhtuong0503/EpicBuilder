package com.epicbuilder.ui.offense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.epicbuilder.ui.components.HeroRow
import com.epicbuilder.ui.components.SearchAndFilterBar
import com.epicbuilder.ui.components.SuggestionCard
import com.epicbuilder.ui.components.TeamSlotsRow
import com.epicbuilder.ui.components.applyFilter
import com.epicbuilder.ui.components.matchesFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffenseScreen(
    onBack: () -> Unit,
    viewModel: OffenseViewModel = viewModel()
) {
    var query by rememberSaveable { mutableStateOf("") }
    var elementFilter by rememberSaveable { mutableStateOf<Element?>(null) }

    val mode = viewModel.mode
    val enemies = viewModel.enemies
    val allies = viewModel.allies
    val pickingEnemies = viewModel.pickingEnemies

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tìm Team Offense") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.reset()
                        query = ""
                        elementFilter = null
                    }) {
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
            Text(
                text = "Team địch cần đánh:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            TeamSlotsRow(
                team = enemies,
                teamSize = mode.teamSize,
                onRemove = { viewModel.removeEnemy(it) }
            )
            Spacer(Modifier.height(10.dp))

            if (pickingEnemies) {
                if (enemies.isNotEmpty()) {
                    Button(
                        onClick = {
                            viewModel.startPickingAllies()
                            query = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Xong — chọn attacker để counter")
                    }
                    Spacer(Modifier.height(8.dp))
                }
                SearchAndFilterBar(
                    query = query,
                    onQueryChange = { query = it },
                    selectedElement = elementFilter,
                    onElementChange = { elementFilter = it }
                )
                Spacer(Modifier.height(8.dp))
                val pickable = viewModel.allHeroes.applyFilter(query, elementFilter)
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(pickable, key = { it.id }) { hero ->
                        HeroRow(
                            hero = hero,
                            onClick = { viewModel.addEnemy(hero) },
                            trailing = {
                                IconButton(onClick = { viewModel.addEnemy(hero) }) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Thêm ${hero.name} vào team địch",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        )
                    }
                }
            } else {
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        text = "Team tấn công của bạn:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(onClick = {
                        viewModel.backToEnemies()
                        query = ""
                    }) {
                        Text("Sửa team địch")
                    }
                }
                Spacer(Modifier.height(6.dp))
                TeamSlotsRow(
                    team = allies,
                    teamSize = mode.teamSize,
                    onRemove = { viewModel.removeAlly(it) }
                )
                Spacer(Modifier.height(10.dp))

                if (allies.size >= mode.teamSize) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = "Team offense đã đủ ${mode.teamSize} hero. Chúc thắng trận!",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                } else {
                    Text(
                        text = if (allies.isEmpty())
                            "Gợi ý attacker tốt nhất để counter team địch:"
                        else
                            "Gợi ý hero tiếp theo (counter + synergy):",
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
                                    viewModel.addAlly(suggestion.hero)
                                    query = ""
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
