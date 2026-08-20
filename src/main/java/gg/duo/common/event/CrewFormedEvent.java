package gg.duo.common.event;

import java.util.List;

/** 크루가 결성됐다. crew 서비스가 생기면 발행한다. (계약만 먼저 둔다) */
public record CrewFormedEvent(Long crewId, String crewName, List<Long> memberIds)
        implements DomainEvent {}
