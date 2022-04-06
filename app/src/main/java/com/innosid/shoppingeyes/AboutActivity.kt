package com.innosid.shoppingeyes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.innosid.R
import com.innosid.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var session: SharedPreferences
    private lateinit var binding: ActivityAboutBinding


    override fun onCreate(savedInstanceState: Bundle?) {

        session = SharedPreferences(this)

        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val newTheme = session.getTheme()
        val font = session.getFont()

        if(newTheme == "HighContrastTheme") {
            theme.applyStyle(R.style.HighContrastTheme_OCR, true)

        }else{
            theme.applyStyle(R.style.Theme_OCR, true)
        }

        val window: Window = this.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this,android.R.color.transparent)

        val text1 = binding.Supervisor
        val text2 = binding.Team
        val text3 = binding.project
        val text4 = binding.projectName

        if (font == "Normal"){
            text1.textSize = 18F
            text2.textSize = 18F
            text3.textSize = 18F
            text4.textSize = 16F
        }else{
            text1.textSize = 23F
            text2.textSize = 23F
            text3.textSize = 23F
            text4.textSize = 21F
        }


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
    }
}
