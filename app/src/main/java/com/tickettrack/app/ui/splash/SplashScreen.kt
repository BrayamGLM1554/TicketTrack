package com.tickettrack.app.ui.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tickettrack.app.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    viewModel: SplashViewModel = viewModel()
) {
    val state = viewModel.state.collectAsState()

    // Animation for the truck driving in from left to center
    val translationX by animateFloatAsState(
        targetValue = if (state.value.isVisible) 0f else -1000f,
        animationSpec = tween(
            durationMillis = 1200,
            easing = FastOutSlowInEasing
        ),
        label = "translationX"
    )

    // Fade in animation
    val alpha by animateFloatAsState(
        targetValue = if (state.value.isVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1200,
            easing = FastOutSlowInEasing
        ),
        label = "alpha"
    )

    // Slight bounce effect when stopping
    val scale by animateFloatAsState(
        targetValue = if (state.value.isVisible) 1f else 0.9f,
        animationSpec = tween(
            durationMillis = 1200,
            easing = FastOutSlowInEasing
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .graphicsLayer {
                    this.translationX = translationX
                    this.alpha = alpha
                    this.scaleX = scale
                    this.scaleY = scale
                }
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.logo_tickettrack),
                contentDescription = "TicketTrack Logo",
                modifier = Modifier
                    .size(120.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // App Name with gradient shine effect
            Text(
                text = "TICKETTRACK",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp,
                    letterSpacing = 4.sp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF5AC5C5),
                            Color(0xFF9FFFFF),
                            Color(0xFF7DD4D4),
                            Color(0xFF5AC5C5)
                        )
                    )
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tagline with subtle gradient
            Text(
                text = "DEL TICKET AL BALANCE EN SEGUNDOS",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Medium,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF5AC5C5),
                            Color(0xFF8DE5E5),
                            Color(0xFF5AC5C5)
                        )
                    )
                )
            )
        }
    }

    LaunchedEffect(true) {
        viewModel.startAnimation()
        delay(2800)
        onSplashFinished()
    }
}