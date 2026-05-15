# AI 기반 식단 기록 관리 앱

## 프로젝트 소개

본 프로젝트는 Kotlin과 Jetpack Compose를 활용하여 개발할 모바일 프로그래밍 개인 프로젝트이다.
사용자는 음식 정보를 직접 입력하거나 음식 사진을 선택하여 식단을 기록할 수 있다.
사진을 선택한 경우 AI 분석 기능을 통해 음식명과 칼로리 추정 결과를 받을 수 있으며, 사용자는 해당 결과를 수정한 뒤 저장할 수 있다.

## 현재 진행 상태

- 11주차 작업 내용: Android 프로젝트 생성, 기본 화면 5개 구현, Navigation Compose 화면 전환 연결, Room DB 설계 코드 작성

## 주요 기능 계획

- 홈 화면: 오늘 섭취 칼로리, 기록 개수, 목표 칼로리 표시
- 음식 추가 화면: 음식명, 칼로리, 메모 직접 입력
- 이미지 선택 기능: 음식 사진 첨부 및 미리보기
- AI 분석 기능: 사진 기반 음식명/칼로리 추정
- 식단 기록 리스트 화면: 저장된 음식 기록을 LazyColumn으로 표시
- 식단 상세 화면: 음식 정보, 사진, 메모, AI 분석 결과 확인
- 내 정보 화면: 닉네임, 목표 칼로리, 목표 유형 설정

## 화면 구성 계획

계획 중인 화면은 다음과 같다.

1. HomeScreen
2. AddMealScreen
3. MealListScreen
4. MealDetailScreen
5. ProfileScreen

계획 중인 화면 전환은 다음과 같다.

- HomeScreen → AddMealScreen
- HomeScreen → MealListScreen
- MealListScreen → MealDetailScreen
- HomeScreen → ProfileScreen
- AddMealScreen → 저장 후 HomeScreen 또는 MealListScreen

## 기술 스택 계획

- Kotlin
- Jetpack Compose
- Navigation Compose
- Room Database
- Image Picker
- AI 분석 모듈

AI 분석 기능은 초기 구현 단계에서 모의 AI 분석기로 구성할 수 있으며, 이후 필요에 따라 Gemini 또는 OpenAI Vision API와 같은 실제 AI API로 교체할 수 있다. API 키는 코드에 직접 하드코딩하지 않는다.

## 과제 요구사항 충족 계획

| 과제 요구사항            | 본 프로젝트 적용 계획                                  |
| ------------------------ | ------------------------------------------------------ |
| 4개 이상의 화면 전환     | 홈, 음식 추가, 식단 리스트, 상세, 내 정보 화면 간 전환 |
| 1개 이상의 리스트 페이지 | 식단 기록 리스트 화면                                  |
| 이미지 포함              | 음식 사진 선택 및 표시                                 |
| 데이터베이스             | Room DB로 식단 기록 저장                               |
| 선택 기능                | AI 음식 분석 기능                                      |
| 적절한 패키지 구조       | data, ui, navigation, viewmodel, ai 패키지로 분리 예정 |

## 개발 일정

- 10주차: 프로젝트 제안서 작성 및 GitHub 문서 정리
- 11주차: 기본 화면 구성, Navigation 연결, Room DB 설계
- 12주차: 식단 추가/조회/상세 기능 구현, 이미지 선택 기능 구현
- 13주차: AI 분석 기능 구현 또는 Mock AI 분석 모듈 적용, UI/UX 개선
- 14주차: 최종 기능 점검, README 정리, APK/QR 준비, 녹화 영상 제작
- 15주차: 최종 발표

## 범위 제한

이번 프로젝트에서는 핵심 식단 기록 기능에 집중하기 위해 다음 기능은 구현하지 않는다.

- 카카오 로그인
- 네이버 로그인
- Firebase Auth
- 지도 기능
- 친구 공유 기능
- 복잡한 영양소 분석
- 운동 기록 기능
- 상용 서비스 수준의 추천 알고리즘
