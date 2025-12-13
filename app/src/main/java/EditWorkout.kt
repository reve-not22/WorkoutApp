import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.workoutapp.AddCheckButton
import com.example.workoutapp.AddExerciseWidget
import com.example.workoutapp.AddPlusButton
import com.example.workoutapp.AddWorkoutViewModel
import com.example.workoutapp.Exercise
import com.example.workoutapp.ExerciseWidget
import com.example.workoutapp.ModifyWorkoutViewModel
import com.example.workoutapp.Workout
import com.example.workoutapp.WorkoutViewModel

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

@Composable
fun EditWorkoutScreen(navController: NavController, workout: Workout) {
    val editWorkoutViewModel = EditWorkoutViewModel(workout)

    Scaffold (
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                OutlinedTextField(
                    state = editWorkoutViewModel.workoutNameState,
                    lineLimits = TextFieldLineLimits.SingleLine
                )

                AddCheckButton(
                    onClick = {
                        editWorkoutViewModel.updateWorkout()
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
            AddExerciseWidget(editWorkoutViewModel,
                onAdd = { newExercise ->
                    editWorkoutViewModel.addExercise(newExercise)
                }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(editWorkoutViewModel.exerciseList.size, key = {index -> editWorkoutViewModel.exerciseList[index].hashCode()}) {index ->
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
                            ExerciseWidget(editWorkoutViewModel, exercise)
                        }
                    )
                }
            }
        }
    }
}