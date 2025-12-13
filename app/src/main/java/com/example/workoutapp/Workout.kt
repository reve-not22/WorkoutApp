package com.example.workoutapp

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList

class Workout(
    var exerciseList: SnapshotStateList<Exercise> = mutableStateListOf(),
    var workoutName: String
)
