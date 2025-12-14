package com.example.workoutapp

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.util.UUID

class Workout(
    var exerciseList: MutableList<Exercise> = mutableListOf(),
    var workoutName: String,
    val id: UUID = UUID.randomUUID()
)
