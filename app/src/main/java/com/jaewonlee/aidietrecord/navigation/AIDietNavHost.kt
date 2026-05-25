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
import com.jaewonlee.aidietrecord.data.model.GoalPlanEntity
import com.jaewonlee.aidietrecord.data.model.MealRecord
import com.jaewonlee.aidietrecord.data.model.MealUploadDraft
import com.jaewonlee.aidietrecord.data.model.UserAccount
import com.jaewonlee.aidietrecord.data.repository.AuthRepository
import com.jaewonlee.aidietrecord.data.repository.GoalRepository
import com.jaewonlee.aidietrecord.data.repository.MealReviewRepository
import com.jaewonlee.aidietrecord.data.repository.MealRepository
import com.jaewonlee.aidietrecord.ui.screen.AddMealScreen
import com.jaewonlee.aidietrecord.ui.screen.GoalSettingsScreen
import com.jaewonlee.aidietrecord.ui.screen.HomeScreen
import com.jaewonlee.aidietrecord.ui.screen.LoginScreen
import com.jaewonlee.aidietrecord.ui.screen.MealAnalysisNoticeUiState
import com.jaewonlee.aidietrecord.ui.screen.MealDetailScreen
import com.jaewonlee.aidietrecord.ui.screen.MealListScreen
import com.jaewonlee.aidietrecord.ui.screen.MealReviewScreen
import com.jaewonlee.aidietrecord.ui.screen.ProfileScreen
import com.jaewonlee.aidietrecord.ui.screen.RecentStatsScreen
import java.time.LocalDate
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
    val goalRepository = remember(mealDatabase) {
        GoalRepository(mealDatabase)
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
    val todayEpochDay = remember { LocalDate.now().toEpochDay() }
    val goalPlansFlow = remember(goalRepository, currentUserId) {
        if (currentUserId > 0) {
            goalRepository.observeGoalPlans(currentUserId)
        } else {
            flowOf(emptyList())
        }
    }
    val activeGoalPlanFlow = remember(goalRepository, currentUserId, todayEpochDay) {
        if (currentUserId > 0) {
            goalRepository.observeActiveGoalPlan(currentUserId, todayEpochDay)
        } else {
            flowOf<GoalPlanEntity?>(null)
        }
    }
    val goalPlans by goalPlansFlow.collectAsState(initial = emptyList())
    val activeGoalPlan by activeGoalPlanFlow.collectAsState(initial = null)
    val bodyMeasurementsFlow = remember(goalRepository, currentUserId) {
        if (currentUserId > 0) {
            goalRepository.observeBodyMeasurements(currentUserId)
        } else {
            flowOf(emptyList())
        }
    }
    val bodyMeasurements by bodyMeasurementsFlow.collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    var nickname by rememberSaveable { mutableStateOf("") }
    var userId by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var authErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var profileErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val defaultGoalStartDate = remember { LocalDate.now().toString() }
    val defaultGoalEndDate = remember { LocalDate.now().plusWeeks(8).minusDays(1).toString() }
    var goalStartDate by rememberSaveable { mutableStateOf(defaultGoalStartDate) }
    var goalEndDate by rememberSaveable { mutableStateOf(defaultGoalEndDate) }
    var goalPeriodMode by rememberSaveable { mutableStateOf("TARGET_DATE") }
    var currentWeight by rememberSaveable { mutableStateOf("72.0") }
    var currentMuscleMass by rememberSaveable { mutableStateOf("") }
    var currentMetabolicRate by rememberSaveable { mutableStateOf("") }
    var currentBodyFatPercent by rememberSaveable { mutableStateOf("") }
    var targetWeight by rememberSaveable { mutableStateOf("69.0") }
    var targetMuscleMass by rememberSaveable { mutableStateOf("") }
    var targetBodyFatPercent by rememberSaveable { mutableStateOf("") }
    var targetWeeks by rememberSaveable { mutableStateOf("8") }
    var targetCalories by rememberSaveable { mutableStateOf("1900") }
    var targetCarbsGram by rememberSaveable { mutableStateOf("220") }
    var proteinGoal by rememberSaveable { mutableStateOf("110") }
    var targetFatGram by rememberSaveable { mutableStateOf("60") }
    var targetFiberGram by rememberSaveable { mutableStateOf("28") }
    var targetSugarGram by rememberSaveable { mutableStateOf("45") }
    var targetSodiumMilligram by rememberSaveable { mutableStateOf("2300") }
    var manualTargetsEnabled by rememberSaveable { mutableStateOf(false) }
    var mealSaveInProgress by rememberSaveable { mutableStateOf(false) }
    var pendingMealReview by remember { mutableStateOf<PendingMealReviewState?>(null) }
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

    LaunchedEffect(activeGoalPlan?.id) {
        activeGoalPlan?.let { goalPlan ->
            goalStartDate = LocalDate.ofEpochDay(goalPlan.validFromEpochDay).toString()
            goalEndDate = LocalDate.ofEpochDay(goalPlan.validToEpochDay - 1L).toString()
            currentWeight = goalPlan.startWeightKg.toGoalNumberText()
            currentMuscleMass = goalPlan.startMuscleMassKg?.toGoalNumberText().orEmpty()
            currentBodyFatPercent = goalPlan.startBodyFatPercent?.toGoalNumberText().orEmpty()
            targetWeight = goalPlan.targetWeightKg?.toGoalNumberText().orEmpty()
            targetMuscleMass = goalPlan.targetMuscleMassKg?.toGoalNumberText().orEmpty()
            targetBodyFatPercent = goalPlan.targetBodyFatPercent?.toGoalNumberText().orEmpty()
            targetWeeks = (((goalPlan.validToEpochDay - goalPlan.validFromEpochDay) + 6L) / 7L)
                .coerceAtLeast(1L)
                .toString()
            targetCalories = goalPlan.dailyCalories.toString()
            targetCarbsGram = goalPlan.dailyCarbsGram.toString()
            proteinGoal = goalPlan.dailyProteinGram.toString()
            targetFatGram = goalPlan.dailyFatGram.toString()
            targetFiberGram = goalPlan.dailyFiberGram.toString()
            targetSugarGram = goalPlan.dailySugarGram.toString()
            targetSodiumMilligram = goalPlan.dailySodiumMilligram.toString()
            manualTargetsEnabled = goalPlan.plannerVersion.startsWith("manual")
        }
    }

    if (!authRestoreCompleted) {
        return
    }

    fun navigateHome() {
        navController.navigate(Route.Home.path) {
            popUpTo(Route.Home.path) { inclusive = false }
            launchSingleTop = true
        }
    }

    val startMealAnalysis: (MealUploadDraft) -> Unit = { mealUploadDraft ->
        if (!mealSaveInProgress && currentUserId > 0) {
            val draft = mealUploadDraft.copy(ownerId = currentUserId)
            mealSaveInProgress = true
            pendingMealReview = PendingMealReviewState(
                draft = draft,
                isAnalyzing = true,
                reviewedMeal = null,
                errorMessage = null
            )
            navigateHome()
            coroutineScope.launch {
                try {
                    val reviewedMeal = mealReviewRepository.reviewMealDraft(draft)
                    pendingMealReview = pendingMealReview
                        ?.takeIf { it.draft.createdAt == draft.createdAt }
                        ?.copy(
                            isAnalyzing = false,
                            reviewedMeal = reviewedMeal,
                            errorMessage = null
                        )
                } catch (throwable: Throwable) {
                    pendingMealReview = pendingMealReview
                        ?.takeIf { it.draft.createdAt == draft.createdAt }
                        ?.copy(
                            isAnalyzing = false,
                            reviewedMeal = null,
                            errorMessage = throwable.message
                                ?: "AI could not analyze this meal. Try again with clearer notes."
                        )
                } finally {
                    mealSaveInProgress = false
                }
            }
        }
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
                            currentMetabolicRate = ""
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
                                currentMetabolicRate = ""
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
                mealAnalysisNotice = pendingMealReview?.let { pendingReview ->
                    MealAnalysisNoticeUiState(
                        isAnalyzing = pendingReview.isAnalyzing,
                        reviewedMeal = pendingReview.reviewedMeal,
                        errorMessage = pendingReview.errorMessage
                    )
                },
                targetCalories = activeGoalPlan?.dailyCalories
                    ?: targetCalories.toPositiveIntOrDefault(2000),
                targetCarbsGram = activeGoalPlan?.dailyCarbsGram
                    ?: targetCarbsGram.toPositiveIntOrDefault(250),
                targetProteinGram = activeGoalPlan?.dailyProteinGram
                    ?: proteinGoal.toPositiveIntOrDefault(100),
                targetFatGram = activeGoalPlan?.dailyFatGram
                    ?: targetFatGram.toPositiveIntOrDefault(60),
                targetFiberGram = activeGoalPlan?.dailyFiberGram
                    ?: targetFiberGram.toPositiveIntOrDefault(25),
                targetSugarGram = activeGoalPlan?.dailySugarGram
                    ?: targetSugarGram.toPositiveIntOrDefault(50),
                targetSodiumMilligram = activeGoalPlan?.dailySodiumMilligram
                    ?: targetSodiumMilligram.toPositiveIntOrDefault(2300),
                onAddMealClick = { navController.navigate(Route.AddMeal.path) },
                onMealListClick = { navController.navigate(Route.MealList.path) },
                onGoalSettingsClick = { navController.navigate(Route.GoalSettings.path) },
                onRecentStatsClick = { navController.navigate(Route.RecentStats.path) },
                onProfileClick = { navController.navigate(Route.Profile.path) },
                onReviewMealClick = { navController.navigate(Route.MealReview.path) },
                onRetryMealAnalysisClick = {
                    pendingMealReview?.draft?.let(startMealAnalysis)
                },
                onDismissMealAnalysisClick = {
                    pendingMealReview = null
                    mealSaveInProgress = false
                }
            )
        }

        composable(Route.AddMeal.path) {
            AddMealScreen(
                initialMeal = null,
                isSaving = mealSaveInProgress,
                onBackClick = { navController.navigateUp() },
                onSaveClick = { mealUploadDraft ->
                    startMealAnalysis(mealUploadDraft)
                }
            )
        }

        composable(Route.MealReview.path) {
            val pendingReview = pendingMealReview
            MealReviewScreen(
                reviewedMeal = pendingReview?.reviewedMeal,
                isAnalyzing = pendingReview?.isAnalyzing == true,
                errorMessage = pendingReview?.errorMessage,
                onConfirmClick = { reviewedMeal ->
                    coroutineScope.launch {
                        mealRepository.addMealRecord(reviewedMeal)
                        pendingMealReview = null
                        navigateHome()
                    }
                },
                onRetryClick = {
                    pendingMealReview?.draft?.let(startMealAnalysis)
                },
                onDiscardClick = {
                    pendingMealReview = null
                    mealSaveInProgress = false
                    navigateHome()
                },
                onBackClick = { navController.navigateUp() }
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
                    currentMetabolicRate = ""
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
                goalStartDate = goalStartDate,
                onGoalStartDateChange = { goalStartDate = it },
                goalEndDate = goalEndDate,
                onGoalEndDateChange = { goalEndDate = it },
                goalPeriodMode = goalPeriodMode,
                onGoalPeriodModeChange = { goalPeriodMode = it },
                currentWeight = currentWeight,
                onCurrentWeightChange = { currentWeight = it },
                currentMuscleMass = currentMuscleMass,
                onCurrentMuscleMassChange = { currentMuscleMass = it },
                currentMetabolicRate = currentMetabolicRate,
                onCurrentMetabolicRateChange = { currentMetabolicRate = it },
                currentBodyFatPercent = currentBodyFatPercent,
                onCurrentBodyFatPercentChange = { currentBodyFatPercent = it },
                targetWeight = targetWeight,
                onTargetWeightChange = { targetWeight = it },
                targetMuscleMass = targetMuscleMass,
                onTargetMuscleMassChange = { targetMuscleMass = it },
                targetBodyFatPercent = targetBodyFatPercent,
                onTargetBodyFatPercentChange = { targetBodyFatPercent = it },
                targetWeeks = targetWeeks,
                onTargetWeeksChange = { targetWeeks = it },
                targetCalories = targetCalories,
                onTargetCaloriesChange = { targetCalories = it },
                targetCarbsGram = targetCarbsGram,
                onTargetCarbsGramChange = { targetCarbsGram = it },
                proteinGoal = proteinGoal,
                onProteinGoalChange = { proteinGoal = it },
                targetFatGram = targetFatGram,
                onTargetFatGramChange = { targetFatGram = it },
                targetFiberGram = targetFiberGram,
                onTargetFiberGramChange = { targetFiberGram = it },
                targetSugarGram = targetSugarGram,
                onTargetSugarGramChange = { targetSugarGram = it },
                targetSodiumMilligram = targetSodiumMilligram,
                onTargetSodiumMilligramChange = { targetSodiumMilligram = it },
                manualTargetsEnabled = manualTargetsEnabled,
                onManualTargetsEnabledChange = { manualTargetsEnabled = it },
                currentGoalPlan = activeGoalPlan,
                latestBodyMeasurement = bodyMeasurements.firstOrNull(),
                onSaveBodyMeasurement = { bodyMeasurementDraft ->
                    if (currentUserId > 0) {
                        coroutineScope.launch {
                            goalRepository.saveBodyMeasurement(currentUserId, bodyMeasurementDraft)
                        }
                    }
                },
                onSaveClick = { goalPlanDraft ->
                    if (currentUserId > 0) {
                        coroutineScope.launch {
                            goalRepository.saveGoalPlan(currentUserId, goalPlanDraft)
                            navController.navigateUp()
                        }
                    }
                },
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(Route.RecentStats.path) {
            RecentStatsScreen(
                mealRecords = mealRecords,
                goalPlans = goalPlans,
                bodyMeasurements = bodyMeasurements,
                targetCalories = activeGoalPlan?.dailyCalories
                    ?: targetCalories.toPositiveIntOrDefault(2000),
                targetCarbsGram = activeGoalPlan?.dailyCarbsGram
                    ?: targetCarbsGram.toPositiveIntOrDefault(250),
                targetProteinGram = activeGoalPlan?.dailyProteinGram
                    ?: proteinGoal.toPositiveIntOrDefault(100),
                targetFatGram = activeGoalPlan?.dailyFatGram
                    ?: targetFatGram.toPositiveIntOrDefault(60),
                onBackClick = { navController.navigateUp() }
            )
        }
    }
}

private data class PendingMealReviewState(
    val draft: MealUploadDraft,
    val isAnalyzing: Boolean,
    val reviewedMeal: MealRecord?,
    val errorMessage: String?
)

private fun String.toPositiveIntOrDefault(defaultValue: Int): Int {
    return toIntOrNull()?.takeIf { it > 0 } ?: defaultValue
}

private fun Double.toGoalNumberText(): String {
    return if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        "%.1f".format(this)
    }
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
