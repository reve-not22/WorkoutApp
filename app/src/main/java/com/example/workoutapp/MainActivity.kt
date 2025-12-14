package com.example.workoutapp

import EditWorkoutScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.workoutapp.ui.theme.WorkoutAppTheme
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorkoutAppTheme {
                val navController = rememberNavController()
                val workoutViewModel: WorkoutViewModel = viewModel()
                NavHost(navController = navController, startDestination = "login") {
                    composable("login") { LoginScreen(navController) }
                    composable("home") { HomeScreen(navController, workoutViewModel) }
                    composable("workout_add") { AddWorkoutScreen(navController, workoutViewModel) }
                    composable("workout_start/{workoutId}",
                        arguments = listOf(
                            navArgument("workoutId") {type = NavType.StringType}
                        )
                        ) {
                            backStackEntry ->
                            val workoutId = backStackEntry.arguments
                                ?.getString("workoutId")
                                ?.let {UUID.fromString(it)}

                            val workout = workoutId?.let {workoutViewModel.getWorkout(it)}

                            if (workout == null) {
                                LaunchedEffect(Unit) {
                                    navController.popBackStack()
                                }
                                return@composable
                            }

                            val workoutScreenViewModel: WorkoutScreenViewModel =
                                viewModel(
                                    factory= WorkoutScreenViewModelFactory(workout)
                                )
                            WorkoutScreen(workoutViewModel, workoutScreenViewModel)
                        }
                    composable("workout_edit/{workoutId}",
                        arguments = listOf(
                            navArgument("workoutId") {type = NavType.StringType}
                        )
                    ) {
                            backStackEntry ->
                        val workoutId = backStackEntry.arguments
                            ?.getString("workoutId")
                            ?.let {UUID.fromString(it)}

                        val workout = workoutId?.let {workoutViewModel.getWorkout(it)}

                        if (workout == null) {
                            LaunchedEffect(Unit) {
                                navController.popBackStack()
                            }
                            return@composable
                        }

                        EditWorkoutScreen(navController, workout, workoutViewModel)
                    }
                }
            }
        }
    }
}

class WorkoutViewModel : ViewModel() {

    private val _workoutList = mutableStateListOf<Workout>()
    val workoutList: List<Workout> get() = _workoutList

    fun addWorkout(workout: Workout) {
        _workoutList.add(workout)
    }

    fun deleteWorkout(workout: Workout) {
        _workoutList.remove(workout)
    }

    fun getWorkout(id: UUID): Workout? = _workoutList.firstOrNull{ it.id == id }

}

@Composable
fun PlusButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.primary
) {
    FloatingActionButton(
        onClick = { onClick() },
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor) {
        Icon(Icons.Default.Add, contentDescription = "add")
    }
}

@Composable
fun TrashButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.primary
) {
    FloatingActionButton(
        onClick = { onClick() },
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor) {
        Icon(Icons.Default.Delete, contentDescription = "delete")
    }
}

@Composable
fun CheckButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.primary
) {
    FloatingActionButton(
        onClick = { onClick() },
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor) {
        Icon(Icons.Default.Check, contentDescription = "done")
    }
}







