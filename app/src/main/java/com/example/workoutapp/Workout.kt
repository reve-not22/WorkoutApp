package com.example.workoutapp

import java.util.*

class Workout(
    var exerciseList: MutableList<Exercise> = mutableListOf(),
    var workoutName: String,
    val id: UUID = UUID.randomUUID()
)

fun Workout.toProto(): WorkoutData =
    WorkoutData.newBuilder()
        .setId(id.toString())
        .setName(workoutName)
        .addAllExercises(exerciseList.map {it.toProto()})
        .build()

fun WorkoutData.toDomain(): Workout =
    Workout(
        workoutName = name,                 // from proto
        id = UUID.fromString(id),            // from proto
        exerciseList = exercisesList
            .map { it.toDomain() }
            .toMutableList()
    )
