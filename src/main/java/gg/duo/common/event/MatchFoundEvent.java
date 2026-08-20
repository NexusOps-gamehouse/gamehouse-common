package gg.duo.common.event;

import java.util.List;

/** 매칭이 성사됐다. match 서비스가 생기면 발행한다. (계약만 먼저 둔다) */
public record MatchFoundEvent(Long matchId, String gameCode, List<Long> memberIds)
        implements DomainEvent {}
