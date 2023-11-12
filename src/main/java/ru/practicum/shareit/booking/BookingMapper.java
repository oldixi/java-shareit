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
                .bookingStartDate(booking.getBookingStartDate())
                .bookingEndDate(booking.getBookingEndDate())
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
                .bookingStartDate(bookingDto.getBookingStartDate())
                .bookingEndDate(bookingDto.getBookingEndDate())
                .status(bookingDto.getStatus())
                .user(user)
                .item(item)
                .build();
    }

    public static Booking toBooking(BookingDto bookingDto, User user, Item item) {
        return Booking.builder()
                .bookingStartDate(bookingDto.getBookingStartDate())
                .bookingEndDate(bookingDto.getBookingEndDate())
                .status(bookingDto.getStatus())
                .user(user)
                .item(item)
                .build();
    }

    public static Booking toBooking(Booking booking, BookingDto bookingDto, User user, Item item) {
        bookingDto.setBookingEndDate(bookingDto.getBookingEndDate() != null ?
                bookingDto.getBookingEndDate() :
                booking.getBookingEndDate());
        bookingDto.setBookingStartDate(bookingDto.getBookingStartDate() != null ?
                bookingDto.getBookingStartDate() :
                booking.getBookingStartDate());
        bookingDto.setStatus(bookingDto.getStatus() != null ?
                bookingDto.getStatus() :
                booking.getStatus());
        return Booking.builder()
                .bookingStartDate(bookingDto.getBookingStartDate())
                .bookingEndDate(bookingDto.getBookingEndDate())
                .status(bookingDto.getStatus())
                .user(user)
                .item(item)
                .build();
    }
}