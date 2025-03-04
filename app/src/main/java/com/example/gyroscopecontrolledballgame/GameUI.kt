package com.example.gyroscopecontrolledballgame
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    ballX: Float,
    ballY: Float,
    isGameWon: Boolean,
    walls: List<Rect>,
    goal: Rect?,
    onSizeChanged: (width: Float, height: Float) -> Unit,
    onReset: () -> Unit
) {
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gyroscope Ball Game") },
                actions = {
                    IconButton(onClick = onReset) {
                        Text("Reset")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            // Game canvas BY CLAUDE BECAUSE I DONT KNOW HOW TO DO IT
            Canvas(
                modifier = Modifier.fillMaxSize(),
                onDraw = {
                    if (canvasSize.width <= 0 || canvasSize.height <= 0) {
                        canvasSize = size
                        onSizeChanged(size.width, size.height)
                    }

                    // Draw walls
                    for (wall in walls) {
                        drawRect(
                            color = Color.Black,
                            topLeft = Offset(wall.left, wall.top),
                            size = Size(wall.width, wall.height)
                        )
                    }

                    // Draw goal area if initialized
                    if (goal != null) {
                        drawRect(
                            color = Color(0xFF00FF00), // 明亮的绿色
                            topLeft = Offset(goal.left, goal.top),
                            size = Size(goal.width, goal.height)
                        )
                    }

                    // Draw the ball
                    drawCircle(
                        color = Color.Blue,
                        center = Offset(ballX, ballY),
                        radius = 15f
                    )

                    // Draw game won message
                    if (isGameWon) {
                        drawRect(
                            color = Color.Red,
                            topLeft = Offset(size.width / 2 - 100f, size.height / 2 - 50f),
                            size = Size(200f, 100f),
                            alpha = 0.7f
                        )
                    }
                }
            )

            // display the winning
            if (isGameWon) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "You Win!",
                        style = TextStyle(
                            color = Color.Red,
                            fontSize = 30.sp,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}