package com.example.dormitory.controller;

import com.example.dormitory.dto.CreateRoomDto;
import com.example.dormitory.dto.RoomDto;
import com.example.dormitory.entity.Dormitory;
import com.example.dormitory.security.UserDetailsImpl;
import com.example.dormitory.service.RoomService;
import com.example.dormitory.service.WardenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;
    private final WardenService wardenService;

    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'WARDEN', 'ADMIN')")
    public ResponseEntity<List<RoomDto>> getAllRooms(
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) String type,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        Long dormitoryId = null;
        if (currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_WARDEN"))) {
            dormitoryId = currentUser.getDormitoryId();
            if (dormitoryId == null) {
                Dormitory dorm = wardenService.getWardenDormitory(currentUser.getUser().getId());
                if (dorm != null) {
                    dormitoryId = dorm.getId();
                }
            }
        }
        return ResponseEntity.ok(roomService.getAllRooms(floor, type, dormitoryId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('WARDEN', 'ADMIN')")
    public ResponseEntity<RoomDto> createRoom(@Valid @RequestBody CreateRoomDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.createRoom(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('WARDEN', 'ADMIN')")
    public ResponseEntity<RoomDto> updateRoom(@PathVariable Long id, @Valid @RequestBody CreateRoomDto dto) {
        return ResponseEntity.ok(roomService.updateRoom(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('WARDEN', 'ADMIN')")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('WARDEN', 'ADMIN')")
    public ResponseEntity<RoomDto> getRoomById(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

}