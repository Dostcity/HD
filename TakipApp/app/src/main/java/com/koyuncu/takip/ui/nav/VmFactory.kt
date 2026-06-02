package com.koyuncu.takip.ui.nav

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/** Lambda ile ViewModel üreten basit factory. */
@Suppress("UNCHECKED_CAST")
fun <T : ViewModel> simpleFactory(creator: () -> T): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = creator() as VM
    }
