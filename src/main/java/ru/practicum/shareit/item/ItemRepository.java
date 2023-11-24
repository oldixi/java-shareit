package ru.practicum.shareit.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    Optional<Item> findByUserIdAndId(long userId, long itemId);

    Optional<Item> findByUserIdNotAndId(long userId, long itemId);

    List<Item> findByRequestUserIdOrderByRequestCreationDateDesc(long userId);

    void deleteByUserIdAndId(long userId, long itemId);

    List<Item> findByUserIdOrderByIdAsc(long userId);

    Page<Item> findByUserIdOrderByIdAsc(long userId, Pageable page);

    List<Item> findByAvailableTrueAndUserIdAndNameContainingIgnoreCaseOrAvailableTrueAndDescriptionContainingIgnoreCase(long userId,
                                                                                                                        String name,
                                                                                                                        String dsc);

    Page<Item> findByAvailableTrueAndUserIdAndNameContainingIgnoreCaseOrAvailableTrueAndDescriptionContainingIgnoreCase(long userId,
                                                                                                                        String name,
                                                                                                                        String dsc,
                                                                                                                        Pageable page);
}