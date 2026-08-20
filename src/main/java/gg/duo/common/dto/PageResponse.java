package gg.duo.common.dto;

import java.util.List;

/**
 * 페이징 응답.
 *
 * Spring 의 Page 를 그대로 직렬화하면 pageable/sort 같은 내부 구조가 응답에
 * 노출되고 프론트가 그 형태에 묶인다. 화면에 필요한 것만 담는다.
 */
public record PageResponse<T>(
        List<T> items,
        int page,
        int size,
        long totalElements,
        boolean hasNext
) {}
