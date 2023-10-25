package ru.practicum.shareit.booking;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

@Data
public class Booking {
    long id;
    @JsonProperty("start") LocalDateTime bookingStartDate;
    @JsonProperty("end") LocalDateTime bookingEndDate;
    Item itemId;
    User booker;
    BookingStatus status;
}