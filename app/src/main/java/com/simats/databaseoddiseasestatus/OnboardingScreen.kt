package com.simats.databaseoddiseasestatus

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(navController: NavController) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            TextButton(onClick = { navController.navigate("login") }) {
                Text(text = "Skip")
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            OnboardingPage(page = page)
        }
        Row(
            Modifier
                .height(50.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(onboardingPages.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(12.dp)

                )
            }
        }
        Button(
            onClick = {
                if (pagerState.currentPage == onboardingPages.size - 1) {
                    navController.navigate("login")
                } else {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (pagerState.currentPage == onboardingPages.size - 1) "Finish" else "Next")
        }
    }
}

private data class OnboardingPageData(
    val imageVector: ImageVector,
    val title: String,
    val description: String,
    val contentDescription: String,
)

private val onboardingPages = listOf(
    OnboardingPageData(
        imageVector = Icons.Filled.Groups,
        title = "Patient Management",
        description = "Efficiently manage patient records and track their health status in real-time",
        contentDescription = "Patient Management Icon"
    ),
    OnboardingPageData(
        imageVector = Icons.Filled.MonitorHeart,
        title = "Disease Tracking",
        description = "Monitor disease progression and recovery with detailed history and analytics",
        contentDescription = "Disease Tracking Icon"
    ),
    OnboardingPageData(
        imageVector = Icons.Filled.BarChart,
        title = "Comprehensive Reports",
        description = "Generate detailed reports and analytics to make informed medical decisions",
        contentDescription = "Comprehensive Reports Icon"
    ),
    OnboardingPageData(
        imageVector = Icons.Filled.VerifiedUser,
        title = "Data Security",
        description = "Rest assured that your data is safe and secure with our advanced security features",
        contentDescription = "Data Security Icon"
    )
)

@Composable
fun OnboardingPage(page: Int) {
    val pageData = onboardingPages[page]
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(Color(0xFFE3F2FD)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = pageData.imageVector,
                contentDescription = pageData.contentDescription,
                tint = Color(0xFF2196F3),
                modifier = Modifier.size(100.dp)
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = pageData.title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = pageData.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    OnboardingScreen(navController = rememberNavController())
}

@Preview(showBackground = true, name = "Disease Tracking Page")
@Composable
fun OnboardingPagePreview() {
    OnboardingPage(page = 1)
}
