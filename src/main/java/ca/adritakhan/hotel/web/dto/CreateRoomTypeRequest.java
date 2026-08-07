package ca.adritakhan.hotel.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateRoomTypeRequest(
        @NotBlank String name,
        @NotBlank String description
) {
}
