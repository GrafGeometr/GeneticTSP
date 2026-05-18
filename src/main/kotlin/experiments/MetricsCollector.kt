package org.example.experiments

import org.example.model.Tour
import org.example.model.Vertex
import java.io.File
import java.io.PrintWriter

class MetricsCollector<V : Vertex>(filePath: String, val loggingRate: Int = 10) : MetricsCallback<V> {
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
        if (generation % loggingRate == 0) {
            val uniqueEdges = DiversityUtils.edgeDiversity(population)
            writer.println("$generation,$bestFitness,$averageFitness,$uniqueEdges")
            writer.flush()
        }
    }

    fun close() {
        writer.close()
    }
}