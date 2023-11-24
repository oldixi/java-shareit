package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.User;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemDtoWithRequestId {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private User user;
    private Long requestId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemDtoWithRequestId)) return false;
        return id != null && id.equals(((ItemDtoWithRequestId) o).getId());
    }
}