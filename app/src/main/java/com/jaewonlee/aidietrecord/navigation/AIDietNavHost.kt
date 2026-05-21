package com.jaewonlee.aidietrecord.navigation

import androidx.compose.runtime.Composable
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
    val mealDatabase = remember(context.applicationContext) {
        MealDatabase.getDatabase(context.applicationContext)
    }
    val mealRepository = remember(mealDatabase) {
        MealRepository(mealDatabase)
    }
    val mealReviewRepository = remember {
        MealReviewRepository()
    }
    val authRepository = remember(mealDatabase) {
        AuthRepository(mealDatabase.authDao())
    }

    var currentUserId by rememberSaveable { mutableStateOf(0L) }
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

    val setCurrentUser: (UserAccount?) -> Unit = { userAccount ->
        currentUserId = userAccount?.id ?: 0L
        currentUserLoginId = userAccount?.userId.orEmpty()
        currentUserNickname = userAccount?.nickname.orEmpty()
        currentUserPasswordHash = userAccount?.passwordHash.orEmpty()
        currentUserCreatedAt = userAccount?.createdAt ?: 0L
    }

    NavHost(
        navController = navController,
        startDestination = Route.Login.path
    ) {
        composable(Route.Login.path) {
            LoginScreen(
                errorMessage = authErrorMessage,
                onLoginClick = { loginId, loginPassword ->
                    coroutineScope.launch {
                        val userAccount = authRepository.login(loginId, loginPassword)
                        if (userAccount == null) {
                            authErrorMessage = "아이디 또는 비밀번호가 올바르지 않습니다."
                        } else {
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
                                authErrorMessage = throwable.message ?: "회원가입에 실패했습니다."
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
                onBackClick = { navController.navigateUp() },
                onSaveClick = { mealUploadDraft ->
                    coroutineScope.launch {
                        val reviewedMeal = mealReviewRepository.reviewMealDraft(
                            mealUploadDraft.copy(ownerId = currentUserId)
                        )
                        mealRepository.addMealRecord(reviewedMeal)
                        navController.navigate(Route.MealList.path) {
                            popUpTo(Route.Home.path)
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
                onEditClick = { navController.navigate(Route.EditMeal.createPath(mealId)) }
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
                onBackClick = { navController.navigateUp() },
                onSaveClick = { mealUploadDraft ->
                    coroutineScope.launch {
                        val reviewedMeal = mealReviewRepository.reviewMealDraft(
                            mealUploadDraft.copy(ownerId = currentUserId)
                        )
                        mealRepository.updateMealRecord(reviewedMeal)
                        navController.navigateUp()
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
                                    setCurrentUser(updatedUser)
                                    nickname = updatedUser.nickname
                                    userId = updatedUser.userId
                                    password = ""
                                    profileErrorMessage = null
                                    navController.navigateUp()
                                }
                                .onFailure { throwable ->
                                    profileErrorMessage = throwable.message ?: "프로필 저장에 실패했습니다."
                                }
                        }
                    }
                },
                onLogoutClick = {
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
        userId.isBlank() -> "아이디를 입력해 주세요."
        nickname.isBlank() -> "닉네임을 입력해 주세요."
        password.isNotBlank() && password.length < 4 -> "새 비밀번호는 4자 이상 입력해 주세요."
        else -> null
    }
}
