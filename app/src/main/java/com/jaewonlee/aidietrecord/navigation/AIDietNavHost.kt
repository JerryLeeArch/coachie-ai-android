package com.jaewonlee.aidietrecord.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jaewonlee.aidietrecord.ui.screen.AddMealScreen
import com.jaewonlee.aidietrecord.ui.screen.GoalSettingsScreen
import com.jaewonlee.aidietrecord.ui.screen.HomeScreen
import com.jaewonlee.aidietrecord.ui.screen.MealDetailScreen
import com.jaewonlee.aidietrecord.ui.screen.MealListScreen
import com.jaewonlee.aidietrecord.ui.screen.ProfileScreen
import com.jaewonlee.aidietrecord.ui.screen.RecentStatsScreen

@Composable
fun AIDietNavHost() {
    val navController = rememberNavController()
    var nickname by rememberSaveable { mutableStateOf("Jerry") }
    var userId by rememberSaveable { mutableStateOf("jerrylee") }
    var password by rememberSaveable { mutableStateOf("") }
    var currentWeight by rememberSaveable { mutableStateOf("72.0") }
    var targetWeight by rememberSaveable { mutableStateOf("69.0") }
    var targetWeeks by rememberSaveable { mutableStateOf("8") }
    var targetCalories by rememberSaveable { mutableStateOf("1900") }
    var proteinGoal by rememberSaveable { mutableStateOf("110") }

    NavHost(
        navController = navController,
        startDestination = Route.Home.path
    ) {
        composable(Route.Home.path) {
            HomeScreen(
                nickname = nickname,
                targetCalories = targetCalories.toPositiveIntOrDefault(2000),
                targetProteinGram = proteinGoal.toPositiveIntOrDefault(100),
                onAddMealClick = { navController.navigate(Route.AddMeal.path) },
                onMealListClick = { navController.navigate(Route.MealList.path) },
                onGoalSettingsClick = { navController.navigate(Route.GoalSettings.path) },
                onRecentStatsClick = { navController.navigate(Route.RecentStats.path) },
                onProfileClick = { navController.navigate(Route.Profile.path) }
            )
        }

        composable(Route.AddMeal.path) {
            AddMealScreen(
                onBackClick = { navController.navigateUp() },
                onSaveClick = {
                    navController.navigate(Route.MealList.path) {
                        popUpTo(Route.Home.path)
                    }
                }
            )
        }

        composable(Route.MealList.path) {
            MealListScreen(
                onBackClick = { navController.navigateUp() },
                onMealClick = { mealId -> navController.navigate(Route.MealDetail.createPath(mealId)) }
            )
        }

        composable(
            route = Route.MealDetail.path,
            arguments = listOf(navArgument("mealId") { type = NavType.LongType })
        ) { backStackEntry ->
            val mealId = backStackEntry.arguments?.getLong("mealId") ?: 1L
            MealDetailScreen(
                mealId = mealId,
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(Route.Profile.path) {
            ProfileScreen(
                nickname = nickname,
                onNicknameChange = { nickname = it },
                userId = userId,
                onUserIdChange = { userId = it },
                password = password,
                onPasswordChange = { password = it },
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(Route.GoalSettings.path) {
            GoalSettingsScreen(
                currentWeight = currentWeight,
                onCurrentWeightChange = { currentWeight = it },
                targetWeight = targetWeight,
                onTargetWeightChange = { targetWeight = it },
                targetWeeks = targetWeeks,
                onTargetWeeksChange = { targetWeeks = it },
                targetCalories = targetCalories,
                onTargetCaloriesChange = { targetCalories = it },
                proteinGoal = proteinGoal,
                onProteinGoalChange = { proteinGoal = it },
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(Route.RecentStats.path) {
            RecentStatsScreen(
                targetCalories = targetCalories.toPositiveIntOrDefault(2000),
                targetProteinGram = proteinGoal.toPositiveIntOrDefault(100),
                onBackClick = { navController.navigateUp() }
            )
        }
    }
}

private fun String.toPositiveIntOrDefault(defaultValue: Int): Int {
    return toIntOrNull()?.takeIf { it > 0 } ?: defaultValue
}
