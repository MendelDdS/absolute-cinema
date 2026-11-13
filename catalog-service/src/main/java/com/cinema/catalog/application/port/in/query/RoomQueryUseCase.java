package com.cinema.catalog.application.port.in.query;

import com.cinema.catalog.application.query.RoomView;
import com.cinema.catalog.domain.model.room.RoomId;

import java.util.List;

public interface RoomQueryUseCase {
    List<RoomView> list();
    RoomView get(RoomId id);
}
