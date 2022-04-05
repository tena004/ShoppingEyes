package com.innosid.shoppingeyes

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.innosid.R
import com.innosid.databinding.ActivityHomeScreenBinding
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.abs


class HomeScreenActivity : AppCompatActivity(){

    private lateinit var binding: ActivityHomeScreenBinding
    private lateinit var buttonPrices: Button
    private lateinit var buttonBanknotes: Button
    private lateinit var buttonSettings: Button
    private lateinit var buttonInstructions: Button
    private lateinit var session: SharedPreferences




    override fun onRestart() {
        super.onRestart()
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        session = SharedPreferences(this)

        val newTheme = session.getTheme()

        if(newTheme == "HighContrastTheme") {
            theme.applyStyle(R.style.HighContrastTheme_OCR, true)
        }else{
            theme.applyStyle(R.style.Theme_OCR, true)
        }


        val window: Window = this.window
        supportActionBar?.hide()
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this,android.R.color.transparent)
        /*window.navigationBarColor = ContextCompat.getColor(this,android.R.color.transparent)*/




        super.onCreate(savedInstanceState)


        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        buttonPrices = binding.pricesBtn
        buttonBanknotes = binding.banknotesBtn
        buttonSettings = binding.settingsBtn
        buttonInstructions = binding.instrucBtn


        buttonPrices.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        /*buttonBanknotes.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }*/
        buttonSettings.setOnClickListener{
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        buttonInstructions.setOnClickListener{
            val intent = Intent(this, InstructionsActivity::class.java)
            startActivity(intent)
        }


    }







}