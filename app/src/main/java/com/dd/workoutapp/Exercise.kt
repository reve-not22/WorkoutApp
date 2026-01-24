package com.dd.workoutapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalDate
import java.util.*

class LoggedExercise(
    type:String,
    weight:Int,
    date: LocalDate
) {
    var type by mutableStateOf(type)
    var weight by mutableIntStateOf(weight)
    var date by mutableStateOf(date)
}

fun LoggedExercise.toProto() : LoggedExerciseData =
    LoggedExerciseData.newBuilder()
        .setType(type)
        .setWeight(weight.toString())
        .setDate(date.toString())
        .build()

fun LoggedExerciseData.toDomain() : LoggedExercise =
    LoggedExercise(
        type = type,
        weight = weight.toIntOrNull() ?: 0,
        date = LocalDate.parse(date)
    )

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

fun Exercise.toLoggedExercise(date:LocalDate): LoggedExercise =
    LoggedExercise(
        type = type,
        weight = weight.toIntOrNull() ?: 0,
        date=date
    )

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
