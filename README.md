# ⚽ MMRSR FC - 축구 시뮬레이션 백엔드 서버

## 📋 목차
- [주요 기능](#-주요-기능)
- [기술 스택](#-기술-스택)
- [시뮬레이션 로직](#-시뮬레이션-로직)
- [API 엔드포인트](#-api-엔드포인트)
- [데이터 모델](#-데이터-모델)

### 핵심 특징
- 🤖 **자동 경기 시뮬레이션**: 5초마다 자동으로 라운드 경기 진행
- 📊 **실시간 통계 관리**: 선수 개인 기록 및 팀 성적 자동 업데이트
- 🏆 **시즌 관리**: 30라운드 시즌 시스템, 이달의 선수 선정
- 🔐 **JWT 인증**: Spring Security 기반 보안 시스템
- 💾 **NoSQL 데이터베이스**: MongoDB와 Redis를 활용하여 실시간 시뮬레이션 결과 데이터 처리

## ✨ 주요 기능

### 1. 경기 시뮬레이션 시스템
- **라운드 로빈 방식**: 모든 팀이 공평하게 홈/어웨이 경기 진행
- **실시간 시뮬레이션**: 90분 경기를 분 단위로 시뮬레이션
- **확률 기반 이벤트**: 팀 오버롤에 따른 슈팅, 골, 코너킥 등 발생
- **포지션별 골 확률**: 공격수(55%), 미드필더(35%), 수비수(10%)

### 2. 선수 및 팀 관리
- 선수 개인 기록 추적 (골, 어시스트, 출전 경기)
- 팀 시즌별 성적 관리 (승/무/패, 득실차)
- 월간 베스트 선수 자동 선정
- 리그 포인트 및 순위 시스템

### 3. 사용자 인증 시스템
- JWT 기반 토큰 인증
- Access Token & Refresh Token
- BCrypt 비밀번호 암호화
- Spring Security 통합

## 🛠 기술 스택

### Backend Framework
- **Spring Boot 3.2.3** 
- **Spring Security**

### Database
- **MongoDB** - 메인 데이터 저장소 (팀, 선수, 시즌 데이터)
- **Redis** - 캐싱 및 토큰 관리

### Security & Authentication
- **JWT (JSON Web Token)** 
- **BCrypt** - 비밀번호 해싱

### Build Tool
- **Gradle** - 
- **Java 17** - 

## ⚙️ 시뮬레이션 로직

### 시뮬레이션 흐름

```
┌─────────────────────────────────────────────┐
│  @Scheduled(fixedRate = 5000)               │
│  매 5초마다 자동 실행                          │
└─────────────────┬───────────────────────────┘
                  │
                  ▼
        ┌──────────────────────┐
        │ 1. 데이터 준비         │
        │ - 팀 목록 조회         │
        │ - 현재 시즌 정보       │
        └──────────┬───────────┘
                   │
                   ▼
        ┌──────────────────────┐
        │ 2. 팀 매칭             │
        │ - 라운드 로빈 알고리즘 │
        │ - 홈/어웨이 결정       │
        └──────────┬───────────┘
                   │
                   ▼
        ┌──────────────────────┐
        │ 3. 경기 시뮬레이션     │
        │ - 90분 분단위 진행     │
        │ - 확률 기반 이벤트     │
        └──────────┬───────────┘
                   │
                   ▼
        ┌──────────────────────┐
        │ 4. 결과 저장           │
        │ - 경기 결과            │
        │ - 선수 통계            │
        │ - 팀 성적              │
        └──────────────────────┘
```

### 상세 시뮬레이션 알고리즘

#### 1. 팀 매칭 (라운드 로빈)
```java
// 총 팀수가 N일 때, (N-1) * 2 라운드 진행
// 홈/어웨이를 바꿔가며 모든 팀이 대결

홈팀 인덱스 = (라운드 % (팀수-1) + 매치번호) % (팀수-1)
어웨이팀 인덱스 = ((팀수-1) - 매치번호 + 라운드 % (팀수-1)) % (팀수-1)

// 후반 라운드(라운드 >= 팀수-1)에서는 홈/어웨이 전환
```

#### 2. 경기 시뮬레이션 (90분)

**홈 어드밴티지**
- 홈팀: 0.9 (유리)
- 어웨이팀: 1.1 (불리)

**팀 성능 비율 계산**
```java
ratio = advantage × (-0.002 × teamOverall + 1.2)
```

**매 분당 이벤트 확률**

| 이벤트 | 기본 확률 | 조건 |
|--------|-----------|------|
| 코너킥 | 10% | 무조건 |
| 슈팅 | 20% | ratio 적용 |
| 유효 슈팅 | 30% | 슈팅 발생 시 + ratio |
| 골 | 40% | 유효 슈팅 발생 시 + ratio |

**골 스코어러 결정 확률**
```
공격수(FW): 55%
미드필더(MF): 35% (누적 90%)
수비수(DF): 10%
```

#### 3. 점유율 계산
```java
// 90분 동안 팀 오버롤 비율로 점유율 결정
홈팀 점유율 + 어웨이 점유율 = 100%

// 예시: 홈팀 오버롤 80, 어웨이팀 오버롤 70
// 홈팀 점유율 ≈ 53%, 어웨이팀 점유율 ≈ 47%
```

#### 4. 통계 자동 업데이트

**경기 종료 후 자동 처리**
- ✅ 선수 개인 기록 업데이트 (골, 어시스트)
- ✅ 팀 시즌 성적 업데이트 (승/무/패, 득실점)
- ✅ 4라운드마다 이달의 선수 선정
- ✅ 30라운드 완료 시 시즌 종료 및 초기화

**이달의 선수 선정 공식**
```java
monthlyScore = (monthlyGoal × 10) + (monthlyAssists × 5)
// 상위 11명 선정
```

### 시뮬레이션 상수

```java
// 경기 설정
MATCH_DURATION_MINUTES = 90
MAX_POSSESSION_PERCENTAGE = 100

// 어드밴티지
HOME_ADVANTAGE = 0.9
AWAY_ADVANTAGE = 1.1

// 확률
CORNER_KICK_PROBABILITY = 0.10
SHOOTING_PROBABILITY = 0.20
EFFECTIVE_SHOT_PROBABILITY = 0.30
GOAL_PROBABILITY = 0.40

// 포지션별 골 확률
STRIKER_GOAL_PROBABILITY = 0.55
MIDFIELDER_GOAL_PROBABILITY = 0.90  // 누적
DEFENDER_GOAL_PROBABILITY = 1.00     // 나머지
```

## 📡 API 

### 인증 (Auth)
- `POST /api/auth/signin` - 로그인
- `POST /api/auth/signup` - 회원가입
- `POST /api/auth/refresh` - 토큰 갱신
- `POST /api/auth/logout` - 로그아웃

### 팀 관리 (Team)
- `GET /api/teams` - 모든 팀 조회
- `GET /api/teams/{id}` - 특정 팀 조회
- `POST /api/teams` - 팀 생성
- `PUT /api/teams/{id}` - 팀 정보 수정
- `DELETE /api/teams/{id}` - 팀 삭제

### 선수 관리 (Player)
- `GET /api/players` - 모든 선수 조회
- `GET /api/players/{id}` - 특정 선수 조회
- `GET /api/players/team/{teamId}` - 팀별 선수 조회
- `POST /api/players` - 선수 등록
- `PUT /api/players/{id}` - 선수 정보 수정
- `DELETE /api/players/{id}` - 선수 삭제

### 경기 관리 (Match)
- `GET /api/matches` - 경기 목록 조회
- `GET /api/matches/{id}` - 특정 경기 조회
- `GET /api/matches/season/{seasonId}` - 시즌별 경기 조회
- `GET /api/matches/round/{roundNumber}` - 라운드별 경기 조회

### 사용자 관리 (User)
- `GET /api/users/me` - 내 정보 조회
- `PUT /api/users/me` - 내 정보 수정

## 💾 데이터 모델

### Team (팀)
```json
{
  "_id": "ObjectId",
  "clubName": "string",
  "homeTown": "string",
  "director": "string",
  "owner": "string",
  "homeStadium": "string",
  "players": [PlayerInTeam],
  "awards": {
    "league": "number",
    "cup": "number"
  },
  "seasons": [SeasonInTeam],
  "rivalTeam": ["string"],
  "views": "number",
  "leaguePoint": "number",
  "teamOverall": "number"
}
```

### Player (선수)
```json
{
  "_id": "ObjectId",
  "name": "string",
  "position": "number",
  "salary": "number",
  "number": "number",
  "mainFoot": "string",
  "age": "number",
  "assertion": "boolean",
  "goal": "number",
  "assist": "number",
  "totalGamesPlayed": "number",
  "totalGoalsScored": "number",
  "totalAssists": "number",
  "teamId": "string",
  "monthlyGoal": "number",
  "monthlyAssists": "number",
  "overall": "number"
}
```

### Match (경기)
```json
{
  "date": "string",
  "stadium": "string",
  "homeTeam": {
    "teamId": "string",
    "clubName": "string",
    "goals": [Goal],
    "shots": "number",
    "effectiveShots": "number",
    "cornerKicks": "number",
    "possession": "number"
  },
  "awayTeam": {
    // homeTeam과 동일 구조
  }
}
```

### Goal (골)
```json
{
  "minute": "number",
  "goalPlayerId": "string",
  "goalPlayerName": "string",
  "assistPlayerId": "string",
  "assistPlayerName": "string"
}
```

### Season (시즌)
```json
{
  "_id": "ObjectId",
  "season": "number",
  "roundList": [Round],
  "roundCount": "number"
}
```

