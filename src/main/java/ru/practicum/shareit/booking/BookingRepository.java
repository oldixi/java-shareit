package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByUserIdAndId(long userId, long bookingId);

    Optional<Booking> findTopByItemIdAndBookingStartDateBeforeOrderByBookingStartDateDesc(long itemId, LocalDateTime now);

    Optional<Booking> findTopByItemIdAndBookingStartDateAfterAndStatusInOrderByBookingStartDateAsc(long itemId,
                                                                                                   LocalDateTime now,
                                                                                                   List<BookingStatus> state);

    List<Booking> findByUserIdOrderByBookingStartDateDesc(long userId);

    List<Booking> findByUserIdAndStatusIsOrderByBookingStartDateDesc(long userId, BookingStatus state);

    List<Booking> findByUserIdAndStatusIsAndBookingEndDateBeforeOrderByBookingStartDateDesc(long userId,
                                                                                            BookingStatus state,
                                                                                            LocalDateTime nowDate);

    List<Booking> findByUserIdAndBookingStartDateAfterOrderByBookingStartDateDesc(long userId,
                                                                                  LocalDateTime nowDate);

    List<Booking> findByUserIdAndBookingEndDateBeforeOrderByBookingStartDateDesc(long userId,
                                                                                 LocalDateTime nowDate);

    List<Booking> findByUserIdAndBookingEndDateAfterAndBookingStartDateBeforeOrderByIdAsc(long userId,
                                                                                          LocalDateTime endNowDate,
                                                                                          LocalDateTime startNowDate);

    List<Booking>  findByItemUserIdOrderByBookingStartDateDesc(long userId);

    List<Booking> findByItemUserIdAndStatusIsOrderByBookingStartDateDesc(long userId, BookingStatus state);

    List<Booking> findByItemUserIdAndBookingStartDateAfterOrderByBookingStartDateDesc(long userId,
                                                                                      LocalDateTime nowDate);

    List<Booking> findByItemUserIdAndBookingEndDateBeforeOrderByBookingStartDateDesc(long userId,
                                                                                     LocalDateTime nowDate);

    List<Booking> findByItemUserIdAndBookingEndDateAfterAndBookingStartDateBeforeOrderByIdAsc(long userId,
                                                                                              LocalDateTime endNowDate,
                                                                                              LocalDateTime startNowDate);
}