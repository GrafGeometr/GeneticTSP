package org.example.load

import org.example.model.PlaneGraph
import org.example.model.Point
import java.io.File

object GraphLoader {
    fun loadPoints(filePath: String): PlaneGraph {
        val file = File(filePath)
        val lines = file.readLines().map { it.trim() }.filter { it.isNotEmpty() }
        return parsePoints(lines)
    }

    fun loadPointsFromResource(resourceName: String): PlaneGraph {
        val stream = object {}.javaClass.getResourceAsStream("/$resourceName")
            ?: throw IllegalArgumentException("Resource not found: $resourceName")
        val lines = stream.bufferedReader().readLines().map { it.trim() }.filter { it.isNotEmpty() }
        return parsePoints(lines)
    }

    private fun parsePoints(lines: List<String>): PlaneGraph {
        val totalPoints = lines.first().toInt()
        val pointLines = lines.drop(1).take(totalPoints)
        return PlaneGraph(pointLines.mapIndexed { id, line ->
            val parts = line.split("\\s+".toRegex())
            Point(parts[0].toDouble(), parts[1].toDouble(), id)
        })
    }
}