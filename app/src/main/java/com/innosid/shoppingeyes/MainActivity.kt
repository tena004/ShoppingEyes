package com.innosid.shoppingeyes

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.*
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.text.method.ScrollingMovementMethod
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.innosid.R
import com.innosid.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.math.abs


//TODO intents fragments activities (less visibility changes)
//TODO real time photo analysis (textBoxing) something like wait x sec, than capture if recognizedText != null
//TODO EUR detection


class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    companion object {
        //5 sec delta time after making an action once (doubleTap or doubleSwipe)
        const val DOUBLE_CLICK_TIME_DELTA: Long = 5000

        const val REQUEST_CODE_PERMISSIONS = 1
        val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
        )
    }

    private lateinit var buttonTake: Button
    private lateinit var buttonShoot: Button

    private lateinit var imgView: ImageView

    private lateinit var textViewData: TextView

    private lateinit var speechRecognizer: SpeechRecognizer

    private lateinit var tts: TextToSpeech

    //start time for calculate delta for double-actions
    private var lastClickTime: Long = 0

    private lateinit var gestureDetector: GestureDetectorCompat

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private var imageCapture: ImageCapture? = null

    private lateinit var binding: ActivityMainBinding

    private lateinit var session: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {

        session = SharedPreferences(this)

        val newTheme = session.getTheme()

        if(newTheme == "HighContrastTheme") {
            theme.applyStyle(R.style.HighContrastTheme_OCR, true)
        }else{
            theme.applyStyle(R.style.Theme_OCR, true)
        }

        val window: Window = this.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this,android.R.color.transparent)

        super.onCreate(savedInstanceState)
        //for easier linking
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        buttonTake = binding.buttonTake
        buttonShoot = binding.buttonShoot
        imgView = binding.imgView

        textViewData = binding.textViewData
        if (session.getFont() == "Large"){
            textViewData.textSize = 35F
        }else{
            textViewData.textSize = 30F
        }
        textViewData.movementMethod = ScrollingMovementMethod()
        //custom gestures applicable on textView too
        textViewData.setOnTouchListener { v, event ->
            gestureDetector.onTouchEvent(event)
        }

        gestureDetector = GestureDetectorCompat(this, MyGestureListener())

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(MySpeechRecognizer())

        tts = TextToSpeech(this, this)

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        buttonTake.setOnClickListener {
            startCamera()
        }


        buttonShoot.setOnClickListener {
            shootImage()

            //TODO get back to MainActivity
            prevView.visibility = View.GONE
            buttonShoot.visibility = View.GONE

            imgView.visibility = View.VISIBLE
            buttonTake.visibility = View.VISIBLE
            textViewData.visibility = View.VISIBLE
        }

        startCamera()
    }

    //for correct photo orientation (for text recognition)
    override fun onStart() {
        super.onStart()
        orientationEventListener.enable()
    }

    override fun onResume() {
        super.onResume()
        orientationEventListener.enable()
    }

    //when you deny permission twice it won't be asked again (from android API 30)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(this, "PERMISSIONS NOT GRANTED.", Toast.LENGTH_SHORT).show()
                //the app will close if it won't get the permissions
                finish()
            }
        }
    }

    //for TTS
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            //val result = textToSpeech.setLanguage(Locale.forLanguageTag("hu-HU"))
            val result = tts.setLanguage(Locale.ENGLISH)
            tts.setSpeechRate(0.8f)
            tts.setPitch(1.1f)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                val installIntent = Intent()
                installIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
                startActivity(installIntent)
            }
        } else {
            val installIntent = Intent()
            installIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
            startActivity(installIntent)
        }
    }

    //for gestures (swipes and hold)
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return if (gestureDetector.onTouchEvent(event)) {
            true
        } else {
            super.onTouchEvent(event)
        }
    }

    override fun onPause() {
        super.onPause()
        orientationEventListener.disable()
        tts.stop()
    }

    override fun onStop() {
        super.onStop()
        speechRecognizer.destroy()
        tts.shutdown()
    }


//----------------------------------------------------------------------------------------------------------------------


    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        //TODO start a new Activity
        imgView.visibility = View.GONE
        buttonTake.visibility = View.GONE
        textViewData.visibility = View.GONE

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            //cameraSelector use case
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            //preview use case
            val preview = Preview.Builder().build().also { myPreview ->
                myPreview.setSurfaceProvider(
                    binding.prevView.surfaceProvider
                )
            }
            //imageCapture use case
            imageCapture = ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY).build()
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                Toast.makeText(this, "CAMERA START FAIL " + e.message, Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
        //TODO
        prevView.visibility = View.VISIBLE
        buttonShoot.visibility = View.VISIBLE
    }

    private fun shootImage() {
        imageCapture?.takePicture(ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageCapturedCallback() {
            @SuppressLint("UnsafeOptInUsageError")
            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                //for preview
                val imageBitmap = imageProxyToBitmap(imageProxy)
                imgView.setImageBitmap(imageBitmap)
                //to process
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val rawImage = InputImage.fromMediaImage(
                        mediaImage, imageProxy.imageInfo.rotationDegrees
                    )
                    textRecognizer.process(rawImage).addOnSuccessListener { visionText ->
                        textViewData.text = getTextFromImage(visionText)
                        //tts!!.speak(textViewData.text, TextToSpeech.QUEUE_FLUSH, null,"")
                    }.addOnFailureListener { error ->
                        Toast.makeText(
                            this@MainActivity, "TEXT RECOGNITION ERROR" + error.message, Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                imageProxy.close()
            }
        })
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
        val planeProxy = image.planes[0]
        val buffer = planeProxy.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer[bytes]
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    //change (imageCapture's) use case's targetRotation if orientation changed
    private val orientationEventListener by lazy {
        object : OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                val rotation = when (orientation) {
                    in 45 until 135 -> Surface.ROTATION_270
                    in 135 until 225 -> Surface.ROTATION_180
                    in 225 until 315 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }
                imageCapture?.targetRotation = rotation
            }
        }
    }

    private fun getTextFromImage(visionText: Text?): String {
        val blockList = visionText?.textBlocks
        return if (blockList?.size == 0) {
            "NO TEXT FOUND"
        } else {
            var recognizedText = ""
            for (block in visionText?.textBlocks!!) {
                for (line in block.lines) {
                    for (element in line.elements) {
                        recognizedText += element.text + " "
                    }
                }
                recognizedText += "\n"
            }
            recognizedText
        }
    }


    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun voiceCommandInput() {
        val voiceIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        voiceIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        //voiceIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hu-HU")
        speechRecognizer.startListening(intent)
    }


//----------------------------------------------------------------------------------------------------------------------


    internal inner class MySpeechRecognizer : RecognitionListener {
        override fun onResults(results: Bundle) {
            val data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            for (command in data!!.iterator()) {
                when {
                    arrayOf("camera", "take", "detect", "recognize").any { it in command } -> {
                        startCamera()
                    }
                    arrayOf("stop", "pause", "OK").any { it in command } -> {
                        tts.stop()
                    }
                }
            }
        }

        //must implement all method
        override fun onReadyForSpeech(params: Bundle) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray) {}
        override fun onEndOfSpeech() {}
        override fun onError(error: Int) {}
        override fun onPartialResults(partialResults: Bundle) {}
        override fun onEvent(eventType: Int, params: Bundle) {}
    }

    internal inner class MyGestureListener : GestureDetector.SimpleOnGestureListener() {
        //to avoid unintentional gesture detections
        private val threshold = 100

        override fun onLongPress(e: MotionEvent?) {
            this@MainActivity.voiceCommandInput()
        }

        override fun onFling(downEvent: MotionEvent?,
                             moveEvent: MotionEvent?,
                             velocityX: Float,
                             velocityY: Float): Boolean {
            val diffX = moveEvent?.x?.minus(downEvent!!.x) ?: 0.0F
            val diffY = moveEvent?.y?.minus(downEvent!!.y) ?: 0.0F

            return if (abs(diffX) > abs(diffY)) {
                // R/L
                if (abs(diffX) > threshold && abs(velocityX) > threshold) {
                    if (diffX > 0) {
                        this@MainActivity.onSwipeRight()
                    } else {
                        this@MainActivity.onLeftSwipe()
                    }
                    true
                } else {
                    super.onFling(downEvent, moveEvent, velocityX, velocityY)
                }
            } else {
                // U/D
                if (abs(diffY) > threshold && abs(velocityY) > threshold) {
                    if (diffY > 0) {
                        this@MainActivity.onSwipeDown()
                    } else {
                        this@MainActivity.onSwipeUp()
                    }
                    true
                } else {
                    super.onFling(downEvent, moveEvent, velocityX, velocityY)
                }
            }
        }
    }

    private fun onSwipeDown() {
        val currentClickTime = System.currentTimeMillis()
        if (currentClickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
            speak("TODO")
        } else {
            speak("SWIPE DOWN")
            lastClickTime = currentClickTime
        }
    }

    private fun onSwipeUp() {
        val currentClickTime = System.currentTimeMillis()
        if (currentClickTime - lastClickTime > DOUBLE_CLICK_TIME_DELTA) {
            speak("SWIPE UP")
            lastClickTime = currentClickTime
        }
    }

    private fun onSwipeRight() {
        val currentClickTime = System.currentTimeMillis()
        if (currentClickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
            startCamera()
        } else {
            speak("TAKE A PICTURE WITH CAMERA. SWIPE RIGHT AGAIN TO BEGIN.")
            lastClickTime = currentClickTime
        }
    }

    private fun onLeftSwipe() {
        val currentClickTime = System.currentTimeMillis()
        if (currentClickTime - lastClickTime > DOUBLE_CLICK_TIME_DELTA) {
            speak("LEFT SWIPE")
            lastClickTime = currentClickTime
        }
    }
}