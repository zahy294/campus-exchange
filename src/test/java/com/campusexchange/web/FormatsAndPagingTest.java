package com.campusexchange.web;

import com.campusexchange.service.Paging;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class FormatsAndPagingTest {

    private final Formats fmt = new Formats();

    @Test
    void rupeesUseIndianGrouping() {
        assertThat(fmt.inr(new BigDecimal("0"))).isEqualTo("₹0");
        assertThat(fmt.inr(new BigDecimal("950.00"))).isEqualTo("₹950");
        assertThat(fmt.inr(new BigDecimal("4500"))).isEqualTo("₹4,500");
        assertThat(fmt.inr(new BigDecimal("28000.00"))).isEqualTo("₹28,000");
        assertThat(fmt.inr(new BigDecimal("125000.5"))).isEqualTo("₹1,25,000.50");
        assertThat(fmt.inr(new BigDecimal("12345678.99"))).isEqualTo("₹1,23,45,678.99");
        assertThat(fmt.inr(null)).isEmpty();
    }

    @Test
    void initialsFromName() {
        assertThat(fmt.initials("Aarav Patel")).isEqualTo("AP");
        assertThat(fmt.initials("priya")).isEqualTo("P");
        assertThat(fmt.initials("  ")).isEqualTo("?");
    }

    @Test
    void pagingClampsBadInput() {
        Pageable p = Paging.of(-4, 100000, "price_asc");
        assertThat(p.getPageNumber()).isZero();
        assertThat(p.getPageSize()).isEqualTo(Paging.MAX_PAGE_SIZE);
        assertThat(p.getSort().getOrderFor("price")).isNotNull();

        assertThat(Paging.of(0, 0, null).getPageSize()).isEqualTo(1);
        assertThat(Paging.of(0, 10, "garbage").getSort().getOrderFor("createdAt").isDescending()).isTrue();
    }
}
