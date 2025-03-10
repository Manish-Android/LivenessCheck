package com.manish.demoofimagecompereason

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.manish.demoofimagecompereason.databinding.ActivityMainBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var binding: ActivityMainBinding
    private var overallTimer: CountDownTimer? = null
    private var isNavigationStarted = false
    private lateinit var shuffledSteps: List<Pair<String, Int>>
    private var currentStepIndex = 0
    private val livenessActions = listOf(
        "Smile 😊" to R.drawable.smiling_face,
        "Turn Head Right 👉" to R.drawable.left_head_turn_face,
        "Turn Head Left 👈" to R.drawable.right_head_turn_face,
        "Blink Both Eyes 👀" to R.drawable.blinking_face,
        "Close Left Eye 👁" to R.drawable.right_eye_close_face,
        "Close Right Eye 👁" to R.drawable.left_eye_close_face
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // ✅ Shuffle steps and images
        shuffledSteps = livenessActions.shuffled()

        // ✅ Show first step
        updateInstruction()

        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 1)

        cameraExecutor = Executors.newSingleThreadExecutor()
        startCamera()

        startOverallTimer()

    }

    private fun startOverallTimer() {
        overallTimer?.cancel() // Cancel any existing timer

        overallTimer = object : CountDownTimer(20000, 1000) { // 20 sec total
            override fun onTick(millisUntilFinished: Long) {
                binding.tvTimer.text = "Time left: ${millisUntilFinished / 1000}s" // Show countdown
            }

            override fun onFinish() {
                binding.tvInstrucation.text = "Time Expired! Restarting..."
                restartLivenessCheck()
            }
        }.start()
    }



    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.preview.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImage(imageProxy)
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
            } catch (exc: Exception) {
                Log.e("CameraX", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }


    @OptIn(ExperimentalGetImage::class)
/*
    private fun processImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()

        val detector = FaceDetection.getClient(options)

        detector.process(image)
            .addOnSuccessListener { faces ->
                for (face in faces) {
                    val leftEyeOpen = face.leftEyeOpenProbability ?: 0.0f
                    val rightEyeOpen = face.rightEyeOpenProbability ?: 0.0f
                    val smileProb = face.smilingProbability ?: 0.0f
                    val headTurnAngle = face.headEulerAngleY
                    val lowerLipContour = face.getContour(FaceContour.LOWER_LIP_BOTTOM)
                    val upperLipContour = face.getContour(FaceContour.UPPER_LIP_TOP)

                    if (lowerLipContour != null && upperLipContour != null) {
                        val lowerLipPoints = lowerLipContour.points
                        val upperLipPoints = upperLipContour.points

                        if (lowerLipPoints.isNotEmpty() && upperLipPoints.isNotEmpty()) {
                            val lowerLipY = lowerLipPoints.map { it.y }.average().toFloat()
                            val upperLipY = upperLipPoints.map { it.y }.average().toFloat()

                            val mouthOpenDistance = lowerLipY - upperLipY

                            val faceHeight = face.boundingBox.height().toFloat()
                            val threshold = faceHeight * 0.08f  // ~8% of face height

                            if (mouthOpenDistance > threshold) {
                                Log.d("Liveness", "User's mouth is OPEN.")
                            } else {
                                Log.d("Liveness", "User's mouth is CLOSED.")
                            }
                        }
                    }

                    if (currentStep == 0 && smileProb > 0.7) {

                        completeStep("Please turn head RIGHT")
                    }
                    else if (currentStep == 1 && headTurnAngle < -15) {
                        completeStep("Please turn head LEFT")
                    }
                    else if (currentStep == 2 && headTurnAngle > 15) {
                        completeStep("Please BLINK your eyes")
                    }
                    else if (currentStep == 3 && leftEyeOpen < 0.3 && rightEyeOpen < 0.3) {
                        completeStep("Please close your RIGHT eye")
                    }
                    else if (currentStep == 4 && leftEyeOpen < 0.3 && rightEyeOpen > 0.3) {

                        completeStep("Please close your LEFT eye")
                    }
                    else if (currentStep == 5 && leftEyeOpen > 0.3 && rightEyeOpen < 0.3) {
                        completeStep("Liveness Check Completed!")
                    }

                }
                imageProxy.close()
            }
            .addOnFailureListener {
                Log.e("Liveness", "Face detection failed", it)
                imageProxy.close()
            }
    }
*/

    private fun processImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()

        val detector = FaceDetection.getClient(options)

        detector.process(image)
            .addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {
                    val face = faces[0]
                    val smileProb = face.smilingProbability ?: 0.0f
                    val leftEyeOpen = face.leftEyeOpenProbability ?: 0.0f
                    val rightEyeOpen = face.rightEyeOpenProbability ?: 0.0f
                    val headTurnAngle = face.headEulerAngleY

                    val (currentAction, _) = shuffledSteps[currentStepIndex] // Get shuffled action

                    when (currentAction) {
                        "Smile 😊" -> if (smileProb > 0.7) runOnUiThread { completeStep() }
                        "Turn Head Left 👈" -> if (headTurnAngle < -15) runOnUiThread { completeStep() }
                        "Turn Head Right 👉" -> if (headTurnAngle > 15) runOnUiThread { completeStep() }
                        "Blink Both Eyes 👀" -> if (leftEyeOpen < 0.3 && rightEyeOpen < 0.3) runOnUiThread { completeStep() }
                        "Close Left Eye 👁" -> if (leftEyeOpen < 0.3 && rightEyeOpen > 0.7) runOnUiThread { completeStep() }
                        "Close Right Eye 👁" -> if (rightEyeOpen < 0.3 && leftEyeOpen > 0.7) runOnUiThread { completeStep() }
                    }
                }
                imageProxy.close()
            }
            .addOnFailureListener {
                Log.e("Liveness", "Face detection failed", it)
                imageProxy.close()
            }
    }


    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun completeStep() {
        if (currentStepIndex < shuffledSteps.size - 1) {
            currentStepIndex++
            updateInstruction()
        } else {
            overallTimer?.cancel()
            binding.tvInstrucation.text = "Liveness Check Completed! ✅"
            binding.imageView.setImageResource(R.drawable.ic_task_completed)

            if (!isNavigationStarted) { // ✅ Prevent multiple calls
                isNavigationStarted = true
                val intent = Intent(this@MainActivity, NextActivity::class.java)
                startActivity(intent)
                finishAffinity()
            }

        }
    }

    private fun updateInstruction() {
        val (instruction, imageRes) = shuffledSteps[currentStepIndex]
        binding.tvInstrucation.text = instruction
        binding.imageView.setImageResource(imageRes)
    }

    private fun restartLivenessCheck() {
        currentStepIndex = 0
        shuffledSteps = livenessActions.shuffled() // Reshuffle steps
        updateInstruction()
        startOverallTimer() // Restart the timer
    }














}

