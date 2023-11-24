package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingDto;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemDtoWithBookingInfo {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private BookingDto lastBooking;
    private BookingDto nextBooking;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemDtoWithBookingInfo)) return false;
        return id != null && id.equals(((ItemDtoWithBookingInfo) o).getId());
    }
}