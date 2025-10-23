package com.evdealer.evdealermanagement.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> {
    List<T> items;
    int page;
    int size;
    long totalElements;
    int totalPages;
    boolean hasPreviousPage;
    boolean hasNextPage;

    //hàm dùng để chuyển đổi từ Page<> sang WishlistPageResponse<>
    //E is Entity in DB
    //R is kiểu DTO response
    public static <E, R> PageResponse<R> fromPage(Page<E> page, Function<E, R> mapper) {
        return PageResponse.<R>builder()
                .items(page.map(mapper).getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasPreviousPage(page.hasPrevious())
                .hasNextPage(page.hasNext())
                .build();

    }

    public static <R> PageResponse<R> of(List<R> items, int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil( (double) totalElements/ (double) size);
        return PageResponse.<R>builder()
                .items(items)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasPreviousPage(page > 0)
                .hasNextPage((page + 1) < totalPages)
                .build();
    }

    public static <T> PageResponse<T> of(List<T> content, Page<?> page) {
        return PageResponse.<T>builder()
                .items(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasPreviousPage(page.hasPrevious())
                .hasNextPage(page.hasNext())
                .build();
    }
}
