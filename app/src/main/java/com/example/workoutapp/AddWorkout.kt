package com.example.workoutapp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController


open class ModifyWorkoutViewModel: ViewModel() {
    var exerciseList = mutableStateListOf<Exercise>()

    var workoutNameState = TextFieldState("")

    var newType = mutableStateOf ("")
    var newWeight = mutableStateOf ("")
    var newReps = mutableStateOf ("")
    var newSets = mutableStateOf ("")

    fun resetNewValues() {
        newType.value = ""
        newWeight.value = ""
        newReps.value = ""
        newSets.value = ""
    }

    fun addExercise(e: Exercise) {exerciseList.add(e)}
    fun removeExercise(e: Exercise) {exerciseList.remove(e)}

    fun updateExercise(
        exercise: Exercise,
        type: String? = null,
        weight: String? = null,
        reps: String? = null,
        sets: String? = null
    ) {
        exercise.apply {
            type?.let{this.type = it}
            weight?.let {this.weight = it}
            reps?.let {this.reps = it}
            sets?.let {this.sets = it}
        }
    }
}
class AddWorkoutViewModel : ModifyWorkoutViewModel() {
    fun saveWorkout(workoutViewModel: WorkoutViewModel) {
        workoutViewModel.addWorkout(Workout(exerciseList, workoutNameState.text as String))
    }
}

@Composable
fun AddWorkoutScreen(navController: NavController, workoutViewModel: WorkoutViewModel) {
    val addWorkoutViewModel: AddWorkoutViewModel = viewModel()

    Scaffold (
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                OutlinedTextField(
                    state = addWorkoutViewModel.workoutNameState,
                    lineLimits = TextFieldLineLimits.SingleLine
                )

                PlusButton(
                    onClick = {
                        addWorkoutViewModel.saveWorkout(workoutViewModel)
                        navController.navigate("home")
                    },
                    modifier = Modifier
                        .padding(8.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.secondary
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            AddExerciseWidget(addWorkoutViewModel,
                onAdd = { newExercise ->
                    addWorkoutViewModel.addExercise(newExercise)
                }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(addWorkoutViewModel.exerciseList.size, key = {index -> addWorkoutViewModel.exerciseList[index].hashCode()}) {index ->
                    val exercise = addWorkoutViewModel.exerciseList[index]

                    val dismissState = rememberSwipeToDismissBoxState (
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == SwipeToDismissBoxValue.StartToEnd || dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                addWorkoutViewModel.removeExercise(exercise)
                                true
                            } else false
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = true,
                        enableDismissFromEndToStart = false,
                        backgroundContent = {
                            val color = when (dismissState.dismissDirection) {
                                SwipeToDismissBoxValue.StartToEnd -> Color.Red
                                SwipeToDismissBoxValue.EndToStart -> Color.Blue
                                SwipeToDismissBoxValue.Settled -> Color.Transparent
                            }

                            val i: ImageVector? = when (dismissState.dismissDirection) {
                                SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Delete
                                SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                                SwipeToDismissBoxValue.Settled -> null
                            }

                            Row (
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color)
                                    .padding(12.dp, 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (i != null) {
                                    Icon(
                                        i,
                                        contentDescription = "delete"
                                    )
                                }
                                Spacer(modifier = Modifier)
                                /*Icon(
                                    painter = painterResource(R.drawable.archive),
                                    contentDescription = "Archive"
                                )*/
                            }
                        },
                        content = {
                            ExerciseWidget(addWorkoutViewModel, exercise, false)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AddExerciseWidget(modifyWorkoutViewModel: ModifyWorkoutViewModel, onAdd: (Exercise) -> Unit, modifier: Modifier = Modifier)
{
    Row (
        modifier = modifier
            .fillMaxWidth()
    ) {
        ExerciseField("Type", modifyWorkoutViewModel.newType.value, {modifyWorkoutViewModel.newType.value = it}, true, Modifier.weight(1f))
        ExerciseField("Weight", modifyWorkoutViewModel.newWeight.value, {modifyWorkoutViewModel.newWeight.value = it}, true, Modifier.weight(1f))
        ExerciseField("Reps", modifyWorkoutViewModel.newReps.value, {modifyWorkoutViewModel.newReps.value = it}, true, Modifier.weight(1f))
        ExerciseField("Sets", modifyWorkoutViewModel.newSets.value, {modifyWorkoutViewModel.newSets.value = it}, true, Modifier.weight(1f))

        PlusButton(
            onClick = {
                onAdd(Exercise(modifyWorkoutViewModel.newType.value,modifyWorkoutViewModel.newWeight.value, modifyWorkoutViewModel.newReps.value,modifyWorkoutViewModel.newSets.value))
                modifyWorkoutViewModel.resetNewValues()
            },
            modifier = Modifier
                .padding(top=10.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun ExerciseWidget(modifyWorkoutViewModel: ModifyWorkoutViewModel, exercise: Exercise, defEnabled: Boolean = false, modifier: Modifier = Modifier)
{
    var isEnabled by remember { mutableStateOf(defEnabled)}
    Row(
        modifier = modifier
            .border(width=2.dp, Color.White, RoundedCornerShape(6.dp))
            .fillMaxWidth()
            .combinedClickable (
                onClick = {
                },
                onDoubleClick = {
                    if (!isEnabled) {
                        isEnabled = true
                    } else {
                        isEnabled = false
                    }
                },
                onLongClick = {
                    if (!isEnabled) {
                        isEnabled = true
                    } else {
                        isEnabled = false
                    }
                }
            )
    ) {
        println(exercise.type)
        ExerciseField("Type", exercise.type,
            {modifyWorkoutViewModel.updateExercise(exercise, type = it)}, isEnabled, Modifier.weight(1f).padding(top=12.dp, bottom = 12.dp))
        ExerciseField("Weight", exercise.weight,
            {modifyWorkoutViewModel.updateExercise(exercise, weight = it)}, isEnabled, Modifier.weight(1f).padding(top=12.dp, bottom = 12.dp))
        ExerciseField("Reps", exercise.reps,
            {modifyWorkoutViewModel.updateExercise(exercise, reps = it)}, isEnabled, Modifier.weight(1f).padding(top=12.dp, bottom = 12.dp))
        ExerciseField("Sets", exercise.sets,
            {modifyWorkoutViewModel.updateExercise(exercise, sets = it)}, isEnabled, Modifier.weight(1f).padding(top=12.dp, bottom = 12.dp))
    }
}

@Composable
fun ExerciseField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
)
{
    OutlinedTextField(
        enabled = enabled,
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier
            .padding(2.dp),
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = Color.Gray.copy(alpha = .9f),
            disabledBorderColor = Color.Gray.copy(alpha = .9f),
            disabledLabelColor = Color.DarkGray.copy(alpha = .9f),
            disabledLeadingIconColor = Color.Black.copy(alpha = .9f),
            disabledTrailingIconColor = Color.Black.copy(alpha = .9f),
            disabledPlaceholderColor = Color.Gray.copy(alpha = .9f)
        )
    )
}




