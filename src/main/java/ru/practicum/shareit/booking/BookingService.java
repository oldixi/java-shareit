package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {
    Booking createBooking(long userId, BookingDto bookingDto);

    Booking updateBooking(long userId, long bookingId, boolean approved);

    Booking getBookingById(long userId, long bookingId);

    List<Booking> getBookingByState(long userId, String state);

    List<Booking> getBookingsByOwnerAndState(long userId, String state);

    Booking getLastBookingByItemId(long itemId);

    Booking getNextBookingByItemId(long itemId);
}