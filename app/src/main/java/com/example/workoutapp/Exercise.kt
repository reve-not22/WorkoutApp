package com.example.workoutapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.UUID

class Exercise(
    type: String,
    weight: String,
    reps: String,
    sets: String
) {
    var type by mutableStateOf(type)
    var weight by mutableStateOf(weight)
    var reps by mutableStateOf(reps)
    var sets by mutableStateOf(sets)
}
