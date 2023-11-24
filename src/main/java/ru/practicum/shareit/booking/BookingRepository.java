package ru.practicum.shareit.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByItemUserIdAndId(long userId, long bookingId);

    Optional<Booking> findTopByItemIdAndStartDateBeforeOrderByStartDateDesc(long itemId, LocalDateTime now);

    Optional<Booking> findTopByItemIdAndStartDateAfterAndStatusInOrderByStartDateAsc(long itemId,
                                                                                     LocalDateTime now,
                                                                                     List<BookingStatus> state);

    List<Booking> findByUserIdOrderByStartDateDesc(long userId);

    Page<Booking> findByUserIdOrderByStartDateDesc(long userId, Pageable page);

    List<Booking> findByUserIdAndStatusIsOrderByStartDateDesc(long userId, BookingStatus state);

    Page<Booking> findByUserIdAndStatusIsOrderByStartDateDesc(long userId, BookingStatus state, Pageable page);

    List<Booking> findByUserIdAndStatusIsAndEndDateBeforeOrderByStartDateDesc(long userId,
                                                                              BookingStatus state,
                                                                              LocalDateTime nowDate);

    List<Booking> findByUserIdAndStartDateAfterOrderByStartDateDesc(long userId, LocalDateTime nowDate);

    Page<Booking> findByUserIdAndStartDateAfterOrderByStartDateDesc(long userId, LocalDateTime nowDate, Pageable page);

    List<Booking> findByUserIdAndEndDateBeforeOrderByStartDateDesc(long userId, LocalDateTime nowDate);

    Page<Booking> findByUserIdAndEndDateBeforeOrderByStartDateDesc(long userId, LocalDateTime nowDate, Pageable page);

    List<Booking> findByUserIdAndEndDateAfterAndStartDateBeforeOrderByIdAsc(long userId,
                                                                            LocalDateTime endNowDate,
                                                                            LocalDateTime startNowDate);

    Page<Booking> findByUserIdAndEndDateAfterAndStartDateBeforeOrderByIdAsc(long userId,
                                                                            LocalDateTime endNowDate,
                                                                            LocalDateTime startNowDate,
                                                                            Pageable page);

    List<Booking> findByItemUserIdOrderByStartDateDesc(long userId);

    Page<Booking> findByItemUserIdOrderByStartDateDesc(long userId, Pageable page);

    List<Booking> findByItemUserIdAndStatusIsOrderByStartDateDesc(long userId, BookingStatus state);

    Page<Booking> findByItemUserIdAndStatusIsOrderByStartDateDesc(long userId, BookingStatus state, Pageable page);

    List<Booking> findByItemUserIdAndStartDateAfterOrderByStartDateDesc(long userId, LocalDateTime nowDate);

    Page<Booking> findByItemUserIdAndStartDateAfterOrderByStartDateDesc(long userId, LocalDateTime nowDate, Pageable page);

    List<Booking> findByItemUserIdAndEndDateBeforeOrderByStartDateDesc(long userId, LocalDateTime nowDate);

    Page<Booking> findByItemUserIdAndEndDateBeforeOrderByStartDateDesc(long userId, LocalDateTime nowDate, Pageable page);

    List<Booking> findByItemUserIdAndEndDateAfterAndStartDateBeforeOrderByIdAsc(long userId,
                                                                                LocalDateTime endNowDate,
                                                                                LocalDateTime startNowDate);

    Page<Booking> findByItemUserIdAndEndDateAfterAndStartDateBeforeOrderByIdAsc(long userId,
                                                                                LocalDateTime endNowDate,
                                                                                LocalDateTime startNowDate,
                                                                                Pageable page);
}