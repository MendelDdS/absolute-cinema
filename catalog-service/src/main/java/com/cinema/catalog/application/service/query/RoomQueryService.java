package com.cinema.catalog.application.service.query;

import com.cinema.catalog.application.exception.RoomNotFoundException;
import com.cinema.catalog.application.port.in.query.RoomQueryUseCase;
import com.cinema.catalog.application.port.out.RoomRepository;
import com.cinema.catalog.application.query.RoomView;
import com.cinema.catalog.domain.model.room.RoomId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RoomQueryService implements RoomQueryUseCase {

    private final RoomRepository roomRepository;

    public RoomQueryService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public List<RoomView> list() {
        return roomRepository.findAll()
                .stream()
                .map(RoomView::from)
                .toList();
    }

    @Override
    public RoomView get(RoomId id) {
        return roomRepository.findById(id)
                .map(RoomView::from)
                .orElseThrow(() -> new RoomNotFoundException(id));
    }
}
