package gg.duo.common.constant;

/** 주로 게임하는 요일. */
public enum PlayDay {
    MON("월"), TUE("화"), WED("수"), THU("목"), FRI("금"), SAT("토"), SUN("일");

    private final String displayName;

    PlayDay(String displayName) { this.displayName = displayName; }

    public String displayName() { return displayName; }
}
