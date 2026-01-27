# Kotlin 학습 커리큘럼

- **대상**: Java 경험자 (콘솔 게임 프로젝트 구현 경험)
- **목표**: Java 패키지 전체(43개 파일)를 Kotlin으로 점진적 변환
- **최종 검증**: Kotlin 실행 결과 = Java 실행 결과
- **방식**: 의존성 기반 11단계 학습 + 점진적 변환 (Java/Kotlin 공존)

---

## 단계 1: 기반 클래스 (의존성 없음)

- [x] 학습 시작
- [x] 이론 학습 완료
- [x] 실습 완료
- [x] 체크포인트 통과

### 학습 목표
- Kotlin의 기본 변수 선언과 타입 추론 이해
- `enum class` 문법과 `when` 표현식 기초 학습
- `data class`의 자동 생성 기능 파악
- Null Safety 기초 개념 습득

### 핵심 개념
- `val`: 불변 변수 (Java의 final 변수와 유사)
- `var`: 가변 변수
- 타입 추론: 변수 타입을 명시하지 않아도 컴파일러가 추론
- `object`: 싱글톤 선언, 상수 그룹에 활용
- `const val`: 컴파일 타임 상수 (primitive와 String만 가능)
- `enum class`: Java enum과 유사하나 `when`과 함께 사용 시 exhaustive 체크
- `data class`: equals, hashCode, toString, copy 자동 생성

### Java와 차이점
- Kotlin은 문장 끝에 세미콜론이 필요 없음
- `public static final` 상수는 `const val`로 대체
- 상수 그룹은 `object` 또는 `companion object`에 정의
- enum은 `when`과 함께 사용 시 모든 케이스 처리 강제 (exhaustive)
- data class는 getter/setter, equals, hashCode, toString 자동 생성
- new 키워드 없이 객체 생성

### Null Safety
- 기본적으로 모든 타입은 non-null
- `?` 접미사로 nullable 타입 표시 (예: `String?`는 null 가능)
- 상수 클래스에서는 대부분 non-null 사용
- enum 값은 기본적으로 non-null

### 변환 실습

#### 파일 1: `MyHomeConstants.java`
- **파일 특성**: static final 상수들만 있는 유틸리티 클래스
- **적용할 Kotlin 개념**: `object`, `const val`

#### 파일 2: `ItemType.java`
- **파일 특성**: 아이템 종류를 정의한 enum
- **적용할 Kotlin 개념**: `enum class`, `when` 표현식 기초

#### 파일 3: `AreaType.java`
- **파일 특성**: 영역 종류를 정의한 enum
- **적용할 Kotlin 개념**: `enum class`

#### 파일 4: `TitleInfo.java`
- **파일 특성**: 칭호 정보를 담는 단순 데이터 클래스
- **적용할 Kotlin 개념**: `data class`, `val`/`var`, 기본 파라미터

### 검증
- 매 파일 변환 후: `./gradlew compileKotlin compileJava`
- 단계 완료 후: 기존 Java 코드에서 변환된 Kotlin 클래스 참조 가능 확인

### 체크포인트
- [x] `val`과 `var`의 차이를 설명할 수 있다
- [x] `const val`과 `val`의 차이를 설명할 수 있다
- [x] `enum class`와 `when` 표현식의 기본 사용법을 이해한다
- [x] `data class`의 자동 생성 기능을 설명할 수 있다
- [x] 4개 파일 변환 완료 + 컴파일 성공

---

## 단계 2: 베이스 클래스

- [ ] 학습 시작
- [ ] 이론 학습 완료
- [ ] 실습 완료
- [ ] 체크포인트 통과

### 선수 조건
- 단계 1 완료 필요

### 학습 목표
- `object`와 파일 레벨 함수의 차이 이해
- `abstract class` 정의 및 추상 멤버 선언 학습
- `open` 키워드의 필요성 파악
- 프로퍼티 문법과 커스텀 getter/setter 이해

### 핵심 개념
- `object`: 싱글톤 선언 (클래스 정의와 인스턴스 생성 동시에)
- 파일 레벨 함수: 클래스 없이 파일에 직접 함수 정의
- `abstract class`: 추상 클래스, abstract 멤버는 자동으로 open
- `open` 키워드: 상속/오버라이드 가능하도록 개방 (기본은 final)
- 프로퍼티: 필드 + getter/setter의 개념 통합
- `protected`: 하위 클래스에서만 접근 가능

### Java와 차이점
- static 메서드만 있는 유틸리티 클래스는 `object`로 대체
- Kotlin 클래스는 기본적으로 final (상속 불가)
- 상속 허용을 위해 명시적으로 `open` 필요
- 필드 선언이 없고 프로퍼티만 존재
- getter/setter가 자동 생성되며 커스텀 구현 가능

### Null Safety
- 추상 프로퍼티의 nullable 여부 신중히 설계
- 부모 클래스에서 정한 nullable 여부는 자식에서 유지 필요
- `lateinit var`: 나중에 초기화할 non-null 변수 (DI에서 활용)

### 변환 실습

#### 파일 1: `MyHomeUtils.java`
- **파일 특성**: static 메서드만 있는 유틸리티 클래스
- **적용할 Kotlin 개념**: `object` 또는 파일 레벨 함수
- **참고**: Java에서 호출 시 `@JvmStatic` 어노테이션 고려

#### 파일 2: `Item.java`
- **파일 특성**: 아이템 계층의 추상 베이스 클래스
- **적용할 Kotlin 개념**: `abstract class`, 프로퍼티, 추상 메서드, `open`

#### 파일 3: `Character.java`
- **파일 특성**: 캐릭터 계층의 추상 베이스 클래스
- **적용할 Kotlin 개념**: `abstract class`, `open` 프로퍼티, 추상 메서드

#### 파일 4: `MiniGamePiece.java`
- **파일 특성**: 미니게임 피스의 추상 베이스 클래스
- **적용할 Kotlin 개념**: `abstract class`, `open`, 프로퍼티

### 검증
- 매 파일 변환 후: `./gradlew compileKotlin compileJava`
- 단계 완료 후: 기존 Java 자식 클래스에서 Kotlin 부모 클래스 상속 가능 확인

### 체크포인트
- [ ] `object`와 파일 레벨 함수의 차이를 설명할 수 있다
- [ ] `open` 키워드의 필요성을 이해한다
- [ ] 추상 클래스와 추상 멤버 정의 방법을 알고 있다
- [ ] 프로퍼티 문법을 이해한다
- [ ] 8개 파일 누적 변환 완료 + 컴파일 성공

---

## 단계 3: 인벤토리 시스템

- [ ] 학습 시작
- [ ] 이론 학습 완료
- [ ] 실습 완료
- [ ] 체크포인트 통과

### 선수 조건
- 단계 2 완료 필요

### 학습 목표
- Kotlin 클래스에서 다른 Kotlin 클래스 참조 방법 학습
- nullable 프로퍼티와 safe call 연산자 실습
- 기본 컬렉션 사용법 (List, MutableList) 학습

### 핵심 개념
- `?.` (safe call): null이면 null 반환, 아니면 메서드 호출
- `?:` (Elvis 연산자): null일 때 기본값 지정
- `!!` (non-null assertion): null이 아님을 단언 (가급적 사용 자제)
- `mutableListOf()`: 가변 리스트 생성
- `listOf()`: 불변 리스트 생성

### Java와 차이점
- nullable 필드는 `?` 접미사로 명시
- null 체크 코드가 `?.`와 `?:`로 간결해짐
- ArrayList는 `mutableListOf()`로 대체
- Collections.unmodifiableList는 `listOf()`와 `toList()`로 대체

### Null Safety
- `data class`의 nullable 필드: `val field: Type?`
- safe call 체이닝: `obj?.field?.method()`
- Elvis 연산자로 기본값 처리: `obj?.field ?: defaultValue`
- let을 활용한 null 처리: `obj?.let { ... }`

### 변환 실습

#### 파일 1: `ItemEntry.java`
- **파일 특성**: Item을 참조하는 인벤토리 엔트리
- **적용할 Kotlin 개념**: `data class`, nullable 필드, Kotlin Item 클래스 참조

#### 파일 2: `Inventory.java`
- **파일 특성**: ItemEntry 리스트를 관리하는 인벤토리
- **적용할 Kotlin 개념**: `mutableListOf`, 컬렉션 메서드, safe call

### 검증
- 매 파일 변환 후: `./gradlew compileKotlin compileJava`
- 단계 완료 후: 인벤토리 기능 동작 확인

### 체크포인트
- [ ] safe call 연산자 `?.`의 동작을 설명할 수 있다
- [ ] Elvis 연산자 `?:`의 용도를 알고 있다
- [ ] `mutableListOf()`와 `listOf()`의 차이를 이해한다
- [ ] 10개 파일 누적 변환 완료 + 컴파일 성공

---

## 단계 4: 아이템 서브클래스

- [ ] 학습 시작
- [ ] 이론 학습 완료
- [ ] 실습 완료
- [ ] 체크포인트 통과

### 선수 조건
- 단계 3 완료 필요

### 학습 목표
- Kotlin 상속 문법 (`:`) 학습
- `override` 키워드 사용법 습득
- `super` 호출을 통한 부모 클래스 접근 방법 이해
- 주 생성자에서 부모 생성자 호출 패턴 학습

### 핵심 개념
- 상속 문법: `class Child : Parent()`
- `override`: 부모의 open 멤버 재정의 (필수 키워드)
- `super`: 부모 클래스 멤버 접근
- 주 생성자 상속: `class Child(param: Type) : Parent(param)`
- `init` 블록: 인스턴스 초기화 로직

### Java와 차이점
- extends 대신 `:` 사용
- @Override 어노테이션 대신 `override` 키워드 (필수)
- 생성자에서 `super()` 호출은 클래스 헤더에서 처리
- 필드 초기화가 주 생성자나 프로퍼티 선언에서 직접 가능

### Null Safety
- 부모 클래스의 nullable 프로퍼티 오버라이드 시 규칙
- 생성자 파라미터의 nullable 여부 설계
- 오버라이드 메서드의 반환 타입 nullable 변환 규칙

### 변환 실습

#### 파일 1: `GrowthItem.java`
- **파일 특성**: Item을 상속하는 성장 아이템
- **적용할 Kotlin 개념**: 상속 (`:`), `override`, 주 생성자 상속

#### 파일 2: `StoreItem.java`
- **파일 특성**: Item을 상속하는 상점 아이템
- **적용할 Kotlin 개념**: 상속, `override`, `super` 호출

#### 파일 3: `CraftItem.java`
- **파일 특성**: Item을 상속하는 제작 아이템
- **적용할 Kotlin 개념**: 상속, `override`

#### 파일 4: `Potion.java`
- **파일 특성**: Item을 상속하는 포션
- **적용할 Kotlin 개념**: 상속, `override`, `data class`와의 차이 이해

### 검증
- 매 파일 변환 후: `./gradlew compileKotlin compileJava`
- 단계 완료 후: 아이템 시스템 전체 동작 확인

### 체크포인트
- [ ] Kotlin 상속 문법 `:`을 사용할 수 있다
- [ ] `override` 키워드가 필수인 이유를 설명할 수 있다
- [ ] 주 생성자에서 부모 생성자 호출 방법을 알고 있다
- [ ] 14개 파일 누적 변환 완료 + 컴파일 성공

---

## 단계 5: 캐릭터 서브클래스

- [ ] 학습 시작
- [ ] 이론 학습 완료
- [ ] 실습 완료
- [ ] 체크포인트 통과

### 선수 조건
- 단계 4 완료 필요

### 학습 목표
- 복잡한 클래스의 Kotlin 변환 실습
- 다중 의존성 처리 (Character + Inventory + Item)
- 보조 생성자 (secondary constructor) 학습
- 다단계 상속 구조 (Merchant → NPC → Character) 이해

### 핵심 개념
- 보조 생성자: `constructor` 키워드로 추가 생성자 정의
- `this()`: 같은 클래스의 다른 생성자 호출
- 프로퍼티 초기화 순서: 주 생성자 파라미터 → init 블록 → 보조 생성자
- 복잡한 프로퍼티: 커스텀 getter/setter 활용

### Java와 차이점
- 여러 생성자 오버로딩이 기본 파라미터로 대체 가능
- this() 호출이 보조 생성자 선언부에 위치
- 복잡한 초기화 로직은 init 블록에 작성
- 필드와 메서드가 프로퍼티로 통합됨

### Null Safety
- 복잡한 클래스에서 nullable 프로퍼티 관리
- 초기화 시점에 따른 nullable 처리
- `lateinit var`와 `lazy`의 적절한 사용
- null 안전한 컬렉션 접근

### 변환 실습

#### 파일 1: `NPC.java`
- **파일 특성**: Character를 상속하는 NPC 클래스
- **적용할 Kotlin 개념**: 상속, 보조 생성자, 프로퍼티 오버라이드

#### 파일 2: `Player.java`
- **파일 특성**: Character를 상속하는 플레이어 (가장 복잡)
- **적용할 Kotlin 개념**: 복잡한 프로퍼티, 컬렉션 활용, 스코프 함수 미리보기
- **참고**: 약 600줄의 복잡한 클래스, 단계적 변환 권장

#### 파일 3: `Merchant.java`
- **파일 특성**: NPC를 상속하는 상인 (다단계 상속)
- **적용할 Kotlin 개념**: 다단계 상속, `super` 체인

### 검증
- 매 파일 변환 후: `./gradlew compileKotlin compileJava`
- 단계 완료 후: 캐릭터 시스템 전체 동작 확인

### 체크포인트
- [ ] 보조 생성자의 용도와 문법을 이해한다
- [ ] 다단계 상속 구조를 Kotlin으로 구현할 수 있다
- [ ] 복잡한 클래스 변환 시 단계적 접근 방법을 알고 있다
- [ ] 17개 파일 누적 변환 완료 + 컴파일 성공

---

## 단계 6: 퀘스트 시스템

- [ ] 학습 시작
- [ ] 이론 학습 완료
- [ ] 실습 완료
- [ ] 체크포인트 통과

### 선수 조건
- 단계 5 완료 필요

### 학습 목표
- Kotlin 컬렉션의 불변/가변 구분 심화 학습
- 컬렉션 초기화 패턴 습득
- 람다 표현식 기초 학습
- 컬렉션 조작 함수 활용

### 핵심 개념
- `mutableListOf()`: 가변 리스트 생성
- `listOf()`: 불변 리스트 생성
- 람다 표현식: `{ 파라미터 -> 본문 }` 형식
- `it` 키워드: 단일 파라미터 람다에서 암시적 파라미터명
- 컬렉션 함수: `find`, `filter`, `forEach`, `map`, `any`, `all`
- `apply` 스코프 함수: 객체 초기화 후 자신 반환

### Java와 차이점
- 컬렉션의 불변/가변이 타입 레벨에서 구분됨
- Java의 Stream은 별도 호출 필요, Kotlin은 컬렉션에 직접 함수 제공
- 람다 문법이 더 간결 (마지막 파라미터가 람다면 괄호 밖으로 이동 가능)
- Collections.singletonList()는 `listOf(single)` 또는 `singletonList()`로 대체

### Null Safety
- nullable 요소를 가진 컬렉션: `List<String?>`
- nullable 컬렉션: `List<String>?`
- `filterNotNull()`: null 요소 제거
- `firstOrNull()`, `find()`: 요소 없으면 null 반환

### 변환 실습

#### 파일 1: `QuestInfo.java`
- **파일 특성**: 퀘스트 정보 데이터 클래스
- **적용할 Kotlin 개념**: `data class`, 기본 파라미터

#### 파일 2: `Quest.java`
- **파일 특성**: 퀘스트 클래스
- **적용할 Kotlin 개념**: 클래스, 프로퍼티, 메서드 변환

#### 파일 3: `Title.java`
- **파일 특성**: 칭호 클래스
- **적용할 Kotlin 개념**: 클래스, TitleInfo 참조

#### 파일 4: `TitleStorage.java`
- **파일 특성**: 칭호 저장소 (static 컬렉션)
- **적용할 Kotlin 개념**: `object`, 컬렉션 초기화, `listOf`

#### 파일 5: `QuestStorage.java`
- **파일 특성**: 퀘스트 저장소 (static 컬렉션, 초기화 로직)
- **적용할 Kotlin 개념**: `object`, `mutableListOf`, `apply`, 람다

### 검증
- 매 파일 변환 후: `./gradlew compileKotlin compileJava`
- 단계 완료 후: 퀘스트/칭호 시스템 동작 확인

### 체크포인트
- [ ] 불변 컬렉션과 가변 컬렉션의 차이를 설명할 수 있다
- [ ] 람다 표현식의 기본 문법을 이해한다
- [ ] `apply` 스코프 함수의 용도를 알고 있다
- [ ] 컬렉션 함수 `find`, `filter`를 사용할 수 있다
- [ ] 22개 파일 누적 변환 완료 + 컴파일 성공

---

## 단계 7: 게임 메카닉

- [ ] 학습 시작
- [ ] 이론 학습 완료
- [ ] 실습 완료
- [ ] 체크포인트 통과

### 선수 조건
- 단계 6 완료 필요

### 학습 목표
- 2D 배열 초기화 및 조작 방법 학습
- `when` 표현식 심화 (표현식으로 사용, 범위 매칭)
- 람다와 고차 함수 활용
- 타이머/주기적 작업 로직의 Kotlin 변환

### 핵심 개념
- 2D 배열: `Array(rows) { Array(cols) { initial } }`
- `when` 표현식: switch의 강화판, 표현식으로 값 반환 가능
- `when`의 범위 매칭: `in 1..10`, `!in`, `is` 타입 체크
- 람다를 파라미터로 받는 함수 호출
- `repeat()`: 반복 실행 함수

### Java와 차이점
- 배열 생성이 람다 기반 초기화로 간결해짐
- switch 대신 when, when은 표현식으로도 사용 가능
- for 루프가 `for (item in collection)` 또는 `repeat()` 형태
- 범위 연산자 `..`와 `until`, `downTo`, `step`

### Null Safety
- 2D 배열에서 nullable 요소 처리
- 게임 로직에서 null 상태 표현
- `when`에서 null 케이스 처리

### 변환 실습

#### 파일 1: `ItemStorage.java`
- **파일 특성**: static 아이템 데이터 저장소
- **적용할 Kotlin 개념**: `object`, 컬렉션 초기화, `mapOf`

#### 파일 2: `Bear.java`
- **파일 특성**: MiniGamePiece를 상속하는 곰
- **적용할 Kotlin 개념**: 상속, 프로퍼티 오버라이드

#### 파일 3: `Fish.java`
- **파일 특성**: MiniGamePiece를 상속하는 물고기
- **적용할 Kotlin 개념**: 상속, 프로퍼티 오버라이드

#### 파일 4: `BearCatchesFishGame.java`
- **파일 특성**: 2D 그리드 기반 미니게임
- **적용할 Kotlin 개념**: 2D 배열, `when`, 람다, 게임 루프

#### 파일 5: `CultivateTimer.java`
- **파일 특성**: 재배 완료 타이머
- **적용할 Kotlin 개념**: Timer 사용, 람다로 TimerTask 대체

#### 파일 6: `StoreTimer.java`
- **파일 특성**: 상점 타이머
- **적용할 Kotlin 개념**: Timer 사용, 람다

#### 파일 7: `ProgressBar.java`
- **파일 특성**: 콘솔 진행률 표시
- **적용할 Kotlin 개념**: `repeat()`, 문자열 템플릿

### 검증
- 매 파일 변환 후: `./gradlew compileKotlin compileJava`
- 단계 완료 후: 미니게임 및 타이머 동작 확인

### 체크포인트
- [ ] Kotlin에서 2D 배열을 초기화할 수 있다
- [ ] `when` 표현식의 다양한 사용법을 이해한다
- [ ] 람다를 Timer나 콜백에 활용할 수 있다
- [ ] 29개 파일 누적 변환 완료 + 컴파일 성공

---

## 단계 8: 영역 (Area) 시스템

- [ ] 학습 시작
- [ ] 이론 학습 완료
- [ ] 실습 완료
- [ ] 체크포인트 통과

### 선수 조건
- 단계 7 완료 필요

### 학습 목표
- 복잡한 추상 클래스 상속 구조 변환
- 스코프 함수 심화 (`let`, `run`, `with`, `also`, `apply`)
- 확장 함수 (extension function) 기초 학습
- 복잡한 게임 로직의 Kotlin idiom 적용

### 핵심 개념
- 스코프 함수:
  - `apply`: 객체 초기화 후 자신 반환
  - `let`: nullable 처리, 변환 후 결과 반환
  - `run`: 객체 컨텍스트에서 로직 실행, 결과 반환
  - `with`: 객체를 인자로 받아 컨텍스트에서 로직 실행
  - `also`: 부수 효과 (로깅 등) 후 자신 반환
- 확장 함수: 기존 클래스에 메서드 추가 (원본 수정 없이)
- `takeIf`, `takeUnless`: 조건부 null 반환

### Java와 차이점
- 스코프 함수로 빌더 패턴 대체 가능
- 확장 함수로 유틸리티 클래스 대체 가능
- 메서드 체이닝이 더 자연스러움
- backing field 개념과 커스텀 getter/setter

### Null Safety
- `let`을 활용한 null 안전한 처리 패턴
- 스코프 함수에서의 nullable 처리 전략
- 확장 함수에서 nullable receiver 처리

### 변환 실습

#### 파일 1: `Area.java`
- **파일 특성**: 영역 계층의 추상 베이스 클래스
- **적용할 Kotlin 개념**: `abstract class`, `open`, 추상 메서드

#### 파일 2: `Farm.java`
- **파일 특성**: Area를 상속하는 농장
- **적용할 Kotlin 개념**: 상속, 스코프 함수

#### 파일 3: `AnimalFarm.java`
- **파일 특성**: Area를 상속하는 목장
- **적용할 Kotlin 개념**: 상속, 컬렉션 활용

#### 파일 4: `Forest.java`
- **파일 특성**: Area를 상속하는 숲
- **적용할 Kotlin 개념**: 상속, 게임 로직 변환

#### 파일 5: `CraftShop.java`
- **파일 특성**: Area를 상속하는 제작소
- **적용할 Kotlin 개념**: 상속, 복잡한 메서드 변환

#### 파일 6: `Arcade.java`
- **파일 특성**: Area를 상속하는 오락실 (숫자야구, 가위바위보)
- **적용할 Kotlin 개념**: 상속, `when`, 랜덤, 사용자 입력 처리
- **참고**: 약 800줄의 복잡한 클래스, 게임별로 단계적 변환 권장

### 검증
- 매 파일 변환 후: `./gradlew compileKotlin compileJava`
- 단계 완료 후: 모든 영역 기능 동작 확인

### 체크포인트
- [ ] 스코프 함수 5가지의 차이를 설명할 수 있다
- [ ] 확장 함수의 개념과 활용법을 이해한다
- [ ] 복잡한 클래스 변환에 Kotlin idiom을 적용할 수 있다
- [ ] 35개 파일 누적 변환 완료 + 컴파일 성공

---

## 단계 9: 사운드 시스템

- [ ] 학습 시작
- [ ] 이론 학습 완료
- [ ] 실습 완료
- [ ] 체크포인트 통과

### 선수 조건
- 단계 8 완료 필요

### 학습 목표
- Java 라이브러리 인터페이스 구현 (LineListener)
- Java Sound API와 Kotlin 연동
- SAM (Single Abstract Method) 변환 이해

### 핵심 개념
- Java 인터페이스 구현: SAM 인터페이스는 람다로 대체 가능
- `object : Interface` 문법: 익명 객체로 인터페이스 구현
- Java 리소스 관리: `use` 함수 (try-with-resources 대체)
- `@Throws` 어노테이션: Java 호출 시 checked exception 선언

### Java와 차이점
- SAM 인터페이스는 람다로 간단히 구현 가능
- checked exception이 없으므로 throws 선언 불필요
- try-with-resources는 `use` 확장 함수로 대체
- 익명 클래스 문법이 `object : Interface { }` 형태

### Null Safety
- Java 라이브러리에서 오는 nullable 값 처리
- `@Nullable`, `@NotNull` 어노테이션 존중
- platform type (타입!)의 위험성 이해

### 변환 실습

#### 파일 1: `SoundPlayerUsingClip.java`
- **파일 특성**: LineListener 인터페이스 구현, Java Sound API 사용
- **적용할 Kotlin 개념**: 인터페이스 구현, nullable 프로퍼티, `when`, `use`
- **참고**: Java 라이브러리 그대로 사용, Kotlin 문법 변환에 집중

#### 파일 2: `SoundPlayerClipTest.java`
- **파일 특성**: SoundPlayerUsingClip 테스트
- **적용할 Kotlin 개념**: main 함수, Java 클래스 호출

### 검증
- 매 파일 변환 후: `./gradlew compileKotlin compileJava`
- 단계 완료 후: 사운드 재생 테스트

### 체크포인트
- [ ] Java 인터페이스를 Kotlin에서 구현하는 방법을 알고 있다
- [ ] SAM 변환의 개념을 이해한다
- [ ] `use` 함수의 용도를 알고 있다
- [ ] 37개 파일 누적 변환 완료 + 컴파일 성공

---

## 단계 10: 스레딩

- [ ] 학습 시작
- [ ] 이론 학습 완료
- [ ] 실습 완료
- [ ] 체크포인트 통과

### 선수 조건
- 단계 9 완료 필요

### 학습 목표
- Runnable 인터페이스 구현 (Coroutine 미사용)
- Thread 클래스 사용법
- 스레드 안전성 고려사항 이해

### 핵심 개념
- Runnable 구현: 클래스로 구현 또는 람다로 대체
- Thread 생성: `Thread(runnable).start()`
- `@Volatile`: 스레드 간 가시성 보장
- `synchronized`: 동기화 블록

### Java와 차이점
- Runnable 구현 시 클래스 상속보다 람다 권장
- synchronized는 함수나 블록에 `@Synchronized` 또는 `synchronized(lock) { }`
- volatile 필드는 `@Volatile` 어노테이션 사용

### Null Safety
- 멀티스레드 환경에서 nullable 처리
- 스레드 간 공유 데이터의 null 안전성
- `@Volatile`과 nullable의 조합

### 변환 실습

#### 파일 1: `LevelUpThread.java`
- **파일 특성**: Runnable 구현, 레벨업 처리
- **적용할 Kotlin 개념**: Runnable 구현, Thread 사용

#### 파일 2: `QuestThread.java`
- **파일 특성**: Runnable 구현, 퀘스트 완료 체크
- **적용할 Kotlin 개념**: Runnable, 컬렉션 접근, 조건문

#### 파일 3: `AchieveTitleThread.java`
- **파일 특성**: Runnable 구현, 칭호 획득 체크
- **적용할 Kotlin 개념**: Runnable, 컬렉션, 반복문

### 검증
- 매 파일 변환 후: `./gradlew compileKotlin compileJava`
- 단계 완료 후: 백그라운드 스레드 동작 확인

### 체크포인트
- [ ] Kotlin에서 Runnable을 구현하는 방법을 알고 있다
- [ ] Thread를 생성하고 시작하는 방법을 이해한다
- [ ] 40개 파일 누적 변환 완료 + 컴파일 성공

### 향후 학습 예고
> 현재 단계에서는 Java Thread 방식을 유지합니다.
> Kotlin Coroutine 학습 후 다음 파일들을 Coroutine 기반으로 리팩토링할 수 있습니다:
> - `LevelUpThread.kt` → `suspend fun` + `delay()` 적용
> - `QuestThread.kt` → `Flow` 또는 `Channel` 적용
> - `AchieveTitleThread.kt` → Coroutine 기반 비동기 처리

---

## 단계 11: 메인 애플리케이션

- [ ] 학습 시작
- [ ] 이론 학습 완료
- [ ] 실습 완료
- [ ] 체크포인트 통과

### 선수 조건
- 단계 10 완료 필요

### 학습 목표
- 전체 애플리케이션 통합
- Kotlin 진입점 (`main` 함수) 설정
- 전체 시스템 동작 검증

### 핵심 개념
- Kotlin main 함수: `fun main(args: Array<String>)` 또는 `fun main()`
- top-level 함수: 클래스 없이 파일에 직접 함수 정의
- `@JvmStatic`: Java에서 static 메서드로 호출 가능하게 함
- 패키지 구조와 import

### Java와 차이점
- main 함수가 클래스 안에 있을 필요 없음
- `public static void main(String[] args)`가 `fun main()`으로 간결해짐
- 클래스명 없이 함수 호출 가능 (top-level 함수)

### Null Safety
- 프로그램 시작 시점의 초기화 순서
- 전역 상태의 nullable 처리
- 종료 시 리소스 정리

### 변환 실습

#### 파일 1: `MyHome.java`
- **파일 특성**: 메인 게임 클래스, 게임 루프
- **적용할 Kotlin 개념**: 클래스 변환, 전체 시스템 통합
- **참고**: 모든 Area, Character, Quest 등 통합

#### 파일 2: `MyHomeTester.java`
- **파일 특성**: 테스트용 진입점
- **적용할 Kotlin 개념**: main 함수, 테스트 시나리오

#### 파일 3: `Main.java`
- **파일 특성**: 애플리케이션 진입점
- **적용할 Kotlin 개념**: top-level main 함수

### 검증
- 매 파일 변환 후: `./gradlew compileKotlin compileJava`
- **최종 검증**:
  1. `./gradlew run` 실행
  2. 게임 전체 기능 테스트
  3. Java 버전과 동일한 동작 확인

### 체크포인트
- [ ] Kotlin의 main 함수 문법을 이해한다
- [ ] top-level 함수의 개념을 알고 있다
- [ ] 43개 파일 전체 변환 완료
- [ ] `./gradlew compileKotlin compileJava` 성공
- [ ] `./gradlew run` 실행 시 게임 정상 동작
- [ ] Java 버전과 Kotlin 버전의 실행 결과가 동일

---

## Spring 교육 대비 핵심 체크리스트

다음 개념들은 Kotlin+Spring 교육에서 자주 등장하므로 반드시 숙지:

- [ ] **data class**: DTO, Entity, Request/Response 객체에 사용 (단계 1, 3, 6)
- [ ] **nullable 타입**: 요청 파라미터, DB nullable 컬럼 처리 (전 단계)
- [ ] **companion object**: 팩토리 메서드 패턴 (`of()`, `from()`) (단계 1, 2)
- [ ] **확장 함수**: 도메인 객체의 유틸리티 메서드 (단계 8)
- [ ] **람다와 고차함수**: 함수형 엔드포인트, 커스텀 로직 주입 (단계 6, 7)
- [ ] **스코프 함수**: 객체 초기화, 빌더 대체 (단계 6, 8)

---

## 변환 대상 파일 전체 목록 (43개)

### 단계별 변환 파일 매핑

| 단계 | 파일 | 누적 | 핵심 학습 포인트 |
|------|------|------|------------------|
| 1 | `MyHomeConstants.java` | 1 | object, const val |
| 1 | `ItemType.java` | 2 | enum class |
| 1 | `AreaType.java` | 3 | enum class |
| 1 | `TitleInfo.java` | 4 | data class |
| 2 | `MyHomeUtils.java` | 5 | object |
| 2 | `Item.java` | 6 | abstract class |
| 2 | `Character.java` | 7 | abstract class, open |
| 2 | `MiniGamePiece.java` | 8 | abstract class |
| 3 | `ItemEntry.java` | 9 | data class, nullable |
| 3 | `Inventory.java` | 10 | mutableListOf, safe call |
| 4 | `GrowthItem.java` | 11 | 상속, override |
| 4 | `StoreItem.java` | 12 | 상속, override |
| 4 | `CraftItem.java` | 13 | 상속, override |
| 4 | `Potion.java` | 14 | 상속, override |
| 5 | `NPC.java` | 15 | 상속, 보조 생성자 |
| 5 | `Player.java` | 16 | 복잡한 클래스 변환 |
| 5 | `Merchant.java` | 17 | 다단계 상속 |
| 6 | `QuestInfo.java` | 18 | data class |
| 6 | `Quest.java` | 19 | 클래스 변환 |
| 6 | `Title.java` | 20 | 클래스 변환 |
| 6 | `TitleStorage.java` | 21 | object, 컬렉션 |
| 6 | `QuestStorage.java` | 22 | object, apply, 람다 |
| 7 | `ItemStorage.java` | 23 | object, mapOf |
| 7 | `Bear.java` | 24 | 상속 |
| 7 | `Fish.java` | 25 | 상속 |
| 7 | `BearCatchesFishGame.java` | 26 | 2D 배열, when |
| 7 | `CultivateTimer.java` | 27 | Timer, 람다 |
| 7 | `StoreTimer.java` | 28 | Timer, 람다 |
| 7 | `ProgressBar.java` | 29 | repeat, 문자열 |
| 8 | `Area.java` | 30 | abstract class |
| 8 | `Farm.java` | 31 | 상속, 스코프 함수 |
| 8 | `AnimalFarm.java` | 32 | 상속 |
| 8 | `Forest.java` | 33 | 상속 |
| 8 | `CraftShop.java` | 34 | 상속 |
| 8 | `Arcade.java` | 35 | 상속, when, 게임 로직 |
| 9 | `SoundPlayerUsingClip.java` | 36 | Java 인터페이스 구현 |
| 9 | `SoundPlayerClipTest.java` | 37 | main 함수 |
| 10 | `LevelUpThread.java` | 38 | Runnable |
| 10 | `QuestThread.java` | 39 | Runnable, 컬렉션 |
| 10 | `AchieveTitleThread.java` | 40 | Runnable |
| 11 | `MyHome.java` | 41 | 전체 통합 |
| 11 | `MyHomeTester.java` | 42 | main, 테스트 |
| 11 | `Main.java` | 43 | main 진입점 |

---

## 학습 진행 상태 (Claude Code 멘토 업데이트 영역)

> ⚠️ 이 섹션은 Claude Code 멘토가 학습 진행 중 업데이트합니다.
> 학습자는 이 섹션을 통해 진행 상황을 확인할 수 있습니다.

### 현재 상태
- **진행 단계**: 단계 1 완료 ✅
- **변환 완료 파일**: 4/43
- **마지막 학습일**: 2025-01-27
- **다음 학습 예정**: 단계 2 - 베이스 클래스 (MyHomeUtils, Item, Character, MiniGamePiece)

### 세션 로그
| 날짜 | 시간 | 다룬 내용 | 완료 여부 |
|------|------|-----------|-----------|
| 2025-01-27 | - | 단계 1 전체 (object, enum class, data class) | ✅ 완료 |

### 학습 노트
> 멘토가 기록하는 학습 중 발견한 인사이트, 주의사항, 학습자 특성 등

- 학습자가 직접 코드를 먼저 작성해보는 스타일 (자기주도적)
- 패키지 구조 리팩토링 의지 있음 (etc→utils, item→enums 등)
- 공식문서를 직접 찾아보는 적극적인 학습 태도
- immutable 스타일에 대한 이해도 높음 (val 선호 원칙 질문)

### 변환된 파일 매핑
| 원본 Java | 변환된 Kotlin | 패키지 변경 |
|-----------|---------------|-------------|
| MyHomeConstants.java | MyHomeConstants.kt | etc → utils |
| ItemType.java | ItemType.kt | item → enums |
| AreaType.java | AreaType.kt | area → enums |
| TitleInfo.java | TitleDetail.kt (리네임) | quest → title |

### Q&A 기록
> 학습 중 나온 질문과 답변 기록

- **Q**: `const val`과 `val`의 차이?
  - **A**: `const val`은 컴파일 타임 상수 (primitive/String만, 인라인됨), `val`은 런타임 상수 (모든 타입, 함수 호출 가능)
- **Q**: Java/Kotlin 같은 패키지에 같은 클래스명이면?
  - **A**: Redeclaration 에러 발생. 변환 완료 후 Java 파일 삭제하거나 패키지 분리 필요
- **Q**: `data class`가 equals/hashCode/toString을 자동 생성하는 원리?
  - **A**: Kotlin의 모든 클래스는 `Any`를 상속. `data class`는 컴파일러가 자동으로 이 메서드들을 오버라이드하는 코드 생성
- **Q**: `var achieved`를 불변으로 개선하려면?
  - **A**: `val achieved`로 변경하고 `copy()` 메서드 활용 (data class 자동 생성)
