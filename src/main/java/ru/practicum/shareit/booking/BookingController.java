package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingDto;

import javax.validation.Valid;
import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public Booking addBookingRequest(@RequestHeader("X-Sharer-User-Id") long userId,
                                     @Valid @RequestBody BookingDto bookingDto) {
        log.info("Request for booking item {} from user {}", bookingDto.getItemId(), userId);
        return bookingService.createBooking(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public Booking updateItem(@RequestHeader("X-Sharer-User-Id") long userId,
                           @PathVariable long bookingId,
                           @RequestParam boolean approved) {
        if (approved) {
            log.info("Request for approving booking request {} from user {}",
                    bookingId, userId);
        } else {
            log.info("Request for rejection booking request {} from user {}",
                    bookingId, userId);
        }
        return bookingService.updateBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public Booking getBookingById(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable long bookingId) {
        log.info("Request for get booking request {} from user {}", bookingId, userId);
        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping
    public List<Booking> getBookingsByState(@RequestHeader("X-Sharer-User-Id") long userId,
                                               @RequestParam(required = false) String state) {
        log.info("Request for get bookings in state {} from user {}", state, userId);
        return bookingService.getBookingByState(userId, state);
    }

    @GetMapping("/owner")
    public List<Booking> getBookingsByOwnerAndState(@RequestHeader("X-Sharer-User-Id") long userId,
                                                       @RequestParam(required = false) String state) {
        log.info("Request for get bookings of user {} in state {}", userId, state);
        return bookingService.getBookingsByOwnerAndState(userId, state);
    }
}