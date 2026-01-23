package com.example.workoutapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.workoutapp.ui.theme.WorkoutAppTheme
import com.google.protobuf.InvalidProtocolBufferException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.*

val Context.workoutDataStore: DataStore<WorkoutDataList> by dataStore(
    fileName = "workouts.pb",
    serializer = WorkoutSerializer
)

object WorkoutSerializer : Serializer<WorkoutDataList> {
    override val defaultValue: WorkoutDataList = WorkoutDataList.getDefaultInstance()
    override suspend fun readFrom(input: InputStream): WorkoutDataList =
        try {
            WorkoutDataList.parseFrom(input)
        } catch (e: InvalidProtocolBufferException) {
            defaultValue
        }

    override suspend fun writeTo(
        t: WorkoutDataList,
        output: OutputStream
    ) = t.writeTo(output)
}

class WorkoutRepository(private val workoutStore:DataStore<WorkoutDataList>) {
    val workoutsFlow: Flow<List<Workout>> = workoutStore.data.map { proto ->
        proto.workoutsList.map{it.toDomain()}
    }

    val loggedFlow: Flow<List<LoggedExercise>> = workoutStore.data.map { proto ->
        proto.loggedExercisesList.map{it.toDomain()}
    }

    val workoutMapFlow: Flow<Map<DayOfWeek, Workout>> = workoutStore.data.map { proto ->
        proto.weekMapMap.mapNotNull { (k, v) ->
            DayOfWeek.valueOf(k) to v.toDomain()
        }.toMap()
    }

    suspend fun updateData(wList:List<Workout>, lList:List<LoggedExercise>) {
        workoutStore.updateData { current ->
            current.toBuilder()
                .clearWorkouts()
                .addAllWorkouts(wList.map {it.toProto()})
                .addAllLoggedExercises(lList.map {it.toProto()})
                .build()
        }
    }

    suspend fun putCalKey(key:String, workout:Workout?) {
        if (workout != null) {
            workoutStore.updateData { current ->
                current.toBuilder()
                    .putWeekMap(key, workout.toProto())
                    .build()
            }
        }
        else {
            workoutStore.updateData { current ->
                current.toBuilder()
                    .removeWeekMap(key)
                    .build()
            }
        }
    }
}

class WorkoutViewModel(
    val workoutRepository: WorkoutRepository
) : ViewModel() {
    init {

        viewModelScope.launch {
            try {
                val wList = workoutRepository.workoutsFlow.firstOrNull()
                val lList = workoutRepository.loggedFlow.firstOrNull()

                if (!lList.isNullOrEmpty()) {
                    _loggedExercises.clear()
                    _loggedExercises.addAll(lList)
                }

                if (!wList.isNullOrEmpty()) {
                    _workoutList.clear()
                    _workoutList.addAll(wList)
                }
            }
            catch (e:Exception){
                throw e
            }
        }
    }
    private val _workoutList = mutableStateListOf<Workout>()
    val workoutList: List<Workout> get() = _workoutList

    private val _loggedExercises = mutableStateListOf<LoggedExercise>()

    val loggedExercises: List<LoggedExercise> get() = _loggedExercises

    fun logExercise(exercise: Exercise) {
        _loggedExercises.add(exercise.toLoggedExercise(LocalDate.now()))
    }

    fun addWorkout(workout: Workout) {
        _workoutList.add(workout)
        persistState()
    }

    fun deleteWorkout(workout: Workout) {
        _workoutList.remove(workout)
        persistState()
    }

    fun getWorkout(id: UUID): Workout? = _workoutList.firstOrNull{ it.id == id }

    val workoutByWeek = mutableStateMapOf<DayOfWeek, Workout?>()

    fun putWorkoutWeek(day: DayOfWeek, workout:Workout?) {
        viewModelScope.launch {
            workoutByWeek[day] = workout
            workoutRepository.putCalKey(day.name, workout)
        }
    }

    fun getCalendarMap(): SnapshotStateMap<DayOfWeek, Workout?>
    {
        //update workout by week
        viewModelScope.launch {
            val map = workoutRepository.workoutMapFlow.firstOrNull()

            if (!map.isNullOrEmpty()) {
                workoutByWeek.clear()
                workoutByWeek.putAll(map)
            }
        }

        return workoutByWeek
    }

    override fun onCleared() {
        persistState()
    }

    fun persistState() {
        viewModelScope.launch {
            workoutRepository.updateData(workoutList, _loggedExercises)
        }
    }
}

class WorkoutVMFactory(
    private val workoutRepository: WorkoutRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WorkoutViewModel(workoutRepository) as T
    }
}

class MainActivity : ComponentActivity() {

    private val workoutViewModel:WorkoutViewModel by viewModels {
        WorkoutVMFactory(
            WorkoutRepository(this.workoutDataStore)
        )
    }

    //change if you ever use multiple activities
    /*override fun onStop() {
        super.onStop()
        workoutViewModel.persistState()
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorkoutAppTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "login") {
                    composable("login") { LoginScreen(navController) }
                    composable("home") { HomeScreen(navController, workoutViewModel) }
                    composable("stats") { StatsScreen(workoutViewModel) }
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
                            WorkoutScreen(workoutViewModel, workoutScreenViewModel, navController)
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

                        val editVM : EditWorkoutViewModel = viewModel(factory=EditWorkoutViewModelFactory(workout))

                        EditWorkoutScreen(navController, editVM, workoutViewModel)
                    }
                }
            }
        }
    }
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







