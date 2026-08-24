package gg.duo.common.dto;

/**
 * 서비스 경계를 넘어 다니는 사용자 표현.
 *
 * user 서비스가 소유한 users 테이블의 "공개 뷰"다. post·chat 은 User 엔티티를
 * 볼 수 없으므로 UserClient 를 통해 이 형태로만 받는다.
 *
 * 엔티티를 import 하지 않는다. 여기에 @Entity 나 Repository 가 들어오면 모든
 * 서비스가 같은 테이블을 직접 만지게 되고, 스키마를 바꿀 때마다 전체를 동시
 * 배포해야 한다. 그게 분산 모놀리스다.
 *
 * 필드 구성은 기존 UserDto 와 동일하다 — 프론트 응답 형태를 바꾸지 않기 위해서다.
 */
public record UserDto(
        Long id,
        String email,
        String nickname,
        String profileImageUrl,
        /** [FR-01] 나이대 구간 대신 숫자 나이. 미입력이면 null. */
        Integer age,
        String game,
        String playStyle,
        String position,
        boolean mic,
        /** 사용자가 설문에서 직접 고른 티어. 한글 값이다. ("다이아몬드") */
        String tier,
        /** 라이엇에서 확인된 티어. 영문 enum 이다. ("DIAMOND") 미연동이면 null. */
        String riotTier,
        String riotRank,
        String playTimes,
        String playDays,
        /** 1회 플레이 선호 분량. ("2~4시간") */
        String playDuration,
        String gameModes,
        String riotNickname,
        String puuid,
        String gameName,
        String tagLine,
        boolean online
) {}
