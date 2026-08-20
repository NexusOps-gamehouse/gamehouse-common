package gg.duo.common.constant;

/** 주로 게임하는 시간대. 설문·모집 조건·매칭 점수가 같은 값을 쓴다. */
public enum PlayTime {
    MORNING("아침"),
    AFTERNOON("낮"),
    EVENING("저녁"),
    DAWN("새벽");

    private final String displayName;

    PlayTime(String displayName) { this.displayName = displayName; }

    public String displayName() { return displayName; }
}
