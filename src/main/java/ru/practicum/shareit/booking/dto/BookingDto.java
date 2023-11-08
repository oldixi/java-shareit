package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.BookingStatus;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
public class BookingDto {
    long id;
    @JsonProperty("start") LocalDateTime bookingStartDate;
    @JsonProperty("end") LocalDateTime bookingEndDate;
    @JsonProperty("item") @NotNull long itemId;
    @JsonProperty("booker") @NotNull long userId;
    BookingStatus status;
}