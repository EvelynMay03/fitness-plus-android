package com.example.register

/**
 * Estimates calories burned using the MET formula:
 * calories = MET x weight(kg) x duration(hours)
 */
object CalorieCalculator {

    fun calculate(met: Double, weightKg: Double, durationMinutes: Int): Int {
        if (met <= 0 || weightKg <= 0 || durationMinutes <= 0) return 0
        val hours = durationMinutes / 60.0
        return (met * weightKg * hours).toInt()
    }
}
