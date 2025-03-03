package com.backend.gjejpune.demo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.backend.gjejpune.demo.payload.response.PagedResponse;

import java.util.List;

/**
 * Utility class for pagination operations
 */
public class PaginationUtils {
    
    private static final int MAX_PAGE_SIZE = 30;
    
    /**
     * Create a pageable object with validation for page size
     * 
     * @param page Page number (0-based)
     * @param size Page size
     * @param sortBy Field to sort by
     * @param sortDirection Sort direction (asc or desc)
     * @return Pageable object
     */
    public static Pageable createPageable(int page, int size, String sortBy, String sortDirection) {
        // Validate and limit page size
        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }
        
        Sort sort = Sort.by(sortBy);
        if ("desc".equalsIgnoreCase(sortDirection)) {
            sort = sort.descending();
        } else {
            sort = sort.ascending();
        }
        
        return PageRequest.of(page, size, sort);
    }
    
    /**
     * Create a pageable object with default sort by createdAt descending
     * 
     * @param page Page number (0-based)
     * @param size Page size
     * @return Pageable object
     */
    public static Pageable createPageable(int page, int size) {
        return createPageable(page, size, "createdAt", "desc");
    }
    
    /**
     * Create a paged response from a page object
     * 
     * @param <T> Type of content
     * @param content Content list
     * @param page Page object
     * @param baseUrl Base URL for next page
     * @return PagedResponse object
     */
    public static <T> PagedResponse<T> createPagedResponse(List<T> content, Page<?> page, String baseUrl) {
        String nextPageUrl = null;
        if (!page.isLast()) {
            nextPageUrl = baseUrl + "?page=" + (page.getNumber() + 1) + "&size=" + page.getSize();
        }
        
        return new PagedResponse<>(
                content,
                page.getNumber(),
                content.size(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast(),
                nextPageUrl
        );
    }
} 