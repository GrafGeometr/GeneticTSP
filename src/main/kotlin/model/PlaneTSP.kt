package org.example.model

import kotlin.math.pow
import kotlin.math.sqrt

class PlaneTSP(override val vertexes: List<Point>) : TSP<Point> {
    override fun distance(v: Point, u: Point): Double {
        val dx = v.x - u.x
        val dy = v.y - u.y
        return sqrt(dx * dx + dy * dy)
    }

    override fun toString() = "PlaneGraph(${vertexes})"
}