package org.example.load

import org.example.model.PlaneTSP
import org.example.model.Point
import java.io.File

object GraphLoader {
    fun loadPoints(filePath: String): PlaneTSP {
        val file = File(filePath)
        val lines = file.readLines().map { it.trim() }.filter { it.isNotEmpty() }
        return parsePoints(lines)
    }

    fun loadPointsFromResource(resourceName: String): PlaneTSP {
        val stream = object {}.javaClass.getResourceAsStream("/$resourceName")
            ?: throw IllegalArgumentException("Resource not found: $resourceName")
        val lines = stream.bufferedReader().readLines().map { it.trim() }.filter { it.isNotEmpty() }
        return parsePoints(lines)
    }

    private fun parsePoints(lines: List<String>): PlaneTSP {
        val totalPoints = lines.first().toInt()
        val pointLines = lines.drop(1).take(totalPoints)
        return PlaneTSP(pointLines.mapIndexed { id, line ->
            val parts = line.split("\\s+".toRegex())
            Point(parts[0].toDouble(), parts[1].toDouble(), id)
        })
    }
}