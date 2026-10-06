package com.example.virtualapp

import android.graphics.drawable.Drawable

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable,
    var isAdded: Boolean = false,
    var fakeAndroidId: String? = null
)
