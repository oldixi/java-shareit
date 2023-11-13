package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByUserIdAndId(long userId, long bookingId);

    Optional<Booking> findTopByItemIdAndStartDateBeforeOrderByStartDateDesc(long itemId, LocalDateTime now);

    Optional<Booking> findTopByItemIdAndStartDateAfterAndStatusInOrderByStartDateAsc(long itemId,
                                                                                     LocalDateTime now,
                                                                                     List<BookingStatus> state);

    List<Booking> findByUserIdOrderByStartDateDesc(long userId);

    List<Booking> findByUserIdAndStatusIsOrderByStartDateDesc(long userId, BookingStatus state);

    List<Booking> findByUserIdAndStatusIsAndEndDateBeforeOrderByStartDateDesc(long userId,
                                                                              BookingStatus state,
                                                                              LocalDateTime nowDate);

    List<Booking> findByUserIdAndStartDateAfterOrderByStartDateDesc(long userId, LocalDateTime nowDate);

    List<Booking> findByUserIdAndEndDateBeforeOrderByStartDateDesc(long userId, LocalDateTime nowDate);

    List<Booking> findByUserIdAndEndDateAfterAndStartDateBeforeOrderByIdAsc(long userId,
                                                                            LocalDateTime endNowDate,
                                                                            LocalDateTime startNowDate);

    List<Booking> findByItemUserIdOrderByStartDateDesc(long userId);

    List<Booking> findByItemUserIdAndStatusIsOrderByStartDateDesc(long userId, BookingStatus state);

    List<Booking> findByItemUserIdAndStartDateAfterOrderByStartDateDesc(long userId, LocalDateTime nowDate);

    List<Booking> findByItemUserIdAndEndDateBeforeOrderByStartDateDesc(long userId, LocalDateTime nowDate);

    List<Booking> findByItemUserIdAndEndDateAfterAndStartDateBeforeOrderByIdAsc(long userId,
                                                                                LocalDateTime endNowDate,
                                                                                LocalDateTime startNowDate);
}