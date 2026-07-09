package com.epicbuilder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.epicbuilder.data.model.Element
import com.epicbuilder.data.model.Hero
import com.epicbuilder.engine.Reason
import com.epicbuilder.ui.theme.color
import java.util.Locale

/** Avatar tròn hiển thị chữ cái đầu, viền màu theo hệ. */
@Composable
fun HeroAvatar(hero: Hero, size: Int = 44) {
    val initials = hero.name
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
    Box(
        modifier = Modifier
            .size(size.dp)
            .border(2.dp, hero.element.color(), CircleShape)
            .background(hero.element.color().copy(alpha = 0.18f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = hero.element.color()
        )
    }
}

@Composable
fun ElementBadge(element: Element) {
    Text(
        text = element.labelVi,
        style = MaterialTheme.typography.labelSmall,
        color = element.color(),
        modifier = Modifier
            .background(element.color().copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
fun ScoreBadge(score: Double) {
    val color = when {
        score >= 12.0 -> Color(0xFF2ECC71)
        score >= 9.5 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(
        text = String.format(Locale.US, "%.1f", score),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = color
    )
}

/** Dòng hero cơ bản trong danh sách chọn. */
@Composable
fun HeroRow(
    hero: Hero,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeroAvatar(hero)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = hero.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ElementBadge(hero.element)
                    Text(
                        text = "${hero.heroClass.labelVi} · ${hero.speedTier.labelVi}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            trailing?.invoke()
        }
    }
}

/** Card gợi ý: hero + điểm + lý do. */
@Composable
fun SuggestionCard(
    hero: Hero,
    score: Double,
    reasons: List<Reason>,
    onAdd: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                HeroAvatar(hero)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = hero.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ElementBadge(hero.element)
                        Text(
                            text = hero.heroClass.labelVi,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                ScoreBadge(score)
                IconButton(onClick = onAdd) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Thêm ${hero.name}",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (hero.noteVi.isNotBlank()) {
                Text(
                    text = hero.noteVi,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            // Hiển thị tối đa 4 lý do có ảnh hưởng lớn nhất (bỏ điểm nền ở vị trí 0)
            val detail = reasons.drop(1).sortedByDescending { kotlin.math.abs(it.delta) }.take(4)
            detail.forEach { reason ->
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val positive = reason.delta >= 0
                    Text(
                        text = if (positive) "+%.1f".format(Locale.US, reason.delta)
                        else "%.1f".format(Locale.US, reason.delta),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (positive) Color(0xFF2ECC71) else Color(0xFFE74C3C)
                    )
                    Text(
                        text = "  ${reason.textVi}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/** Ô tìm kiếm + bộ lọc theo hệ. */
@Composable
fun SearchAndFilterBar(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedElement: Element?,
    onElementChange: (Element?) -> Unit
) {
    Column {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Tìm hero…") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Xóa tìm kiếm")
                    }
                }
            },
            singleLine = true
        )
        LazyRow(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(Element.entries) { element ->
                FilterChip(
                    selected = selectedElement == element,
                    onClick = {
                        onElementChange(if (selectedElement == element) null else element)
                    },
                    label = { Text(element.labelVi) }
                )
            }
        }
    }
}

/** Hero khớp bộ lọc từ khóa + hệ? */
fun Hero.matchesFilter(query: String, element: Element?): Boolean =
    (element == null || this.element == element) &&
        (query.isBlank() || name.contains(query.trim(), ignoreCase = true))

/** Lọc danh sách hero theo từ khóa + hệ. */
fun List<Hero>.applyFilter(query: String, element: Element?): List<Hero> =
    filter { it.matchesFilter(query, element) }
