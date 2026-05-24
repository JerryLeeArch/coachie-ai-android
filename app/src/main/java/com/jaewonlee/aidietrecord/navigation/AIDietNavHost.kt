package com.jaewonlee.aidietrecord.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jaewonlee.aidietrecord.data.ai.GeminiMealAnalyzer
import com.jaewonlee.aidietrecord.data.local.AuthSessionStore
import com.jaewonlee.aidietrecord.data.local.MealDatabase
import com.jaewonlee.aidietrecord.data.model.UserAccount
import com.jaewonlee.aidietrecord.data.repository.AuthRepository
import com.jaewonlee.aidietrecord.data.repository.MealReviewRepository
import com.jaewonlee.aidietrecord.data.repository.MealRepository
import com.jaewonlee.aidietrecord.ui.screen.AddMealScreen
import com.jaewonlee.aidietrecord.ui.screen.GoalSettingsScreen
import com.jaewonlee.aidietrecord.ui.screen.HomeScreen
import com.jaewonlee.aidietrecord.ui.screen.LoginScreen
import com.jaewonlee.aidietrecord.ui.screen.MealDetailScreen
import com.jaewonlee.aidietrecord.ui.screen.MealListScreen
import com.jaewonlee.aidietrecord.ui.screen.ProfileScreen
import com.jaewonlee.aidietrecord.ui.screen.RecentStatsScreen
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@Composable
fun AIDietNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val authSessionStore = remember(context.applicationContext) {
        AuthSessionStore(context.applicationContext)
    }
    val mealDatabase = remember(context.applicationContext) {
        MealDatabase.getDatabase(context.applicationContext)
    }
    val mealRepository = remember(mealDatabase) {
        MealRepository(mealDatabase)
    }
    val mealReviewRepository = remember(context.applicationContext) {
        MealReviewRepository(
            geminiMealAnalyzer = GeminiMealAnalyzer(context.applicationContext)
        )
    }
    val authRepository = remember(mealDatabase) {
        AuthRepository(mealDatabase.authDao())
    }

    var currentUserId by rememberSaveable {
        mutableStateOf(authSessionStore.getSavedUserId())
    }
    var currentUserLoginId by rememberSaveable { mutableStateOf("") }
    var currentUserNickname by rememberSaveable { mutableStateOf("") }
    var currentUserPasswordHash by rememberSaveable { mutableStateOf("") }
    var currentUserCreatedAt by rememberSaveable { mutableStateOf(0L) }
    val currentUser = currentUserOrNull(
        id = currentUserId,
        userId = currentUserLoginId,
        nickname = currentUserNickname,
        passwordHash = currentUserPasswordHash,
        createdAt = currentUserCreatedAt
    )

    val mealRecordsFlow = remember(mealRepository, currentUserId) {
        if (currentUserId > 0) {
            mealRepository.observeMealRecords(currentUserId)
        } else {
            flowOf(emptyList())
        }
    }
    val mealRecords by mealRecordsFlow.collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    var nickname by rememberSaveable { mutableStateOf("") }
    var userId by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var authErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var profileErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var currentWeight by rememberSaveable { mutableStateOf("72.0") }
    var targetWeight by rememberSaveable { mutableStateOf("69.0") }
    var targetWeeks by rememberSaveable { mutableStateOf("8") }
    var targetCalories by rememberSaveable { mutableStateOf("1900") }
    var proteinGoal by rememberSaveable { mutableStateOf("110") }
    var mealSaveInProgress by rememberSaveable { mutableStateOf(false) }
    var authRestoreCompleted by rememberSaveable {
        mutableStateOf(authSessionStore.getSavedUserId() <= 0L)
    }

    val setCurrentUser: (UserAccount?) -> Unit = { userAccount ->
        currentUserId = userAccount?.id ?: 0L
        currentUserLoginId = userAccount?.userId.orEmpty()
        currentUserNickname = userAccount?.nickname.orEmpty()
        currentUserPasswordHash = userAccount?.passwordHash.orEmpty()
        currentUserCreatedAt = userAccount?.createdAt ?: 0L
    }

    LaunchedEffect(Unit) {
        val savedUserId = authSessionStore.getSavedUserId()
        if (savedUserId > 0L) {
            val savedUser = authRepository.getUserById(savedUserId)
            if (savedUser == null) {
                authSessionStore.clear()
                setCurrentUser(null)
                nickname = ""
                userId = ""
                password = ""
            } else {
                setCurrentUser(savedUser)
                nickname = savedUser.nickname
                userId = savedUser.userId
                password = ""
                authErrorMessage = null
            }
        }
        authRestoreCompleted = true
    }

    if (!authRestoreCompleted) {
        return
    }

    NavHost(
        navController = navController,
        startDestination = if (currentUserId > 0L) Route.Home.path else Route.Login.path
    ) {
        composable(Route.Login.path) {
            LoginScreen(
                errorMessage = authErrorMessage,
                onLoginClick = { loginId, loginPassword ->
                    coroutineScope.launch {
                        val userAccount = authRepository.login(loginId, loginPassword)
                        if (userAccount == null) {
                            authErrorMessage = "The login ID or password is incorrect."
                        } else {
                            authSessionStore.saveUserId(userAccount.id)
                            setCurrentUser(userAccount)
                            nickname = userAccount.nickname
                            userId = userAccount.userId
                            password = ""
                            authErrorMessage = null
                            navController.navigate(Route.Home.path) {
                                popUpTo(Route.Login.path) { inclusive = true }
                            }
                        }
                    }
                },
                onRegisterClick = { loginId, registerNickname, loginPassword ->
                    coroutineScope.launch {
                        authRepository
                            .register(
                                userId = loginId,
                                nickname = registerNickname,
                                password = loginPassword
                            )
                            .onSuccess { userAccount ->
                                authSessionStore.saveUserId(userAccount.id)
                                setCurrentUser(userAccount)
                                nickname = userAccount.nickname
                                userId = userAccount.userId
                                password = ""
                                authErrorMessage = null
                                navController.navigate(Route.Home.path) {
                                    popUpTo(Route.Login.path) { inclusive = true }
                                }
                            }
                            .onFailure { throwable ->
                                authErrorMessage = throwable.message ?: "Registration failed."
                            }
                    }
                }
            )
        }

        composable(Route.Home.path) {
            HomeScreen(
                nickname = nickname,
                mealRecords = mealRecords,
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
                initialMeal = null,
                isSaving = mealSaveInProgress,
                onBackClick = { navController.navigateUp() },
                onSaveClick = { mealUploadDraft ->
                    if (!mealSaveInProgress) {
                        mealSaveInProgress = true
                        coroutineScope.launch {
                            try {
                                val reviewedMeal = mealReviewRepository.reviewMealDraft(
                                    mealUploadDraft.copy(ownerId = currentUserId)
                                )
                                mealRepository.addMealRecord(reviewedMeal)
                                navController.navigate(Route.MealList.path) {
                                    popUpTo(Route.Home.path)
                                }
                            } finally {
                                mealSaveInProgress = false
                            }
                        }
                    }
                }
            )
        }

        composable(Route.MealList.path) {
            MealListScreen(
                mealRecords = mealRecords,
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
                mealRecord = mealRecords.firstOrNull { it.id == mealId },
                onBackClick = { navController.navigateUp() },
                onEditClick = { navController.navigate(Route.EditMeal.createPath(mealId)) },
                onDeleteClick = { mealRecord ->
                    coroutineScope.launch {
                        mealRepository.deleteMealRecord(mealRecord)
                        val returnedToList = navController.popBackStack(
                            route = Route.MealList.path,
                            inclusive = false
                        )
                        if (!returnedToList) {
                            navController.navigate(Route.MealList.path) {
                                popUpTo(Route.Home.path)
                            }
                        }
                    }
                }
            )
        }

        composable(
            route = Route.EditMeal.path,
            arguments = listOf(navArgument("mealId") { type = NavType.LongType })
        ) { backStackEntry ->
            val mealId = backStackEntry.arguments?.getLong("mealId") ?: 1L
            val mealRecord = mealRecords.firstOrNull { it.id == mealId }

            AddMealScreen(
                initialMeal = mealRecord,
                isSaving = mealSaveInProgress,
                onBackClick = { navController.navigateUp() },
                onSaveClick = { mealUploadDraft ->
                    if (!mealSaveInProgress) {
                        mealSaveInProgress = true
                        coroutineScope.launch {
                            try {
                                val reviewedMeal = mealReviewRepository.reviewMealDraft(
                                    mealUploadDraft.copy(ownerId = currentUserId)
                                )
                                mealRepository.updateMealRecord(reviewedMeal)
                                navController.navigateUp()
                            } finally {
                                mealSaveInProgress = false
                            }
                        }
                    }
                }
            )
        }

        composable(Route.Profile.path) {
            ProfileScreen(
                nickname = nickname,
                onNicknameChange = {
                    nickname = it
                    profileErrorMessage = null
                },
                userId = userId,
                onUserIdChange = {
                    userId = it
                    profileErrorMessage = null
                },
                password = password,
                onPasswordChange = {
                    password = it
                    profileErrorMessage = null
                },
                errorMessage = profileErrorMessage,
                onBackClick = { navController.navigateUp() },
                onSaveClick = {
                    val userAccount = currentUser
                    profileErrorMessage = validateProfileInput(userId, nickname, password)
                    if (userAccount != null && profileErrorMessage == null) {
                        coroutineScope.launch {
                            authRepository
                                .updateProfile(
                                    currentUser = userAccount,
                                    userId = userId,
                                    nickname = nickname,
                                    newPassword = password
                                )
                                .onSuccess { updatedUser ->
                                    authSessionStore.saveUserId(updatedUser.id)
                                    setCurrentUser(updatedUser)
                                    nickname = updatedUser.nickname
                                    userId = updatedUser.userId
                                    password = ""
                                    profileErrorMessage = null
                                    navController.navigateUp()
                                }
                                .onFailure { throwable ->
                                    profileErrorMessage = throwable.message ?: "Profile update failed."
                                }
                        }
                    }
                },
                onLogoutClick = {
                    authSessionStore.clear()
                    setCurrentUser(null)
                    nickname = ""
                    userId = ""
                    password = ""
                    profileErrorMessage = null
                    authErrorMessage = null
                    navController.navigate(Route.Login.path) {
                        popUpTo(Route.Home.path) { inclusive = true }
                    }
                }
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
                mealRecords = mealRecords,
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

private fun currentUserOrNull(
    id: Long,
    userId: String,
    nickname: String,
    passwordHash: String,
    createdAt: Long
): UserAccount? {
    if (id <= 0) return null
    return UserAccount(
        id = id,
        userId = userId,
        nickname = nickname,
        passwordHash = passwordHash,
        createdAt = createdAt
    )
}

private fun validateProfileInput(
    userId: String,
    nickname: String,
    password: String
): String? {
    return when {
        userId.isBlank() -> "Enter a login ID."
        nickname.isBlank() -> "Enter a nickname."
        password.isNotBlank() && password.length < 4 -> "New password must be at least 4 characters."
        else -> null
    }
}
