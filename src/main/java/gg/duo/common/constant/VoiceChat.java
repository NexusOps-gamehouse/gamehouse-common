package gg.duo.common.constant;

/**
 * 모집글이 요구하는 음성채팅 수준.
 *
 * 예전에는 posts.mic_required 라는 boolean 하나였다. 그런데 "마이크는 있는데
 * 말은 별로 안 하고 싶다"가 실제로 가장 흔한 상태고, boolean 은 그걸 표현하지
 * 못해 사람들이 '필수'와 '무관' 사이에서 아무거나 골랐다. 그 값으로는 필터를
 * 만들 수 없다.
 *
 * FR-02 의 '음성채팅 정도'가 공통 항목으로 올라오면서 마이크 여부를 대체한다.
 * (둘을 같이 두면 "마이크 필수 + 음성 상관없음" 같은 모순된 조합이 저장된다)
 *
 * REQUIRED  : 하드 필터. 음성 못 하는 사람은 추천에서 제외한다.
 * PREFERRED : 소프트 점수. 음성 되는 사람에게 가산점만 준다.
 * ANY       : 조건 없음.
 */
public enum VoiceChat {
    REQUIRED("필수"),
    PREFERRED("있으면 좋음"),
    ANY("상관없음");

    private final String displayName;

    VoiceChat(String displayName) { this.displayName = displayName; }

    public String displayName() { return displayName; }
}
