package com.cinema.catalog.application.port.in.query;

import com.cinema.catalog.application.query.RoomView;

import java.util.List;

public interface RoomQueryUseCase {
    List<RoomView> list();
}
