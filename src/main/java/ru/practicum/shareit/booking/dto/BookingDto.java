package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingDto {
    long id;
    @JsonProperty("start") LocalDateTime bookingStartDate;
    @JsonProperty("end") LocalDateTime bookingEndDate;
    @JsonProperty("itemId") Long itemId;
    @JsonProperty("bookerId") Long userId;
    BookingStatus status;
}