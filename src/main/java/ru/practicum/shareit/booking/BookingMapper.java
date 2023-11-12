package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.util.List;
import java.util.stream.Collectors;

public class BookingMapper {
    public static BookingDto toBookingDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        return BookingDto.builder()
                .id(booking.getId())
                .startDate(booking.getStartDate())
                .endDate(booking.getEndDate())
                .status(booking.getStatus())
                .itemId(booking.getItem() != null ? booking.getItem().getId() : null)
                .userId(booking.getUser() != null ? booking.getUser().getId() : null)
                .build();
    }

    public static List<BookingDto> toBookingDto(List<Booking> bookings) {
        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    public static Booking toBooking(long bookingId, BookingDto bookingDto, User user, Item item) {
        return Booking.builder()
                .id(bookingId)
                .startDate(bookingDto.getStartDate())
                .endDate(bookingDto.getEndDate())
                .status(bookingDto.getStatus())
                .user(user)
                .item(item)
                .build();
    }

    public static Booking toBooking(BookingDto bookingDto, User user, Item item) {
        return Booking.builder()
                .startDate(bookingDto.getStartDate())
                .endDate(bookingDto.getEndDate())
                .status(bookingDto.getStatus())
                .user(user)
                .item(item)
                .build();
    }

    public static Booking toBooking(Booking booking, BookingDto bookingDto, User user, Item item) {
        bookingDto.setEndDate(bookingDto.getEndDate() != null ?
                bookingDto.getEndDate() :
                booking.getEndDate());
        bookingDto.setStartDate(bookingDto.getStartDate() != null ?
                bookingDto.getStartDate() :
                booking.getStartDate());
        bookingDto.setStatus(bookingDto.getStatus() != null ?
                bookingDto.getStatus() :
                booking.getStatus());
        return Booking.builder()
                .startDate(bookingDto.getStartDate())
                .endDate(bookingDto.getEndDate())
                .status(bookingDto.getStatus())
                .user(user)
                .item(item)
                .build();
    }
}