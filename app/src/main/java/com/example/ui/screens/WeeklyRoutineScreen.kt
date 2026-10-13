package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClassRoutineItem
import com.example.data.model.RoutineDay
import com.example.ui.components.ClassRoutineCard
import com.example.ui.components.SectionSelectorPill
import com.example.viewmodel.RoutineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyRoutineScreen(
  viewModel: RoutineViewModel,
  modifier: Modifier = Modifier
) {
  val selectedSection by viewModel.selectedSection.collectAsState()
  val selectedDay by viewModel.selectedDay.collectAsState()
  val routine = viewModel.getRoutineForDay(selectedSection, selectedDay)

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Dashboard,
              contentDescription = null,
              modifier = Modifier.size(24.dp),
              tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = "Routine Dashboard",
              fontWeight = FontWeight.ExtraBold,
              style = MaterialTheme.typography.titleLarge
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    modifier = modifier
  ) { paddingValues ->
    BoxWithConstraints(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      val isWideScreen = maxWidth > 600.dp

      Column(modifier = Modifier.fillMaxSize()) {
        // Dashboard Controls Section
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          // Section Selector
          Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            SectionSelectorPill(
              selectedSection = selectedSection,
              onSectionSelected = { viewModel.setSection(it) }
            )
          }

          // Summary Statistics Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            val totalClasses = routine.count { !it.isFreePeriod }
            val freePeriods = routine.count { it.isFreePeriod }

            SummaryStatCard(
              label = "Classes",
              value = totalClasses.toString(),
              icon = Icons.Default.School,
              modifier = Modifier.weight(1f)
            )
            SummaryStatCard(
              label = "Free Periods",
              value = freePeriods.toString(),
              icon = Icons.Default.EventNote,
              modifier = Modifier.weight(1f)
            )
          }

          // Modern Day Selector (Scrollable Chips)
          LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
          ) {
            items(RoutineDay.entries) { day ->
              val isSelected = day == selectedDay
              val isToday = viewModel.isToday(day)

              FilterChip(
                selected = isSelected,
                onClick = { viewModel.setDay(day) },
                label = {
                  Text(
                    text = if (isToday) "Today (${day.shortName})" else day.fullName,
                    style = MaterialTheme.typography.labelLarge
                  )
                },
                leadingIcon = if (isSelected) {
                  { Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(16.dp)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = MaterialTheme.colorScheme.primary,
                  selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                  selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
              )
            }
          }
        }

        // Routine List Section
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
          contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          item {
            Text(
              text = "${selectedDay.fullName} Schedule",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(bottom = 4.dp)
            )
          }

          if (isWideScreen) {
            // Adaptive Grid-like view for wide screens
            val chunkedRoutine = routine.chunked(2)
            items(chunkedRoutine) { pair ->
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                pair.forEach { item ->
                  Box(modifier = Modifier.weight(1f)) {
                    ClassRoutineCard(
                      item = item,
                      isCurrent = false,
                      onAddTaskClick = { /* Handled in Today screen or via dialog */ }
                    )
                  }
                }
                if (pair.size == 1) {
                  Spacer(modifier = Modifier.weight(1f))
                }
              }
            }
          } else {
            // Standard list for normal screens
            items(routine) { item ->
              ClassRoutineCard(
                item = item,
                isCurrent = false,
                onAddTaskClick = { /* Handled in Today screen or via dialog */ }
              )
            }
          }

          if (routine.isEmpty()) {
            item {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Icon(
                    Icons.Default.Info,
                    null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.outline
                  )
                  Spacer(modifier = Modifier.height(12.dp))
                  Text(
                    "No classes scheduled for this day.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun SummaryStatCard(
  label: String,
  value: String,
  icon: ImageVector,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    )
  ) {
    Row(
      modifier = Modifier.padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        modifier = Modifier.size(40.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
          )
        }
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Text(
          text = value,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.ExtraBold
        )
        Text(
          text = label,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}
