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
import kotlinx.android.synthetic.main.activity_settings.*


class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var session: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {

        session = SharedPreferences(this)
        val newTheme = session.getTheme()
        val switchState = session.getSwitchState()
        val fontSwitch = session.getFontSwitchState()
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
        val largeFont : Switch = binding.fontSwitch

        val text1 = binding.textView
        val text2 = binding.textView2

        highContrast.isChecked = switchState

        largeFont.isChecked = fontSwitch

        highContrast.setOnCheckedChangeListener{ _, isChecked ->
            if(isChecked){
                session.setSwitchState(true)
                session.changeTheme("HighContrastTheme")
                overridePendingTransition(0, 0)
                finish()
                overridePendingTransition(0, 0)
                startActivity(intent)
                overridePendingTransition(0, 0)
            }
            else{
                session.setSwitchState(false)
                session.changeTheme("Theme")
                overridePendingTransition(0, 0)
                finish()
                overridePendingTransition(0, 0)
                startActivity(intent)
                overridePendingTransition(0, 0)
            }

        }

        largeFont.setOnCheckedChangeListener{ _, isChecked ->
            if(isChecked){
                session.setFontSwitchState(true)
                session.changeFont("Large")
                text1.textSize = 30F
                text2.textSize = 30F
            }
            else{
                session.setFontSwitchState(false)
                session.changeFont("Normal")
                text1.textSize = 25F
                text2.textSize = 25F
            }

        }

        aboutButton.setOnClickListener{
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }


    }


}