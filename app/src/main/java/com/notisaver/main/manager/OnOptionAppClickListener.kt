package com.notisaver.main.manager

import android.view.MenuItem

interface OnOptionAppClickListener {
    fun onOptionSelected(packageHashcode: String, itemMenuSelected: MenuItem)
    fun onAppClick(packageHashcode: String)
}