package com.udacity.project4.utils

import java.text.DecimalFormat

fun Number.round(): String{
    val decimalFormat = DecimalFormat("#.#####")
    return decimalFormat.format(this)
}