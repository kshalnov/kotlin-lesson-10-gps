package ru.gb.course1.kotlin_lesson_10_gps

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.gb.course1.kotlin_lesson_10_gps.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}