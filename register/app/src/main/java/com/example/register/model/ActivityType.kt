package com.example.register.model

import com.example.register.R

enum class ActivityType(
    val key: String,
    val emoji: String,
    val labelRes: Int,
    val descRes: Int,
    val cardBgRes: Int,
    val met: Double,
) {
    RUNNING(
        key = "running",
        emoji = "🏃",
        labelRes = R.string.activity_running,
        descRes = R.string.activity_running_desc,
        cardBgRes = R.drawable.home_card_blue,
        met = 9.8,
    ),
    CYCLING(
        key = "cycling",
        emoji = "🚴",
        labelRes = R.string.activity_cycling,
        descRes = R.string.activity_cycling_desc,
        cardBgRes = R.drawable.home_card_coral,
        met = 7.5,
    ),
    WEIGHTLIFTING(
        key = "weightlifting",
        emoji = "🏋️",
        labelRes = R.string.activity_weightlifting,
        descRes = R.string.activity_weightlifting_desc,
        cardBgRes = R.drawable.home_card_purple,
        met = 6.0,
    ),
    YOGA(
        key = "yoga",
        emoji = "🧘",
        labelRes = R.string.activity_yoga,
        descRes = R.string.activity_yoga_desc,
        cardBgRes = R.drawable.home_category_bg,
        met = 2.5,
    ),
    CARDIO(
        key = "cardio",
        emoji = "🤸",
        labelRes = R.string.activity_cardio,
        descRes = R.string.activity_cardio_desc,
        cardBgRes = R.drawable.home_card_coral,
        met = 7.0,
    ),
    SWIMMING(
        key = "swimming",
        emoji = "🏊",
        labelRes = R.string.activity_swimming,
        descRes = R.string.activity_swimming_desc,
        cardBgRes = R.drawable.home_card_blue,
        met = 8.0,
    ),
    HIKING(
        key = "hiking",
        emoji = "🥾",
        labelRes = R.string.activity_hiking,
        descRes = R.string.activity_hiking_desc,
        cardBgRes = R.drawable.home_card_purple,
        met = 6.0,
    ),
    WALKING(
        key = "walking",
        emoji = "🚶",
        labelRes = R.string.activity_walking,
        descRes = R.string.activity_walking_desc,
        cardBgRes = R.drawable.home_card_coral,
        met = 3.5,
    );

    companion object {
        fun fromKey(key: String?): ActivityType? {
            return entries.find { it.key == key }
        }
    }
}
