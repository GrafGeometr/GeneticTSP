package org.example.experiments

import org.example.model.Tour
import org.example.model.Vertex
import java.io.File
import java.io.PrintWriter

class MetricsCollector<V : Vertex>(filePath: String) : MetricsCallback<V> {
    private val writer = PrintWriter(File(filePath))

    init {
        writer.println("generation,best,average,unique_edges")
    }

    override fun onGeneration(
        generation: Int,
        population: List<Tour<V>>,
        fitnesses: List<Double>,
        bestFitness: Double,
        averageFitness: Double
    ) {
        val uniqueEdges = DiversityUtils.edgeDiversity(population)
        writer.println("$generation,$bestFitness,$averageFitness,$uniqueEdges")
    }

    fun close() {
        writer.close()
    }
}