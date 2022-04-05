package com.innosid.shoppingeyes

import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.innosid.R
import com.innosid.databinding.ActivitySettingsBinding


class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var session: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {

        session = SharedPreferences(this)
        val newTheme = session.getTheme()
        var switchState = session.getSwitchState()
        val window: Window = this.window


        if(newTheme == "HighContrastTheme") {
            theme.applyStyle(R.style.HighContrastTheme_OCR, true)
        }else{
            theme.applyStyle(R.style.Theme_OCR, true)
        }


        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this,android.R.color.transparent)
        /*window.navigationBarColor = ContextCompat.getColor(this,android.R.color.transparent)*/

        super.onCreate(savedInstanceState)


        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val highContrast : Switch = binding.contrastSwitch
        val aboutButton = binding.aboutBtn

        if(switchState){
            highContrast.setChecked(true)
        }else{
            highContrast.setChecked(false)
        }

        highContrast.setOnCheckedChangeListener{ _, isChecked ->
            if(isChecked){
                session.setSwitchState(true)
                session.changeTheme("HighContrastTheme")
                overridePendingTransition(0, 0)
                finish()
                overridePendingTransition(0, 0)
                startActivity(getIntent())
                overridePendingTransition(0, 0)
            }
            else{
                session.setSwitchState(false)
                session.changeTheme("Theme")
                overridePendingTransition(0, 0)
                finish()
                overridePendingTransition(0, 0)
                startActivity(getIntent())
                overridePendingTransition(0, 0)
            }

        }

        aboutButton.setOnClickListener{
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }


    }


}