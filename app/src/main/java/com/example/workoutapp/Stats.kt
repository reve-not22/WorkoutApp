package com.example.workoutapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.jetbrains.letsPlot.Figure
import org.jetbrains.letsPlot.compose.PlotPanel
import org.jetbrains.letsPlot.core.plot.base.DataFrame
import org.jetbrains.letsPlot.geom.geomDensity
import org.jetbrains.letsPlot.geom.geomLine
import org.jetbrains.letsPlot.geom.geomPoint
import org.jetbrains.letsPlot.letsPlot
import kotlin.collections.map
import kotlin.to

@Composable
fun StatsScreen(navController: NavController, workoutViewModel: WorkoutViewModel) {
    Scaffold (

    ) {
        val figure = createFigure(workoutViewModel)

        Column(
            modifier = Modifier.fillMaxSize().padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 10.dp),
        ) {

            PlotPanel(
                figure = figure,
                modifier = Modifier.fillMaxSize()
            ) { computationMessages ->
                computationMessages.forEach { println("[DEMO APP MESSAGE] $it") }
            }
        }
    }
}

private fun createFigure(workoutViewModel: WorkoutViewModel):Figure {
    val dataPoints =
        workoutViewModel.loggedExercises
            .groupBy { exercise ->
                Pair(exercise.date, exercise.type)
            }.map { (key, exercises) ->
                LoggedExercise(
                    date = key.first,
                    type = key.second,
                    weight = exercises.maxOf { it.weight }
                )
            }.sortedBy{it.date}

    val data: Map<String, List<*>> = mapOf(
        "date" to dataPoints.map { it.date.toString() },
        "type" to dataPoints.map { it.type },
        "weight" to dataPoints.map { it.weight }
    )

    return letsPlot(data) {
        x = "date"; y = "weight"; color = "type"
    } + geomPoint(size = 4.0) + geomLine()
}