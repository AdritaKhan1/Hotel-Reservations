package ca.adritakhan.hotel.web;

import ca.adritakhan.hotel.domain.RoomType;
import ca.adritakhan.hotel.repository.RoomTypeRepository;
import ca.adritakhan.hotel.service.InventoryService;
import ca.adritakhan.hotel.service.RoomTypeNotFoundException;
import ca.adritakhan.hotel.web.dto.CreateRoomTypeRequest;
import ca.adritakhan.hotel.web.dto.RoomTypeResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/room-types")
public class RoomTypeController {

    private final InventoryService inventory;
    private final RoomTypeRepository roomTypes;

    public RoomTypeController(InventoryService inventory, RoomTypeRepository roomTypes) {
        this.inventory = inventory;
        this.roomTypes = roomTypes;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoomTypeResponse create(@Valid @RequestBody CreateRoomTypeRequest request) {
        RoomType created = inventory.createRoomType(request.name(), request.description());
        return RoomTypeResponse.from(created);
    }

    @GetMapping
    public List<RoomTypeResponse> list() {
        return roomTypes.findAll().stream().map(RoomTypeResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomTypeResponse> get(@PathVariable Long id) {
        RoomType type = roomTypes.findById(id).orElseThrow(() -> new RoomTypeNotFoundException(id));
        return ResponseEntity.ok(RoomTypeResponse.from(type));
    }
}
