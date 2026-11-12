package com.cinema.catalog.adapter.out.persistence;

import com.cinema.catalog.adapter.out.persistence.mapper.RoomPersistenceMapper;
import com.cinema.catalog.adapter.out.persistence.repository.RoomJpaRepository;
import com.cinema.catalog.application.port.out.RoomRepository;
import com.cinema.catalog.domain.model.room.Room;
import com.cinema.catalog.domain.model.room.RoomId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class RoomPersistenceAdapter implements RoomRepository {

    private final RoomJpaRepository jpaRepository;
    private final RoomPersistenceMapper mapper;


    public RoomPersistenceAdapter(RoomJpaRepository jpaRepository, RoomPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void save(Room room) {
        jpaRepository.save(mapper.toEntity(room));
    }

    @Override
    public Optional<Room> findById(RoomId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public List<Room> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }
}
