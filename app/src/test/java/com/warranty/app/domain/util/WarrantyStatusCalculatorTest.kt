package com.warranty.app.domain.util

import com.google.common.truth.Truth.assertThat
import com.warranty.app.domain.model.WarrantyItem
import com.warranty.app.domain.model.WarrantyStatus
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Test

class WarrantyStatusCalculatorTest {

    private val clock = Clock.fixed(Instant.parse("2024-08-01T00:00:00Z"), ZoneOffset.UTC)
    private val calculator = WarrantyStatusCalculator(clock)

    @Test
    fun ctive when expiration is in future beyond 30 days() {
        val item = WarrantyItem(
            id = 1,
            name = "Laptop",
            purchaseDate = LocalDate.of(2024, 1, 1),
            expirationDate = LocalDate.of(2024, 10, 1)
        )

        val result = calculator.compute(item)

        assertThat(result.status).isEqualTo(WarrantyStatus.ACTIVE)
        assertThat(result.daysRemaining).isEqualTo(62)
    }

    @Test
    fun expiring soon when within 30 days() {
        val item = WarrantyItem(
            id = 1,
            name = "Camera",
            purchaseDate = LocalDate.of(2024, 7, 1),
            expirationDate = LocalDate.of(2024, 8, 15)
        )

        val result = calculator.compute(item)

        assertThat(result.status).isEqualTo(WarrantyStatus.EXPIRING_SOON)
        assertThat(result.daysRemaining).isEqualTo(14)
    }

    @Test
    fun expired when date passed() {
        val item = WarrantyItem(
            id = 1,
            name = "Headphones",
            purchaseDate = LocalDate.of(2023, 1, 1),
            expirationDate = LocalDate.of(2024, 7, 1)
        )

        val result = calculator.compute(item)

        assertThat(result.status).isEqualTo(WarrantyStatus.EXPIRED)
        assertThat(result.daysRemaining).isEqualTo(-31)
    }

    @Test
    fun duration months fallback when expiration null() {
        val item = WarrantyItem(
            id = 1,
            name = "Phone",
            purchaseDate = LocalDate.of(2024, 1, 15),
            durationMonths = 12
        )

        val result = calculator.compute(item)

        assertThat(result.status).isEqualTo(WarrantyStatus.ACTIVE)
        assertThat(result.daysRemaining).isEqualTo(167)
    }
}
