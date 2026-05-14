# 11주차 스크린샷 촬영 가이드

## 1. 앱 실행

Android Studio에서 프로젝트를 연 뒤 다음 순서로 실행한다.

1. 상단 실행 대상에서 에뮬레이터를 선택한다.
2. Run 버튼을 눌러 앱을 실행한다.
3. 앱 이름 `AIDietRecord`가 실행되면 HomeScreen부터 확인한다.

## 2. 저장 위치

스크린샷은 다음 폴더에 저장한다.

```text
docs/screenshots/
```

폴더가 없으면 직접 생성한다.

## 3. 촬영해야 할 화면

| 순서 | 화면 | 이동 방법 | 저장 파일명 |
| --- | --- | --- | --- |
| 1 | HomeScreen | 앱 실행 직후 | `week11_01_home.png` |
| 2 | AddMealScreen | 홈 화면에서 `음식 추가` 버튼 클릭 | `week11_02_add_meal.png` |
| 3 | MealListScreen | 홈 화면에서 `기록 보기` 버튼 클릭 | `week11_03_meal_list.png` |
| 4 | MealDetailScreen | 식단 리스트에서 아무 기록 하나 클릭 | `week11_04_meal_detail.png` |
| 5 | ProfileScreen | 홈 화면에서 `내 정보 설정` 버튼 클릭 | `week11_05_profile.png` |

## 4. 보고서에 넣을 때 확인할 점

- HomeScreen에는 오늘 섭취 칼로리, 목표 칼로리, 기록 개수, 이동 버튼이 보여야 한다.
- AddMealScreen에는 음식명, 칼로리, 메모 입력칸과 이미지 미리보기 영역이 보여야 한다.
- MealListScreen에는 LazyColumn 형태의 식단 기록 리스트와 각 기록의 날짜/시간이 보여야 한다.
- MealDetailScreen에는 음식 상세 정보, 기록 시간, AI 분석 기록 영역이 보여야 한다.
- ProfileScreen에는 닉네임, 목표 칼로리, 목표 유형 설정 UI가 보여야 한다.

## 5. 제출 전 체크

스크린샷을 저장한 뒤 `docs/week11_progress_report_1.md`의 4번 항목에 있는 파일명과 실제 저장 파일명이 같은지 확인한다.
