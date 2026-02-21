package com.dd.workoutapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController

class EditWorkoutViewModel(
    val workout: Workout
) : ModifyWorkoutViewModel() {

    init{
        this.exerciseList = mutableStateListOf<Exercise>().apply {
            addAll(workout.exerciseList)
        }
        this.workoutNameState = TextFieldState(workout.workoutName)
    }

    fun updateWorkout() {
        workout.workoutName = workoutNameState.text.toString()
        workout.exerciseList = exerciseList
    }
}

class EditWorkoutViewModelFactory(
    private val workout: Workout
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditWorkoutViewModel(workout) as T
    }
}

@Composable
fun EditWorkoutScreen(navController: NavController, editWorkoutViewModel: EditWorkoutViewModel, workoutViewModel: WorkoutViewModel) {

    var showDialog by remember {mutableStateOf(true)}
    var recover by remember { mutableStateOf<Boolean?>(null) }

    val currentName = editWorkoutViewModel.workoutNameState.text as String
    val currentEList = editWorkoutViewModel.exerciseList

    LaunchedEffect(Unit) {
        if (!workoutViewModel.draftMap.contains("edit") || workoutViewModel.draftMap["edit"]?.workoutName != currentName) {
            workoutViewModel.addDraft("edit", Workout(currentEList, currentName))
            showDialog = false
        }
    }

    val draft = workoutViewModel.draftMap["edit"]

    if (showDialog &&
        draft != null &&
        draft.exerciseList.isNotEmpty() &&
        draft.workoutName == currentName) {

        AlertDialog(
            onDismissRequest = {showDialog = false},
            confirmButton = {
                Button(onClick = {
                    recover = true
                    showDialog = false
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    recover = false
                    workoutViewModel.addDraft("edit", Workout(currentEList, currentName))
                }) {
                    Text("No")
                }
            },
            title = {
                Text("Recover workout edit?")
            }
        )
    }

    LaunchedEffect(recover) {
        when (recover) {
            true -> {
                editWorkoutViewModel.exerciseList.clear()
                for (exercise in workoutViewModel.draftMap["edit"]?.exerciseList!!) {
                    editWorkoutViewModel.addExercise(exercise)
                }
                workoutViewModel.draftMap["edit"]?.exerciseList = currentEList
            }
            false -> {
                workoutViewModel.addDraft("edit", Workout(currentEList, currentName))
            }
            null -> {}
        }
    }

    Scaffold (
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Row (
                    modifier = Modifier.fillMaxWidth()
                ){
                    TrashButton(
                        onClick = {
                            workoutViewModel.deleteWorkout(editWorkoutViewModel.workout)
                            navController.navigate("home") {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier
                            .padding(horizontal=8.dp)
                            .weight(1f),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.secondary
                    )
                    OutlinedTextField(
                        state = editWorkoutViewModel.workoutNameState,
                        lineLimits = TextFieldLineLimits.SingleLine,
                        modifier = Modifier
                            .weight(2f)
                    )
                    CheckButton(
                        onClick = {
                            editWorkoutViewModel.updateWorkout()
                            workoutViewModel.persistState()
                            navController.navigate("home")
                        },
                        modifier = Modifier
                            .padding(horizontal=8.dp)
                            .weight(1f),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.secondary
                    )
                }
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
            AddExerciseWidget(editWorkoutViewModel,
                onAdd = { newExercise ->
                    editWorkoutViewModel.addExercise(newExercise)
                }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(editWorkoutViewModel.exerciseList.size, key = {index -> editWorkoutViewModel.exerciseList[index].id}) {index ->
                    val exercise = editWorkoutViewModel.exerciseList[index]

                    val dismissState = rememberSwipeToDismissBoxState (
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == SwipeToDismissBoxValue.StartToEnd || dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                editWorkoutViewModel.removeExercise(exercise)
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
                            ExerciseWidget(Modifier, editWorkoutViewModel, exercise, false)
                        }
                    )
                }
            }
        }
    }
}