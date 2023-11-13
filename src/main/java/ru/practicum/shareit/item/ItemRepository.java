package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    Optional<Item> findByUserIdAndId(long userId, long itemId);

    Optional<Item> findByUserIdNotAndId(long userId, long itemId);

    List<Item> findByUserId(long userId);

    void deleteByUserIdAndId(long userId, long itemId);

    List<Item> findByAvailableTrueAndUserIdAndNameContainingIgnoreCaseOrAvailableTrueAndDescriptionContainingIgnoreCase(long userId,
                                                                                                                        String name,
                                                                                                                        String dsc);
}