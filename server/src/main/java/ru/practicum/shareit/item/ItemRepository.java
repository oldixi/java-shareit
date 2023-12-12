package ru.practicum.shareit.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

    @Query(value = "select i.* from items i " +
            "where i.is_available = true " +
            "and (upper(i.name collate \"en_US\") like upper(concat('%', ?1, '%') collate \"en_US\") " +
            "or upper(i.description collate \"en_US\") like upper(concat('%', ?2, '%') collate \"en_US\")) ",
            nativeQuery = true)
    List<Item> searchItemsByText(String name, String description);

    @Query(value = "select i.* from items i " +
            "where i.is_available = true " +
            "and (upper(i.name collate \"en_US\") like upper(concat('%', ?1, '%') collate \"en_US\") " +
            "or upper(i.description collate \"en_US\") like upper(concat('%', ?2, '%') collate \"en_US\")) ",
            nativeQuery = true)
    Page<Item> searchItemsByText(String name, String description, Pageable page);
}