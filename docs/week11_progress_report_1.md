# 모바일 프로그래밍 개인 프로젝트 1차 진행보고서

## 1. 프로젝트 정보

- 프로젝트명: AI 기반 식단 기록 관리 앱
- 제출 주차: 11주차
- 기간: 05-11 ~ 05-17
- 개발 목표: 사용자가 식사 정보를 직접 입력하거나 음식 사진을 선택하여 식단을 기록하고, AI 분석 결과를 참고하여 음식명과 칼로리를 저장할 수 있는 Android 앱 개발

## 2. 이번 주 진행 목표

11주차에는 10주차 프로젝트 제안서를 바탕으로 실제 Android 프로젝트를 생성하고, Jetpack Compose 기반 기본 화면과 화면 전환 구조를 구현하는 것을 목표로 하였다. 또한 식단 기록 저장을 위한 Room Database 설계 코드를 작성하여 다음 주 기능 구현의 기반을 마련하였다.

이번 주의 핵심 작업 범위는 다음과 같다.

- Android Studio 기반 프로젝트 생성
- 앱의 주요 화면 5개 구현
- Navigation Compose를 이용한 화면 전환 연결
- LazyColumn 기반 식단 기록 리스트 화면 구현
- 식단 기록 저장을 위한 Room Database 설계 코드 작성
- 이미지 선택 및 AI 분석 기능의 UI 흐름 정리

## 3. 이번 주 진행 내용

### 3.1 주요 화면 구조 정리

프로젝트 요구사항인 4개 이상의 화면 전환을 충족하기 위해 총 5개의 화면을 구현하였다.

1. HomeScreen
   - 오늘 섭취 칼로리, 기록 개수, 목표 칼로리를 요약해서 보여주는 화면
   - 음식 추가, 식단 리스트, 내 정보 화면으로 이동하는 진입점 역할

2. AddMealScreen
   - 음식명, 칼로리, 메모를 직접 입력하는 화면
   - 음식 사진 미리보기 영역 포함
   - Mock AI 분석 버튼을 통해 임시 분석 결과를 확인할 수 있도록 구성

3. MealListScreen
   - 샘플 식단 기록을 LazyColumn으로 표시하는 리스트 화면
   - 각 식단 기록의 날짜와 시간을 함께 표시
   - 각 항목을 선택하면 상세 화면으로 이동

4. MealDetailScreen
   - 선택한 식단 기록의 음식명, 기록 시간, 칼로리, 메모, 이미지, AI 분석 결과를 확인하는 화면

5. ProfileScreen
   - 사용자 닉네임, 목표 칼로리, 목표 유형을 설정하는 화면

### 3.2 화면 전환 흐름 설계

Navigation Compose를 사용하여 다음과 같은 화면 이동 흐름을 구현하였다.

- HomeScreen -> AddMealScreen
- HomeScreen -> MealListScreen
- MealListScreen -> MealDetailScreen
- HomeScreen -> ProfileScreen
- AddMealScreen -> 저장 후 HomeScreen 또는 MealListScreen

이 구조를 통해 과제의 필수 조건인 4개 이상의 화면 전환을 충족할 수 있다.

### 3.3 데이터베이스 구조 설계

식단 기록은 Room Database를 사용하여 저장할 예정이다. 11주차에는 Entity, DAO, Database, Repository의 기본 설계 코드를 작성하였다. 기본 데이터 모델은 다음과 같다.

```kotlin
@Entity(tableName = "meal_records")
data class MealRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val foodName: String,
    val calories: Int,
    val memo: String,
    val imageUri: String?,
    val aiFoodName: String?,
    val aiCalories: Int?,
    val createdAt: Long
)
```

Room 구성은 다음 역할로 나누어 작성하였다.

- Entity: `data/model/MealRecord.kt`
- DAO: `data/local/MealDao.kt`
- Database: `data/local/MealDatabase.kt`
- Repository: `data/repository/MealRepository.kt`

우선 12주차에는 현재 작성한 설계 코드를 실제 화면 상태와 연결하여 식단 기록 추가와 조회 기능을 구현하고, 이후 수정 및 삭제 기능을 확장할 예정이다.

### 3.4 AI 분석 모듈 구조 정리

선택 기능으로 AI 분석 기능을 적용할 예정이다. 다만 실제 외부 API를 바로 연결하기보다는, 초기 단계에서는 Mock AI 분석 흐름을 사용하여 앱 화면과 사용자 흐름을 먼저 구현하였다.

Mock AI 분석 모듈의 역할은 다음과 같다.

- AddMealScreen에서 Mock AI 분석 버튼 제공
- 버튼 클릭 시 임시 음식명과 칼로리 추정값 표시
- 추후 이미지 선택 기능과 실제 AI API를 연결할 수 있도록 화면 흐름 유지

추후 개발 상황에 따라 Gemini 또는 OpenAI Vision API와 같은 실제 외부 AI API로 교체할 수 있다. 이 경우 API 키는 코드에 직접 작성하지 않고 별도 설정 방식으로 관리한다.

### 3.5 패키지 구조 계획

프로젝트가 커졌을 때 코드가 섞이지 않도록 다음과 같은 패키지 구조를 적용하였다.

```text
data
data.local
data.model
data.repository
navigation
ui
ui.screen
```

이 구조를 통해 화면, 데이터베이스, 화면 전환을 역할별로 분리하였다. AI 모듈과 ViewModel은 12~13주차 기능 구현 단계에서 추가할 예정이다.

### 3.6 문서 정리

10주차 프로젝트 제안서와 README를 바탕으로 11주차 1차 진행보고서를 작성하였다. README에는 현재 단계가 11주차 기본 화면 구현 단계임을 표시하고, 제출 문서 목록에 1차 진행보고서와 스크린샷 가이드 링크를 추가하였다.

### 3.7 빌드 확인

Android Studio 내장 JDK를 사용하여 다음 명령으로 디버그 빌드를 확인하였다.

```bash
./gradlew :app:assembleDebug
```

빌드 결과는 성공이며, 생성된 앱은 에뮬레이터에서 실행하여 스크린샷을 촬영할 수 있는 상태이다.

## 4. 구현 화면 및 스크린샷

11주차에 구현한 화면은 다음과 같다. 스크린샷은 Android Studio 에뮬레이터에서 앱을 실행한 뒤 아래 순서대로 촬영하여 `docs/screenshots/` 폴더에 저장한다.

| 번호 | 화면 | 파일명 | 포함 내용 |
| --- | --- | --- | --- |
| 1 | HomeScreen | `week11_01_home.png` | 오늘 섭취 칼로리, 기록 개수, 목표 칼로리, 주요 이동 버튼 |
| 2 | AddMealScreen | `week11_02_add_meal.png` | 음식명, 칼로리, 메모 입력 UI, 이미지 미리보기 영역, Mock AI 분석 버튼 |
| 3 | MealListScreen | `week11_03_meal_list.png` | LazyColumn 기반 식단 기록 리스트, 기록 날짜와 시간 |
| 4 | MealDetailScreen | `week11_04_meal_detail.png` | 음식 상세 정보, 기록 시간, 이미지 표시 영역, AI 분석 기록 |
| 5 | ProfileScreen | `week11_05_profile.png` | 닉네임, 목표 칼로리, 목표 유형 설정 UI |

스크린샷 촬영 방법은 `docs/week11_screenshot_guide.md`에 정리하였다.

## 5. 문제 해결 과정

이번 주에는 프로젝트 생성 후 바로 전체 기능을 모두 구현하려고 하면 범위가 과도하게 커질 수 있는 문제가 있었다. 식단 기록, 이미지 선택, AI 분석, 데이터 저장을 모두 한 번에 구현하기보다 11주차에는 화면 구조와 데이터베이스 설계를 먼저 완성하는 방향으로 범위를 조정하였다.

이를 해결하기 위해 다음과 같이 구현 우선순위를 나누었다.

1. 기본 화면과 Navigation을 먼저 구현하였다.
2. Room Database의 Entity, DAO, Database, Repository 구조를 작성하였다.
3. 이미지 선택 기능은 실제 파일 선택 전, 미리보기 영역을 먼저 구성하였다.
4. AI 분석 기능은 초기에는 Mock 분석 버튼으로 앱 흐름을 확인하도록 구현하였다.
5. 실제 저장/조회와 이미지 선택 기능은 12주차에 연결하기로 하였다.

이 방식으로 핵심 식단 기록 기능의 화면 흐름을 먼저 완성하고, 선택 기능인 AI 분석은 앱 흐름을 해치지 않는 범위에서 점진적으로 확장할 수 있다.

## 6. GitHub 커밋 내역

현재까지 주요 커밋은 다음과 같다.

- 10주차 프로젝트 제안서 작성
- 제출 기한 관련 문서 정리

11주차에는 Android 프로젝트 생성, 기본 화면 구현, Navigation 연결, Room DB 설계, 진행보고서 문서 갱신 작업을 수행하였다. 커밋 시에는 다음과 같이 나누어 관리할 수 있다.

- Android 프로젝트 기본 구조 추가
- 11주차 Compose 화면 및 Navigation 구현
- Room DB 설계 코드 추가
- 1차 진행보고서 및 README 갱신

## 7. 다음 주 구현 예정 내용

12주차에는 11주차에 구현한 구조를 바탕으로 실제 데이터 저장 및 조회 기능을 연결할 예정이다.

- Room Database 인스턴스 생성 및 앱과 연결
- AddMealScreen에서 입력한 식단 기록 저장
- MealListScreen에서 DB에 저장된 식단 기록 조회
- MealDetailScreen에서 선택한 기록 상세 표시
- 이미지 선택 기능 구현 및 선택 이미지 미리보기
- Mock AI 분석 결과를 입력 화면에 반영

12주차 이후에는 사용자가 사진을 선택하고 AI 추정 결과를 확인한 뒤 저장할 수 있는 흐름을 완성할 예정이다.
