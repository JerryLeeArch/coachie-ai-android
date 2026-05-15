package com.jaewonlee.aidietrecord.navigation

sealed class Route(val path: String) {
    data object Home : Route("home")
    data object AddMeal : Route("addMeal")
    data object MealList : Route("mealList")
    data object Profile : Route("profile")
    data object GoalSettings : Route("goalSettings")
    data object RecentStats : Route("recentStats")
    data object MealDetail : Route("mealDetail/{mealId}") {
        fun createPath(mealId: Long): String = "mealDetail/$mealId"
    }
}
