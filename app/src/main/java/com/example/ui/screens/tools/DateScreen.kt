package com.example.ui.screens.tools

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ToolType
import com.example.ui.components.ResultDisplayCard
import com.example.ui.theme.AmberAccent
import com.example.ui.viewmodel.RandomlyViewModel
import com.example.util.HapticFeedbackUtil
import com.example.util.RandomGenerators
import com.example.util.ShareUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateScreen(
    viewModel: RandomlyViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by viewModel.settings.collectAsState()
    val isFavorite = viewModel.isToolFavorite(ToolType.DATE.id)

    val today = remember { LocalDate.now() }
    var startDate by remember { mutableStateOf(today.withDayOfYear(1)) }
    var endDate by remember { mutableStateOf(today.withDayOfYear(today.lengthOfYear())) }

    var selectedDateResult by remember { mutableStateOf<LocalDate?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isGenerating by remember { mutableStateOf(false) }
    val dateScale = remember { Animatable(1f) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy") }

    fun generate() {
        if (isGenerating) return
        errorMessage = null

        val testCheck = RandomGenerators.generateDate(startDate, endDate)
        if (testCheck.isFailure) {
            errorMessage = testCheck.exceptionOrNull()?.message ?: "Start date must be before end date"
            return
        }

        isGenerating = true
        HapticFeedbackUtil.performImpact(context, settings.hapticsEnabled)

        scope.launch {
            if (settings.animationsEnabled) {
                val cycleSteps = 8
                for (step in 0 until cycleSteps) {
                    RandomGenerators.generateDate(startDate, endDate).getOrNull()?.let { tempDate ->
                        selectedDateResult = tempDate
                    }
                    HapticFeedbackUtil.performImpact(context, settings.hapticsEnabled)
                    val delayMs = 35L + (step * 15L)
                    kotlinx.coroutines.delay(delayMs)
                }
            }

            val finalResult = RandomGenerators.generateDate(startDate, endDate)
            finalResult.fold(
                onSuccess = { date ->
                    selectedDateResult = date
                    if (settings.animationsEnabled) {
                        dateScale.snapTo(0.7f)
                        dateScale.animateTo(
                            1f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
                        )
                    }
                    HapticFeedbackUtil.performSuccess(context, settings.hapticsEnabled)
                    val formatted = date.format(dateFormatter)
                    viewModel.recordResult(
                        toolType = ToolType.DATE,
                        title = "Random Date",
                        result = formatted,
                        details = "Between $startDate and $endDate"
                    )
                },
                onFailure = { ex ->
                    errorMessage = ex.message ?: "Start date must be before end date"
                }
            )

            isGenerating = false
        }
    }

    LaunchedEffect(Unit) {
        if (selectedDateResult == null) {
            generate()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Random Date", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite(ToolType.DATE) }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = if (isFavorite) "Unfavorite" else "Favorite",
                            tint = if (isFavorite) AmberAccent else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            selectedDateResult?.let { date ->
                item {
                    val formatted = date.format(dateFormatter)
                    val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
                    Box(
                        modifier = Modifier.graphicsLayer {
                            scaleX = dateScale.value
                            scaleY = dateScale.value
                        }
                    ) {
                        ResultDisplayCard(
                            resultText = formatted,
                            detailsText = "Day of week: $dayOfWeek",
                            accentColor = ToolType.DATE.accentColor,
                            onCopy = {
                                ShareUtil.copyToClipboard(context, formatted)
                                viewModel.showMessage("Copied date to clipboard")
                            },
                            onShare = {
                                ShareUtil.shareText(context, "Random Date", formatted)
                            },
                            onRegenerate = { generate() }
                        )
                    }
                }
            }

            // Quick Preset Ranges
            item {
                Text("Quick Ranges", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val presets = listOf(
                        "This Year" to (today.withDayOfYear(1) to today.withDayOfYear(today.lengthOfYear())),
                        "Next 30 Days" to (today to today.plusDays(30)),
                        "Next 365 Days" to (today to today.plusDays(365)),
                        "Past 10 Years" to (today.minusYears(10) to today),
                        "21st Century" to (LocalDate.of(2000, 1, 1) to LocalDate.of(2099, 12, 31))
                    )
                    items(presets) { (title, range) ->
                        FilterChip(
                            selected = startDate == range.first && endDate == range.second,
                            onClick = {
                                startDate = range.first
                                endDate = range.second
                                generate()
                            },
                            label = { Text(title) }
                        )
                    }
                }
            }

            // Date Range selection card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Custom Date Range", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Start Date", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$startDate", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            }
                            Column {
                                Text("End Date", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$endDate", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("❌ $errorMessage", color = MaterialTheme.colorScheme.error)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { generate() },
                            enabled = !isGenerating,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("generate_date_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (isGenerating) "Flipping Calendar..." else "Generate Random Date",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
