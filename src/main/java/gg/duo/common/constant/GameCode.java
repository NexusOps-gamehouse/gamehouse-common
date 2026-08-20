package gg.duo.common.constant;

/**
 * 지원 게임.
 *
 * 문자열 대신 코드로 두는 이유: 이 값이 user(게임별 프로필), post(모집 조건),
 * match(추천) 세 서비스를 넘어 다닌다. 한쪽이 "LoL", 다른 쪽이 "lol" 을 쓰면
 * 이벤트가 조용히 매칭에 실패한다.
 */
public enum GameCode {
    LOL("리그 오브 레전드"),
    VALORANT("발로란트");

    private final String displayName;

    GameCode(String displayName) { this.displayName = displayName; }

    public String displayName() { return displayName; }
}
