package ru.practicum.shareit_gateway.request.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
public class ItemRequestDto {
    long id;

    @NotNull
    @NotEmpty
    @NotBlank
    String description;

    @JsonProperty("requestor")
    @NotNull
    long userId;

    @JsonProperty("created")
    LocalDateTime creationDate;
}