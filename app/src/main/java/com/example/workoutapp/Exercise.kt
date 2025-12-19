package com.example.workoutapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.UUID

class Exercise(
    type: String,
    weight: String,
    reps: String,
    sets: String,
    var id: UUID = UUID.randomUUID()
) {
    var type by mutableStateOf(type)
    var weight by mutableStateOf(weight)
    var reps by mutableStateOf(reps)
    var sets by mutableStateOf(sets)

    fun copyExercise() = Exercise(type, weight, reps, sets)

}

fun Exercise.toProto(): ExerciseData =
    ExerciseData.newBuilder()
        .setId(id.toString())
        .setType(type)
        .setWeight(weight)
        .setReps(reps)
        .setSets(sets)
        .build()

fun ExerciseData.toDomain(): Exercise =
    Exercise (
        id = UUID.fromString(id),
        type = type,
        weight = weight,
        reps = reps,
        sets = sets
    )
