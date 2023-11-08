package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
public class ItemRequestDto {
    long id;
    String description;
    @JsonProperty("requestor") @NotNull long userId;
    @JsonProperty("created") LocalDateTime creationDate;
}