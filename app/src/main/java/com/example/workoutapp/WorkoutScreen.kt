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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    fun findWeightSuggestion(exercise: Exercise): Int {

        val reps = exercise.reps.toIntOrNull() ?: 0
        val weight = exercise.weight.toIntOrNull() ?: 0

        return when {
            reps > 12 -> weight + 5
            reps < 8 -> weight - 5
            else -> weight
        }
    }

    fun logExercise(workoutViewModel: WorkoutViewModel, exercise: Exercise, navController: NavController) {
        val setsLeft = (exercise.sets.toIntOrNull() ?: 0) - 1
        exercise.sets = setsLeft.toString()
        workoutViewModel.logExercise(exercise)

        completedList.add(exercise.copyExercise())

        exercise.sets.toIntOrNull()?.let {
            if (it <= 0) {
                uncompletedList.remove(exercise)
            }
        }
        checkMaxExercise(workoutViewModel)

        if (uncompletedList.isEmpty()) {
            //finalize workout
            navController.navigate("home")
        }
    }

    fun checkMaxExercise(workoutViewModel: WorkoutViewModel){
        val grouped = completedList.groupBy { it.type }

        for ((type, exercisesOfType) in grouped) {
            val maxWeight = exercisesOfType.maxOfOrNull { it.weight.toIntOrNull() ?: 0 }
            val maxReps = exercisesOfType.maxOfOrNull { it.reps.toIntOrNull() ?: 0 }

            val globalExercise =
                workoutViewModel.workoutList.find { it == getWk() }?.exerciseList?.find { it.type == type }

            val globalWeight = globalExercise?.weight?.toIntOrNull() ?: 0
            val globalReps = globalExercise?.reps?.toIntOrNull() ?: 0

            if (globalExercise != null) {
                if (maxWeight != null && maxWeight > globalWeight) {
                    globalExercise.weight = maxWeight.toString()
                }

                if (maxReps != null && maxReps > globalReps) {
                    globalExercise.reps = maxReps.toString()
                }
            }
        }

        workoutViewModel.persistState()
    }
}

class TimerViewModel : ViewModel() {
    private val _timer = MutableStateFlow(0L)
    val timer = _timer.asStateFlow()

    private var timerJob: Job? = null

    fun startTimer(value:Long) {
        timerJob?.cancel()
        _timer.value = value
        timerJob = viewModelScope.launch {
            while (timer.value > 0) {
                delay(1000)
                _timer.value--
            }
            timerJob?.cancel()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

@Composable
fun TimerScreenContent(timerViewModel: TimerViewModel) {
    val timerValue by timerViewModel.timer.collectAsState()

    TimerScreen(
        timerValue = timerValue
    )
}

@Composable
fun TimerScreen(
    timerValue: Long
) {
    Text(text = timerValue.formatTime())
}

fun Long.formatTime(): String {
    val remainingSeconds = this % 60
    return String.format(":%02d", remainingSeconds)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(workoutViewModel:WorkoutViewModel, workoutScreenViewModel: WorkoutScreenViewModel, navController: NavController) {
    val timerViewModel: TimerViewModel = viewModel()
    Scaffold (
        topBar = {
            TopAppBar(
                title = {
                        //timer
                    Row (
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TimerScreenContent(timerViewModel)
                        Spacer(modifier = Modifier.weight(0.76f))

                        Text(workoutScreenViewModel.getWk().workoutName)
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            )
        },
    ) {
        paddingValues ->

        LazyColumn (
            modifier = Modifier.fillMaxSize(),
            contentPadding = paddingValues,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(workoutScreenViewModel.uncompletedList, key = {it.hashCode()}) {exercise ->
                WorkoutExerciseWidget(timerViewModel, workoutViewModel, workoutScreenViewModel, navController, exercise)
            }
        }
    }
}

@Composable
fun WorkoutExerciseWidget(timerViewModel: TimerViewModel, workoutViewModel: WorkoutViewModel, workoutScreenViewModel: WorkoutScreenViewModel, navController: NavController, exercise: Exercise, modifier: Modifier = Modifier)
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
                timerViewModel.startTimer(60)
                workoutScreenViewModel.logExercise(workoutViewModel, exercise, navController)
            },
            Modifier.weight(0.5f)
        ) {
            Icon(Icons.Default.Check, contentDescription = "Complete")
        }
        ExerciseField("Type", exercise.type, {}, false,
            Modifier.weight(1f).fillMaxWidth())
        SuggestionStepper("Weight", weightValue, {returnV -> workoutScreenViewModel.updateWeight(exercise, returnV)}, workoutScreenViewModel.findWeightSuggestion(exercise), 5,
            Modifier.weight(1f).fillMaxWidth())
        Stepper("Reps", repsValue, {returnV -> workoutScreenViewModel.updateReps(exercise, returnV)}, 1,
            Modifier.weight(1f).fillMaxWidth())
        ExerciseField("Sets left", exercise.sets, {newValue -> workoutScreenViewModel.updateSets(exercise, newValue)}, false,
            Modifier.weight(1f).fillMaxWidth())
    }
}


@Composable
fun SuggestionStepper(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    suggestion: Int,
    step: Int = 1,
    modifier: Modifier = Modifier
) {
    var containerHeight by remember { mutableStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { coords ->
                containerHeight = coords.size.height   // height in px
            }

    ) {
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

        val offsetDp = with(LocalDensity.current) { containerHeight.toDp() }

        AssistChip(
            onClick = {},
            label = { Text("$suggestion") },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = offsetDp/2),
            colors = SuggestionChipDefaults.suggestionChipColors(
                containerColor=Color.Green,
                labelColor = Color.Black
            )
        )
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
