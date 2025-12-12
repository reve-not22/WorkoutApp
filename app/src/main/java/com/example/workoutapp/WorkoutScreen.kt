package com.example.workoutapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class WorkoutScreenViewModelFactory(
    private val workout: Workout
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WorkoutScreenViewModel(workout) as T
    }
}

class WorkoutScreenViewModel(var workout: Workout): ViewModel() {

    val completedList = mutableListOf<Exercise>()
    val uncompletedList = mutableStateListOf<Exercise>().apply {
        addAll(workout.exerciseList.map { it.copyExercise() } )
    }

    fun getWk(): Workout {
        return workout
    }

    fun updateWeight(exercise: Exercise, weight: Int) {
        exercise.weight = weight.toString()
    }

    fun updateReps(exercise: Exercise, reps: Int) {
        exercise.reps = reps.toString()
    }

    fun updateSets(exercise: Exercise, sets: String) {
        exercise.sets = sets
    }

    fun logExercise(workoutViewModel: WorkoutViewModel, exercise: Exercise) {
        exercise.sets = (exercise.sets.toIntOrNull()?.minus(1)).toString()
        completedList.add(exercise.copyExercise())

        exercise.sets.toIntOrNull()?.let {
            if (it <= 0) {
                uncompletedList.remove(exercise)
            }
        }
        if (uncompletedList.isEmpty()) {
            //finalize workout
            checkMaxExercise(workoutViewModel)
        }
    }

    fun checkMaxExercise(workoutViewModel: WorkoutViewModel){
        val grouped = completedList.groupBy { it.type }

        for ((type, exercisesOfType) in grouped) {
            val maxWeight = exercisesOfType.maxOfOrNull { it.weight.toIntOrNull() ?: 0 }
            val maxReps = exercisesOfType.maxOfOrNull { it.reps.toIntOrNull() ?: 0 }

            val globalExercise =
                workoutViewModel.workoutList.find { it == getWk() }?.exerciseList?.find { it.type == type }

            if (globalExercise != null) {
                if (maxWeight != null && maxWeight > globalExercise.weight.toInt()) {
                    globalExercise.weight = maxWeight.toString()
                }

                if (maxReps != null && maxReps > globalExercise.reps.toInt()) {
                    globalExercise.reps = maxReps.toString()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(workoutViewModel:WorkoutViewModel, workoutScreenViewModel: WorkoutScreenViewModel) {
    Scaffold (
        topBar = {
            TopAppBar(
                title = {
                    Box (
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(workoutScreenViewModel.getWk().workoutName)
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
            items(workoutScreenViewModel.uncompletedList, key = {it.hashCode()}) {exercise ->
                WorkoutExerciseWidget(workoutViewModel, workoutScreenViewModel, exercise)
            }
        }
    }
}

@Composable
fun WorkoutExerciseWidget(workoutViewModel: WorkoutViewModel, workoutScreenViewModel: WorkoutScreenViewModel, exercise: Exercise, modifier: Modifier = Modifier)
{
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val weightValue = exercise.weight.toIntOrNull() ?: 0
        val repsValue = exercise.reps.toIntOrNull() ?: 0

        IconButton(
            onClick = {
                //log exercise
                workoutScreenViewModel.logExercise(workoutViewModel, exercise)
            },
            Modifier.weight(0.5f)
        ) {
            Icon(Icons.Default.Check, contentDescription = "Complete")
        }
        ExerciseField("Type", exercise.type, {}, false,
            Modifier.weight(1f).fillMaxWidth())
        Stepper("Weight", weightValue, {newValue -> workoutScreenViewModel.updateWeight(exercise, newValue)}, 1,
            Modifier.weight(1f).fillMaxWidth())
        Stepper("Reps", repsValue, {newValue -> workoutScreenViewModel.updateReps(exercise, newValue)}, 1,
            Modifier.weight(1f).fillMaxWidth())
        ExerciseField("Sets left", exercise.sets, {newValue -> workoutScreenViewModel.updateSets(exercise, newValue)}, false,
            Modifier.weight(1f).fillMaxWidth())
    }
}

@Composable
fun Stepper(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    step: Int = 1,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        OutlinedTextField(
            value = value.toString(),
            onValueChange = { newText ->
                newText.toIntOrNull()?.let { onValueChange(it) }
            },
            label = { Text(label) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = false,
            textStyle = TextStyle(
                textAlign = TextAlign.Center
            ),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.Gray.copy(alpha = .9f),
                disabledBorderColor = Color.Gray.copy(alpha = .9f),
                disabledLabelColor = Color.DarkGray.copy(alpha = .9f),
                disabledLeadingIconColor = Color.Black.copy(alpha = .9f),
                disabledTrailingIconColor = Color.Black.copy(alpha = .9f),
                disabledPlaceholderColor = Color.Gray.copy(alpha = .9f)
            )
        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = modifier
                .padding(horizontal = 2.dp, vertical = 2.dp)
                .matchParentSize()
        ) {
            IconButton(
                onClick = { onValueChange(value - step) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Decrease")
            }

            IconButton(
                onClick = { onValueChange(value + step) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Increase")
            }
        }
    }
}
