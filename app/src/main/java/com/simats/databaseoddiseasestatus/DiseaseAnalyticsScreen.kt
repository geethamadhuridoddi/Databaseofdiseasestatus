package com.simats.databaseoddiseasestatus

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.simats.databaseoddiseasestatus.ui.theme.DatabaseOdDiseaseStatusTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiseaseAnalyticsScreen(navController: NavController, viewModel: ReportViewModel = viewModel()) {
    val analytics by viewModel.analytics
    val isLoading by viewModel.isLoading

    LaunchedEffect(Unit) {
        viewModel.fetchDiseaseAnalytics()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Disease Analytics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF3F51B5),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status Distribution Chart
                AnalyticsCard(title = "Status Distribution") {
                    StatusPieChart(analytics?.statusDistribution ?: emptyMap())
                }

                // Severity Levels Chart
                AnalyticsCard(title = "Severity Levels") {
                    SeverityBarChart(analytics?.severityLevels ?: emptyMap())
                }
            }
        }
    }
}

@Composable
fun AnalyticsCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatusPieChart(statusData: Map<String, Int>) {
    val data = listOf(
        PieChartData("Active", statusData["active"]?.toFloat() ?: 0f, Color(0xFFFFB300)),
        PieChartData("Recovering", statusData["recovering"]?.toFloat() ?: 0f, Color(0xFFFB8C00)),
        PieChartData("Recovered", statusData["recovered"]?.toFloat() ?: 0f, Color(0xFF4CAF50)),
        PieChartData("Critical", statusData["critical"]?.toFloat() ?: 0f, Color(0xFFE53935))
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(180.dp)) {
                val total = data.sumOf { it.value.toDouble() }.toFloat()
                var startAngle = 0f
                data.forEach { slice ->
                    if (total > 0 && slice.value > 0) {
                        val sweepAngle = (slice.value / total) * 360f
                        drawArc(
                            color = slice.color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            size = Size(size.width, size.height),
                            style = Fill
                        )
                        startAngle += sweepAngle
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            data.forEach { slice ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                    Box(modifier = Modifier.size(12.dp).background(slice.color))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${slice.name}: ${slice.value.toInt()}", fontSize = 12.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SeverityBarChart(severityData: Map<String, Int>) {
    val labels = listOf("Low", "Medium", "High", "Critical")
    val values = listOf(
        severityData["low"]?.toFloat() ?: 0f,
        severityData["medium"]?.toFloat() ?: 0f,
        severityData["high"]?.toFloat() ?: 0f,
        severityData["critical"]?.toFloat() ?: 0f
    )
    val barColor = Color(0xFF4285F4)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val barWidth = width / (labels.size * 2f)
            val maxVal = values.maxOrNull()?.coerceAtLeast(1f) ?: 1f
            val labelPadding = 20.dp.toPx()
            val chartHeight = height - labelPadding * 2

            // Draw grid lines and Y-axis labels
            val yLines = 5
            for (i in 0 until yLines) {
                val y = chartHeight - (i * (chartHeight / (yLines - 1))) + labelPadding
                drawLine(
                    color = Color.LightGray,
                    start = Offset(40.dp.toPx(), y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
                drawContext.canvas.nativeCanvas.drawText(
                    (i * (maxVal / (yLines - 1))).toInt().toString(),
                    10.dp.toPx(),
                    y + 5.dp.toPx(),
                    android.graphics.Paint().apply {
                        textSize = 12.sp.toPx()
                        color = android.graphics.Color.GRAY
                    }
                )
            }

            // Draw bars and X-axis labels
            labels.forEachIndexed { index, label ->
                val x = (index * 2 + 1) * barWidth + 20.dp.toPx()
                val barHeight = (values[index] / maxVal) * chartHeight
                
                if (barHeight > 0) {
                    drawRect(
                        color = barColor,
                        topLeft = Offset(x - barWidth / 2, chartHeight - barHeight + labelPadding),
                        size = Size(barWidth, barHeight)
                    )
                }

                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    x - label.length * 3.dp.toPx(),
                    height - 5.dp.toPx(),
                    android.graphics.Paint().apply {
                        textSize = 12.sp.toPx()
                        color = android.graphics.Color.GRAY
                    }
                )
            }
        }
    }
}

data class PieChartData(val name: String, val value: Float, val color: Color)

@Preview(showBackground = true)
@Composable
fun DiseaseAnalyticsScreenPreview() {
    DatabaseOdDiseaseStatusTheme {
        DiseaseAnalyticsScreen(navController = rememberNavController())
    }
}
