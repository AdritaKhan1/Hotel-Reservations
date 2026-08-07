package ca.adritakhan.hotel.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record OpenInventoryRequest(
        @NotNull LocalDate from,
        @NotNull LocalDate to,
        @Min(1) int totalRooms
) {
}
