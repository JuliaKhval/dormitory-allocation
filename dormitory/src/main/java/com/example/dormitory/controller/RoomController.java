package com.example.dormitory.controller;

import com.example.dormitory.dto.CreateRoomDto;
import com.example.dormitory.dto.RoomDto;
import com.example.dormitory.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'WARDEN','ADMIN')")
    public ResponseEntity<List<RoomDto>> getAllRooms(
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) String type) {
        return ResponseEntity.ok(roomService.getAllRooms(floor, type));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('WARDEN','ADMIN')")
    public ResponseEntity<RoomDto> createRoom(@Valid @RequestBody CreateRoomDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.createRoom(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('WARDEN','ADMIN')")
    public ResponseEntity<RoomDto> updateRoom(@PathVariable Long id, @Valid @RequestBody CreateRoomDto dto) {
        return ResponseEntity.ok(roomService.updateRoom(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('WARDEN','ADMIN')")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }
}