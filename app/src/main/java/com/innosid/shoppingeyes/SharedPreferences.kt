package com.innosid.shoppingeyes

import android.content.Context
import android.content.SharedPreferences

class SharedPreferences(context: Context) {
    private var sharedPref = "preferences"
    private var preferences: SharedPreferences = context.getSharedPreferences(sharedPref, Context.MODE_PRIVATE)
    private var editor: SharedPreferences.Editor = preferences.edit()

    private var THEME = "theme"
    private var FONT_SIZE = "fontSize"
    private var SWITCH_STATE = "switchState"
    private var FONT_SWITCH = "fontSwitchState"




    fun getSwitchState(): Boolean {
        return preferences.getBoolean(SWITCH_STATE, false)
    }


    fun setSwitchState(state: Boolean){
        editor.putBoolean(SWITCH_STATE, state)
        editor.commit()
    }

    fun getTheme(): String? {
        return preferences.getString(THEME, "Theme")
    }

    fun changeTheme(theme: String){
        editor.putString(THEME, theme)
        editor.commit()
    }

    fun getFont(): String? {
        return preferences.getString(FONT_SIZE, "Normal")
    }

    fun changeFont(font: String){
        editor.putString(FONT_SIZE, font)
        editor.commit()
    }

    fun getFontSwitchState() : Boolean {
        return preferences.getBoolean(FONT_SWITCH, false)
    }

    fun setFontSwitchState(large: Boolean){
        editor.putBoolean(FONT_SWITCH, large)
        editor.commit()
    }
}