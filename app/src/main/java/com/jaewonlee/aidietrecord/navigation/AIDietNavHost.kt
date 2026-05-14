package com.jaewonlee.aidietrecord.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jaewonlee.aidietrecord.ui.screen.AddMealScreen
import com.jaewonlee.aidietrecord.ui.screen.HomeScreen
import com.jaewonlee.aidietrecord.ui.screen.MealDetailScreen
import com.jaewonlee.aidietrecord.ui.screen.MealListScreen
import com.jaewonlee.aidietrecord.ui.screen.ProfileScreen

@Composable
fun AIDietNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Route.Home.path
    ) {
        composable(Route.Home.path) {
            HomeScreen(
                onAddMealClick = { navController.navigate(Route.AddMeal.path) },
                onMealListClick = { navController.navigate(Route.MealList.path) },
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
            ProfileScreen(onBackClick = { navController.navigateUp() })
        }
    }
}
