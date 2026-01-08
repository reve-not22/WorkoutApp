package com.dd.workoutapp

import java.util.UUID

data class ActiveWorkout(
    val workoutId: UUID,
    val uncompletedExercises: List<Exercise>
)

fun ActiveWorkoutData.toDomain() : ActiveWorkout =
    ActiveWorkout(
        workoutId = UUID.fromString(id),
        uncompletedExercises = remainingExercisesList.map{it.toDomain()}.toMutableList()
    )

fun ActiveWorkout.toProto() : ActiveWorkoutData =
    ActiveWorkoutData.newBuilder()
        .setId(workoutId.toString())
        .addAllRemainingExercises(uncompletedExercises.map{it.toProto()})
        .build()