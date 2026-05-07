package org.example.model

import kotlin.math.pow
import kotlin.math.sqrt

class PlaneGraph(override val vertexes: List<Point>) : Graph<Point> {
    override fun distance(v: Point, u: Point): Double = sqrt((v.x - u.x).pow(2) + (v.y - u.y).pow(2))

    override fun toString() = "PlaneGraph(${vertexes})"
}