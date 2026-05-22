# AI 기반 식단 기록 관리 앱

Kotlin과 Jetpack Compose를 사용하여 개발 중인 모바일 프로그래밍 개인 프로젝트이다. 사용자는 음식명, 설명, 사진을 입력하여 식단을 기록할 수 있고, Firebase AI Gemini 분석 또는 로컬 추정 로직을 통해 칼로리와 영양 정보를 자동으로 저장할 수 있다.

## 현재 진행 상태

- 10주차: 프로젝트 제안서 작성 및 GitHub 문서 정리
- 11주차: 기본 화면 구성, Navigation 연결, LazyColumn 리스트, Room DB 설계
- 12주차: Room DB 실제 연결, 사용자별 식단 저장/조회/상세/수정, 이미지 선택, Firebase AI Gemini 분석 초안 구현

## 구현된 주요 기능

- 로그인 및 회원가입
- 사용자별 식단 기록 분리
- 홈 화면에서 오늘 섭취 칼로리, 남은 칼로리, 영양소 요약 표시
- 음식 추가 화면에서 여러 음식 항목 입력
- 음식 항목별 사진 선택, 변경, 삭제
- 선택한 이미지 URI 저장 및 미리보기 표시
- Firebase AI Gemini 기반 음식 사진/설명 분석
- AI 분석 실패 시 로컬 영양 추정 로직으로 fallback
- Room DB 기반 식단 저장, 조회, 수정
- 식단 기록 리스트 화면의 Today / All Records 필터
- 식단 상세 화면에서 이미지, 음식 목록, 영양 정보, AI 분석 기록 확인
- 최근 통계 화면에서 날짜별 칼로리와 영양소 경향 표시
- 목표 설정 화면에서 목표 칼로리와 단백질 목표 입력
- 프로필 화면에서 닉네임, 아이디, 비밀번호 수정 및 로그아웃

## 화면 구성

현재 구현된 화면은 다음과 같다.

1. LoginScreen
2. HomeScreen
3. AddMealScreen
4. MealListScreen
5. MealDetailScreen
6. EditMealScreen
7. ProfileScreen
8. GoalSettingsScreen
9. RecentStatsScreen

주요 화면 전환은 다음과 같다.

- LoginScreen -> HomeScreen
- HomeScreen -> AddMealScreen
- HomeScreen -> MealListScreen
- MealListScreen -> MealDetailScreen
- MealDetailScreen -> EditMealScreen
- HomeScreen -> ProfileScreen
- HomeScreen -> GoalSettingsScreen
- HomeScreen -> RecentStatsScreen
- AddMealScreen -> 저장 후 MealListScreen

## 기술 스택

- Kotlin
- Jetpack Compose
- Material3
- Navigation Compose
- Room Database
- Kotlin Coroutines / Flow
- Android Activity Result API
- Firebase AI SDK
- Google Services Gradle Plugin

## 데이터 구조

Room Database는 다음 Entity를 사용한다.

- `UserAccount`: 로컬 사용자 계정 정보
- `MealEntity`: 한 끼 식단 기록의 대표 정보
- `MealFoodEntity`: 한 끼 식단에 포함된 개별 음식 항목

한 끼 식단은 여러 음식 항목을 가질 수 있도록 `MealEntity`와 `MealFoodEntity`를 1:N 구조로 분리했다. 화면에서는 `MealWithFoods`를 `MealRecord`로 변환하여 사용한다.

## 과제 요구사항 충족 상태

| 과제 요구사항            | 현재 구현 상태                                                                    |
| ------------------------ | --------------------------------------------------------------------------------- |
| 4개 이상의 화면 전환     | Login, Home, AddMeal, MealList, Detail, Edit, Profile, Goal, Stats 화면 전환 구현 |
| 1개 이상의 리스트 페이지 | MealListScreen에서 LazyColumn 식단 기록 리스트 구현                               |
| 이미지 포함              | OpenDocument 이미지 선택 및 UriImage 미리보기 구현                                |
| 데이터베이스             | Room DB로 사용자, 식단, 음식 항목 저장/조회 구현                                  |
| 선택 기능                | Firebase AI Gemini 기반 AI 분석 기능 구현                                         |
| 패키지 구조              | data, navigation, ui.screen, ui.util 등으로 분리                                  |
