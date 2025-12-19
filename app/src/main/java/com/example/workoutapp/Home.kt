package com.example.workoutapp

import android.R.color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import io.github.boguszpawlowski.composecalendar.SelectableWeekCalendar
import io.github.boguszpawlowski.composecalendar.day.DefaultDay
import io.github.boguszpawlowski.composecalendar.rememberSelectableWeekCalendarState
import java.time.LocalDate
import java.time.format.TextStyle.FULL
import java.time.format.TextStyle.SHORT
import java.util.Locale


@Composable
fun WorkoutCalendar(
    onDayClicked: (LocalDate) -> Unit,
    workoutViewModel: WorkoutViewModel,
    modifier:Modifier = Modifier
) {
    SelectableWeekCalendar(
        calendarState = rememberSelectableWeekCalendarState(),

        daysOfWeekHeader = { daysOfWeek ->
            Row{
                daysOfWeek.forEach { dayOfWeek ->
                    Text(
                        textAlign = TextAlign.Center,
                        text = dayOfWeek.getDisplayName(SHORT, Locale.getDefault()),
                        modifier = Modifier
                            .weight(1f)
                            .wrapContentHeight(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },

        weekHeader = { weekState ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.testTag("WeekLabel"),
                    text = weekState.currentWeek.yearMonth.month
                        .getDisplayName(FULL, Locale.getDefault())
                        .lowercase()
                        .replaceFirstChar { it.titlecase() },
                    style = MaterialTheme.typography.headlineLarge,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = weekState.currentWeek.yearMonth.year.toString(),
                    style = MaterialTheme.typography.headlineLarge
                )
            }
        },

        dayContent = { dayState ->
            val workout = workoutViewModel.getCalendarMap()[dayState.date.dayOfWeek]
            val isToday = dayState.date == LocalDate.now()

            val boxColor = when {
                isToday -> Color.Red
                else -> MaterialTheme.colorScheme.background
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(boxColor),
                contentAlignment = Alignment.Center
            ) {
                DefaultDay (
                    state = dayState,
                    onClick= {
                        onDayClicked(dayState.date)
                    }
                )
                if (workout != null) {
                    Box (
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = workout.workoutName,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalWorkoutPickerDialog(
    date:LocalDate,
    workoutViewModel: WorkoutViewModel,
    onDismiss: () -> Unit,
    onWorkoutSelected: (Workout?) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {Text("Assign workout")},
        text = {
            Column{
                Text("Select workout for ${date.dayOfWeek}")
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = {expanded = false}
                ) {
                    DropdownMenuItem(
                        text = {Text("None")},
                        onClick = {
                            onWorkoutSelected(null)
                            onDismiss()
                        }
                    )
                    workoutViewModel.workoutList.forEach { workout ->
                        DropdownMenuItem(
                            text= {Text(workout.workoutName)},
                            onClick = {
                                onWorkoutSelected(workout)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {}
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(navController: NavController, workoutViewModel: WorkoutViewModel) {
    Scaffold (
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("workout_add")
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add workout")
            }
        }
    ) { innerPadding ->

        val count = workoutViewModel.workoutList.size
        val columns = when {
            count <= 1 -> 1
            count <= 2 -> 2
            count <= 4 -> 2
            else -> 3
        }

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            var selectedDate by remember {mutableStateOf<LocalDate?>(null)}
            val openAlertDialog = remember { mutableStateOf(false) }

            WorkoutCalendar(
                onDayClicked = {clickedDate ->
                    selectedDate = clickedDate
                    openAlertDialog.value = true
                },
                workoutViewModel,
                modifier = Modifier.weight(1f)
            )

            selectedDate?.let {

                when {
                    openAlertDialog.value -> {
                        CalWorkoutPickerDialog(
                            date = it,
                            workoutViewModel = workoutViewModel,
                            onDismiss = {
                                selectedDate = null
                                openAlertDialog.value = false
                            },
                            onWorkoutSelected = { workout ->
                                selectedDate?.let { date ->
                                    workoutViewModel.putWorkoutWeek(it.dayOfWeek, workout)
                                }
                            }
                        )
                    }
                }


            }
            LazyVerticalGrid (
                columns = GridCells.Fixed(columns),
                contentPadding = PaddingValues(12.dp),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(workoutViewModel.workoutList) { workout ->
                    WorkoutWidget(workout, navController, Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .heightIn(min = 120.dp))
                }
            }
        }
    }
}

@Composable
fun WorkoutWidget(
    workout: Workout,
    navController: NavController,
    modifier:Modifier = Modifier
) {
    Card(
        modifier = modifier
            .combinedClickable (
                onClick = {
                    navController.navigate("workout_start/${workout.id}")
                },
                onLongClick = {
                    navController.navigate("workout_edit/${workout.id}")
                }
            ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box (
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {Text(workout.workoutName)}
    }
}

