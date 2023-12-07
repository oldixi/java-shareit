package ru.practicum.shareit.request;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRequestRepository  extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findByUserIdOrderByCreationDateDesc(long userId);

    List<ItemRequest> findByUserIdNotOrderByCreationDateDesc(long userId);

    Page<ItemRequest> findByUserIdNot(long userId, Pageable page);
}