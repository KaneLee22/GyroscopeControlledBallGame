package com.example.gyroscopecontrolledballgame

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import kotlin.math.abs

class MainActivity : ComponentActivity(), SensorEventListener {

    // Sensor manager and gyroscope sensor
    private lateinit var sensorManager: SensorManager
    private var gyroscopeSensor: Sensor? = null

    // Game state
    private var ballX by mutableStateOf(0f)
    private var ballY by mutableStateOf(0f)
    private var isGameWon by mutableStateOf(false)
    private var walls by mutableStateOf<List<Rect>>(emptyList())
    private var goal by mutableStateOf<Rect?>(null)

    // Screen dimensions
    private var screenWidth = 0f
    private var screenHeight = 0f

    // Movement sensitivity - adjust these values to change ball speed
    private val sensitivityX = 5f
    private val sensitivityY = 5f

    // Ball properties
    private val ballRadius = 15f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize sensor manager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Get gyroscope sensor
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        // Check if device has a gyroscope
        if (gyroscopeSensor == null) {
            Toast.makeText(this, "This device does not have a gyroscope sensor", Toast.LENGTH_LONG).show()
            finish() // Close app if gyroscope is not available
            return
        }

        setContent {
            GyroscopeBallGameTheme {
                GameScreen(
                    ballX = ballX,
                    ballY = ballY,
                    isGameWon = isGameWon,
                    walls = walls,
                    goal = goal,
                    onSizeChanged = { width, height ->
                        screenWidth = width
                        screenHeight = height
                        initializeGame(width, height)
                    },
                    onReset = { resetGame() }
                )
            }
        }
        showInstructions()
    }

    /**
     * Initialize game elements
     */
    private fun initializeGame(width: Float, height: Float) {
        createWalls(width, height)
    }

    /**
     * Create wall obstacles for the maze
     */
    private fun createWalls(width: Float, height: Float) {
        val wallsList = mutableListOf<Rect>()
        val wallThickness = 10f
        val mazeSize = minOf(width, height) * 0.8f
        val startX = (width - mazeSize) / 2
        val startY = (height - mazeSize) / 2

        wallsList.add(Rect(startX, startY, startX + mazeSize, startY + wallThickness))
        wallsList.add(Rect(startX, startY + mazeSize - wallThickness, startX + mazeSize, startY + mazeSize))
        wallsList.add(Rect(startX, startY, startX + wallThickness, startY + mazeSize))
        wallsList.add(Rect(startX + mazeSize - wallThickness, startY, startX + mazeSize, startY + mazeSize))

        val horizontalWalls = listOf(
            Rect(startX, startY + mazeSize * 0.25f, startX + mazeSize * 0.5f, startY + mazeSize * 0.25f + wallThickness),

            Rect(startX + mazeSize * 0.5f + mazeSize * 0.1f, startY + mazeSize * 0.25f, startX + mazeSize, startY + mazeSize * 0.25f + wallThickness),

            Rect(startX + mazeSize * 0.5f, startY + mazeSize * 0.5f, startX + mazeSize * 0.9f, startY + mazeSize * 0.5f + wallThickness),

            Rect(startX, startY + mazeSize * 0.75f, startX + mazeSize * 0.8f, startY + mazeSize * 0.75f + wallThickness)
        )

        val verticalWalls = listOf(
            Rect(startX + mazeSize * 0.5f, startY, startX + mazeSize * 0.5f + wallThickness, startY + mazeSize * 0.15f),

            Rect(startX + mazeSize * 0.75f, startY + mazeSize * 0.25f, startX + mazeSize * 0.75f + wallThickness, startY + mazeSize * 0.5f),

            Rect(startX + mazeSize * 0.33f, startY + mazeSize * 0.5f, startX + mazeSize * 0.33f + wallThickness, startY + mazeSize * 0.75f),

            Rect(startX + mazeSize * 0.8f, startY + mazeSize * 0.75f, startX + mazeSize * 0.8f + wallThickness, startY + mazeSize)
        )

        wallsList.addAll(horizontalWalls)
        wallsList.addAll(verticalWalls)

        goal = Rect(
            startX + mazeSize - 30f,
            startY + mazeSize - 30f,
            startX + mazeSize - 10f,
            startY + mazeSize - 10f
        )

        ballX = startX + mazeSize / 2
        ballY = startY + mazeSize / 2

        walls = wallsList
    }

    /**
     * Show game instructions
     */
    private fun showInstructions() {
        Toast.makeText(
            this,
            "Tilt your phone to move the ball to the green area!",
            Toast.LENGTH_LONG
        ).show()
    }

    /**
     * Register sensor listener when activity starts
     */
    override fun onResume() {
        super.onResume()
        // Register sensor listener with game-appropriate update rate
        sensorManager.registerListener(
            this,
            gyroscopeSensor,
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    /**
     * Unregister sensor listener when activity pauses
     */
    override fun onPause() {
        super.onPause()
        // Unregister to save battery
        sensorManager.unregisterListener(this)
    }

    /**
     * Handle sensor data changes
     */
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            // Get rotation rates around each axis
            val rotationX = event.values[0] // Around X-axis (roll)
            val rotationY = event.values[1] // Around Y-axis (pitch)

            // Convert rotation to movement
            // Note: we invert Y because phone coordinate system is different
            // from screen coordinate system
            val movementX = rotationY * sensitivityX
            val movementY = rotationX * sensitivityY

            // Update ball position
            updateBallPosition(movementX, movementY)
        }
    }

    /**
     * Update ball position based on gyroscope data
     */
    private fun updateBallPosition(xAcceleration: Float, yAcceleration: Float) {
        // If game is won, don't update
        if (isGameWon) return

        // Calculate new position
        ballX += xAcceleration
        ballY += yAcceleration

        // Check wall collisions and adjust position
        checkWallCollisions()

        // Check if ball reached the goal
        checkGoal()
    }

    /**
     * Check for collisions with walls and prevent ball from passing through
     */
    private fun checkWallCollisions() {
        for (wall in walls) {
            // Check if ball intersects with any wall
            if (ballX + ballRadius > wall.left &&
                ballX - ballRadius < wall.right &&
                ballY + ballRadius > wall.top &&
                ballY - ballRadius < wall.bottom) {
                val leftOverlap = abs(ballX + ballRadius - wall.left)
                val rightOverlap = abs(wall.right - (ballX - ballRadius))
                val topOverlap = abs(ballY + ballRadius - wall.top)
                val bottomOverlap = abs(wall.bottom - (ballY - ballRadius))

                when (minOf(leftOverlap, rightOverlap, topOverlap, bottomOverlap)) {
                    leftOverlap -> ballX = wall.left - ballRadius
                    rightOverlap -> ballX = wall.right + ballRadius
                    topOverlap -> ballY = wall.top - ballRadius
                    bottomOverlap -> ballY = wall.bottom + ballRadius
                }
            }
        }

        ballX = ballX.coerceIn(ballRadius, screenWidth - ballRadius)
        ballY = ballY.coerceIn(ballRadius, screenHeight - ballRadius)
    }

    /**
     * Check if ball has reached the goal
     */
    private fun checkGoal() {
        goal?.let { goalRect ->
            if (ballX > goalRect.left && ballX < goalRect.right &&
                ballY > goalRect.top && ballY < goalRect.bottom) {
                isGameWon = true
            }
        }
    }

    /**
     * Reset the game
     */
    private fun resetGame() {
        val mazeSize = minOf(screenWidth, screenHeight) * 0.8f
        val startX = (screenWidth - mazeSize) / 2
        val startY = (screenHeight - mazeSize) / 2

        ballX = startX + mazeSize / 2
        ballY = startY + mazeSize / 2

        isGameWon = false
    }
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
    }
}