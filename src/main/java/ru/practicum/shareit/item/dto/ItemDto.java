package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    @JsonProperty("request") private Long requestId;
}