package com.andanza.backend.common;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

class PageParamsTest {

    @Test
    void usesTheFirstPageAndTwentyElementsByDefault() {
        Pageable pageable = new PageParams(null, null).toPageable(Sort.unsorted());

        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(20);
    }

    @Test
    void respectsTheRequestedPageAndSize() {
        Pageable pageable = new PageParams(3, 50).toPageable(Sort.by("createdAt"));

        assertThat(pageable.getPageNumber()).isEqualTo(3);
        assertThat(pageable.getPageSize()).isEqualTo(50);
        assertThat(pageable.getSort()).isEqualTo(Sort.by("createdAt"));
    }
}
