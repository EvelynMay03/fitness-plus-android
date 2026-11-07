package com.example.register

object BmiCalculator {

    enum class Category(val labelRes: Int) {
        UNDERWEIGHT(R.string.bmi_underweight),
        NORMAL(R.string.bmi_normal),
        OVERWEIGHT(R.string.bmi_overweight),
        OBESE(R.string.bmi_obese),
    }

    data class Result(
        val bmi: Double,
        val category: Category,
    )

    fun calculate(weightKg: Double, heightCm: Double): Result? {
        if (weightKg <= 0 || heightCm <= 0) return null

        val heightM = heightCm / 100.0
        val bmi = weightKg / (heightM * heightM)
        val category = when {
            bmi < 18.5 -> Category.UNDERWEIGHT
            bmi < 25.0 -> Category.NORMAL
            bmi < 30.0 -> Category.OVERWEIGHT
            else -> Category.OBESE
        }

        return Result(bmi = bmi, category = category)
    }
}
