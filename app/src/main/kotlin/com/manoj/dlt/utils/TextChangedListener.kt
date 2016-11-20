package com.manoj.dlt.utils

import android.text.Editable
import android.text.TextWatcher

abstract class TextChangedListener : TextWatcher {

    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
    }

    override fun afterTextChanged(editable: Editable) {
    }
}
