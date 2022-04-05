package com.innosid.shoppingeyes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.innosid.R

class AboutActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_about)
    }
}
