package com.example.dormitory.controller;

import com.example.dormitory.dto.CreateRoomDto;
import com.example.dormitory.dto.RoomDto;
import com.example.dormitory.enums.RequestPreferenceStatus;
import com.example.dormitory.service.AllocationService;
import com.example.dormitory.service.RequestPreferenceService;
import com.example.dormitory.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/warden")
@RequiredArgsConstructor
@Tag(name = "Warden", description = "Функции коменданта")
public class WardenController {
    private final RoomService roomService;
    private final RequestPreferenceService preferenceService;
    private final AllocationService allocationService;

    @GetMapping("/rooms")
    @Operation(summary = "Получить все комнаты")
    public ResponseEntity<List<RoomDto>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    @PostMapping("/rooms")
    @Operation(summary = "Создать комнату")
    public ResponseEntity<RoomDto> createRoom(@RequestBody CreateRoomDto dto) {
        return ResponseEntity.ok(roomService.createRoom(dto));
    }

    @DeleteMapping("/rooms/{id}")
    @Operation(summary = "Удалить комнату")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/preferences/{id}")
    @Operation(summary = "Изменить статус предпочтения")
    public ResponseEntity<Void> updatePreferenceStatus(@PathVariable Long id,
                                                       @RequestParam RequestPreferenceStatus status) {
        preferenceService.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/allocate/run")
    @Operation(summary = "Запустить распределение")
    public ResponseEntity<String> runAllocation() {
        allocationService.runAllocation();
        return ResponseEntity.ok("Allocation started");
    }
}