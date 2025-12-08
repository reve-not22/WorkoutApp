package com.example.workoutapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndSelectAll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun AddWorkoutScreen(navController: NavController) {

    val exerciseList = remember { mutableStateListOf<Exercise>() }

    Scaffold (
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                val state = rememberTextFieldState("")
                OutlinedTextField(
                    state = state,
                    lineLimits = TextFieldLineLimits.SingleLine
                )

                AddPlusButton(
                    onClick = {
                        GlobalVars.workoutList.add(Workout(exerciseList, state.text as String))
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
            AddExerciseWidget(
                onAdd = { newExercise ->
                    exerciseList.add(newExercise)
                }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(exerciseList.size, key = {index -> exerciseList[index].hashCode()}) {index ->
                    val exercise = exerciseList[index]

                    val dismissState = rememberSwipeToDismissBoxState (
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == SwipeToDismissBoxValue.StartToEnd || dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                exerciseList.remove(exercise)
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
                                SwipeToDismissBoxValue.Settled, null -> Color.Transparent
                            }

                            val i: ImageVector? = when (dismissState.dismissDirection) {
                                SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Delete
                                SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                                SwipeToDismissBoxValue.Settled, null -> null
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
                            ExerciseWidget(exercise)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AddExerciseWidget(onAdd: (Exercise) -> Unit, modifier: Modifier = Modifier)
{
    var type by remember { mutableStateOf ("")}
    var weight by remember { mutableStateOf ("")}
    var reps by remember { mutableStateOf ("")}
    var sets by remember { mutableStateOf ("")}

    Row (
        modifier = modifier
            .fillMaxWidth()
    ) {
        ExerciseField("Type", type, {type = it}, true, Modifier.weight(1f))
        ExerciseField("Weight", weight, {weight = it}, true, Modifier.weight(1f))
        ExerciseField("Reps", reps, {reps = it}, true, Modifier.weight(1f))
        ExerciseField("Sets", sets, {sets = it}, true, Modifier.weight(1f))

        AddPlusButton(
            onClick = {
                onAdd(Exercise(type, weight, reps, sets))

                type = ""
                weight = ""
                reps = ""
                sets = ""
            },
            modifier = Modifier
                .padding(2.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun ExerciseWidget(exercise: Exercise, modifier: Modifier = Modifier)
{
    Row(
        modifier = modifier
            .fillMaxWidth()
    ) {
        println(exercise.type)
        ExerciseField("Type", exercise.type, {exercise.type = it}, false, Modifier.weight(1f))
        ExerciseField("Weight", exercise.weight, {exercise.weight = it}, false, Modifier.weight(1f))
        ExerciseField("Reps", exercise.reps, {exercise.reps = it}, false, Modifier.weight(1f))
        ExerciseField("Sets", exercise.sets, {exercise.sets = it}, false, Modifier.weight(1f))
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




