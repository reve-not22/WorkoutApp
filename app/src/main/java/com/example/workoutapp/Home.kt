package com.example.workoutapp

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(navController: NavController, workoutViewModel: WorkoutViewModel) {
    Scaffold (modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            FlowRow (
                modifier = Modifier
                    .padding(innerPadding)
            ) {
                for (workout in workoutViewModel.workoutList) {
                    WorkoutWidget(workoutViewModel, workout, navController)
                }
            }
            AddPlusButton(
                onClick = {
                    navController.navigate("workout_add")
                },
                modifier = Modifier
                    .padding(innerPadding)
            )
        }
    }
}

@Composable
fun WorkoutWidget(
    workoutViewModel: WorkoutViewModel,
    workout: Workout,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = {
            val index = workoutViewModel.workoutList.indexOf(workout)
            if (index >= 0) {
                navController.navigate("workout_start/$index")
            }},
        modifier = Modifier.padding(4.dp)
    ) {
        Text(workout.workoutName)
    }
}

