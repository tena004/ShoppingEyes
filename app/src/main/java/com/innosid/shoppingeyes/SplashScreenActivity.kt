package com.innosid.shoppingeyes

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.innosid.R
import com.innosid.databinding.ActivitySplashScreenBinding


class SplashScreenActivity : AppCompatActivity() {

    private lateinit var session: SharedPreferences
    private lateinit var binding: ActivitySplashScreenBinding

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


        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        var appLogo = binding.logo
        if(newTheme == "HighContrastTheme") {
            appLogo.setImageResource(R.drawable.logo_light)
        }else{
            appLogo.setImageResource(R.drawable.logo_dark)
        }
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@SplashScreenActivity, HomeScreenActivity::class.java)
            startActivity(intent)
            finish()
        }, 2000)
    }
}


