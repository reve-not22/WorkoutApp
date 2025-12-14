package com.example.workoutapp

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
            LazyVerticalGrid (
                columns = GridCells.Fixed(columns),
                contentPadding = PaddingValues(12.dp),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(workoutViewModel.workoutList) { workout ->
                    WorkoutWidget(workoutViewModel, workout, navController, Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .heightIn(min = 120.dp))
                }
            }
            PlusButton(
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

