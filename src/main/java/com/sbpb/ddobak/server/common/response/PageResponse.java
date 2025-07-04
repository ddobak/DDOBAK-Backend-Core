package com.sbpb.ddobak.server.common.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 페이징된 데이터 응답을 위한 클래스
 * Spring Data의 Page 객체를 클라이언트 친화적인 형태로 변환
 * 
 * JSON 응답 형식:
 * {
 * "content": [...],
 * "page_info": {
 * "current_page": 0,
 * "page_size": 20,
 * "total_elements": 100,
 * "total_pages": 5,
 * "has_next": true,
 * "has_previous": false
 * }
 * }
 */
@Getter
@Setter
public class PageResponse<T> {

    private List<T> content;
    private PageInfo pageInfo;

    // 기본 생성자
    public PageResponse() {
    }

    // 전체 필드 생성자
    public PageResponse(List<T> content, PageInfo pageInfo) {
        this.content = content;
        this.pageInfo = pageInfo;
    }

    /**
     * Spring Data Page 객체에서 PageResponse로 변환
     * 
     * @param page Spring Data Page 객체
     * @return PageResponse 객체
     */
    public static <T> PageResponse<T> from(org.springframework.data.domain.Page<T> page) {
        PageInfo pageInfo = new PageInfo(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious());

        return new PageResponse<>(page.getContent(), pageInfo);
    }

    /**
     * 직접 생성하는 팩토리 메서드
     */
    public static <T> PageResponse<T> of(List<T> content, int currentPage, int pageSize,
            long totalElements, int totalPages,
            boolean hasNext, boolean hasPrevious) {
        PageInfo pageInfo = new PageInfo(currentPage, pageSize, totalElements,
                totalPages, hasNext, hasPrevious);
        return new PageResponse<>(content, pageInfo);
    }

    // Getter/Setter 메서드들은 Lombok으로 자동 생성됨

    /**
     * 페이지 정보를 담는 내부 클래스
     */
    @Getter
    @Setter
    public static class PageInfo {

        private int currentPage;
        private int pageSize;
        private long totalElements;
        private int totalPages;
        private boolean hasNext;
        private boolean hasPrevious;

        // 기본 생성자
        public PageInfo() {
        }

        // 전체 필드 생성자
        public PageInfo(int currentPage, int pageSize, long totalElements,
                int totalPages, boolean hasNext, boolean hasPrevious) {
            this.currentPage = currentPage;
            this.pageSize = pageSize;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.hasNext = hasNext;
            this.hasPrevious = hasPrevious;
        }

        // Getter/Setter 메서드들은 Lombok으로 자동 생성됨

        @Override
        public String toString() {
            return String.format("PageInfo{currentPage=%d, pageSize=%d, totalElements=%d, totalPages=%d}",
                    currentPage, pageSize, totalElements, totalPages);
        }
    }

    @Override
    public String toString() {
        return String.format("PageResponse{contentSize=%d, pageInfo=%s}",
                content != null ? content.size() : 0, pageInfo);
    }
}