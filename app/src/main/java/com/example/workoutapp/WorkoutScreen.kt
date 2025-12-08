package com.example.workoutapp

import android.R.attr.value
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(navController: NavController, workout: Workout) {
    Scaffold (
        topBar = {
            TopAppBar(
                title = {
                    Box (
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(workout.workoutName)
                    }
                }
            )
        }
    ) {
        paddingValues ->
        LazyColumn (
            modifier = Modifier.fillMaxSize(),
            contentPadding = paddingValues,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(workout.exerciseList.size, key = {index -> workout.exerciseList[index].hashCode()}) {index ->
                val exercise = workout.exerciseList[index]

                WorkoutExerciseWidget(exercise)
            }
        }
    }
}

@Composable
fun WorkoutExerciseWidget(exercise: Exercise, modifier: Modifier = Modifier)
{
    Row(
        modifier = modifier
            .fillMaxWidth()
    ) {
        val weightValue = exercise.weight.toIntOrNull() ?: 0
        val repsValue = exercise.reps.toIntOrNull() ?: 0
        val setsValue = exercise.sets.toIntOrNull() ?: 0

        ExerciseField("Type", exercise.type, {exercise.type = it}, false, Modifier.weight(1f))
        Stepper("Weight", weightValue, {exercise.weight = it.toString()}, 1, Modifier.weight(1f))
        Stepper("Reps", repsValue, {exercise.reps = it.toString()}, 1,Modifier.weight(1f))
        Stepper("Sets", setsValue, {exercise.sets = it.toString()}, 1, Modifier.weight(1f))
    }
}

@Composable
fun Stepper(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    step: Int = 1,
    modifier: Modifier = Modifier
)
{
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
            .padding(horizontal = 2.dp, vertical = 2.dp)
            .border(1.dp, Color.Gray.copy(alpha = .9f), shape = RoundedCornerShape(8.dp))
    ) {
        IconButton(
            onClick = {onValueChange(value - step)},
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Decrease")
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                fontSize = 12.sp
            )
            Text(
                "$value",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 12.sp
            )
        }

        IconButton(
            onClick = {onValueChange(value + step)},
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Increase")
        }
    }

}
