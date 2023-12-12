package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ItemRequestDtoWithItems {
    long id;
    String description;

    @JsonProperty("requestor")
    @NotNull
    long userId;

    @JsonProperty("created")
    LocalDateTime creationDate;

    List<ItemDto> items;
}