package ru.gb.course1.kotlin_lesson_10_gps.util

import android.location.Location

fun Location.toPrintString(): String {
    return "[$latitude, $longitude]"
}