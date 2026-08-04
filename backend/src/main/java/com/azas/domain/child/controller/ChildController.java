package com.azas.domain.child.controller;

import com.azas.domain.child.dto.ChildCreateRequest;
import com.azas.domain.child.dto.ChildListResponse;
import com.azas.domain.child.dto.ChildResponse;
import com.azas.domain.child.dto.ChildUpdateRequest;
import com.azas.domain.child.service.ChildService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/children")
@RequiredArgsConstructor
public class ChildController {
    private final ChildService childService;

    @PostMapping
    public ResponseEntity<ChildResponse> createChild(@RequestBody ChildCreateRequest request) {
        Long memberId = 1L;

        ChildResponse childResponse = childService.createChild(memberId, request);
        return ResponseEntity.ok(childResponse);
    }

    @GetMapping
    public ResponseEntity<ChildListResponse> getChildren() {
        Long memberId = 1L;

        ChildListResponse childListResponse= childService.getChildren(memberId);
        return ResponseEntity.ok(childListResponse);
    }

    @GetMapping("/{childId}")
    public ResponseEntity<ChildResponse> getChild(@PathVariable Long childId) {
        Long memberId = 1L;

        ChildResponse childResponse = childService.getChild(memberId, childId);
        return ResponseEntity.ok(childResponse);
    }


    @PatchMapping("/{childId}")
    public ResponseEntity<ChildResponse> updateChild(
            @PathVariable Long childId,
            @RequestBody ChildUpdateRequest request
    ) {
        Long memberId = 1L;
        ChildResponse childResponse = childService.updateChild(memberId, childId, request);
        return ResponseEntity.ok(childResponse);
    }

    @DeleteMapping("/{childId}")
    public ResponseEntity<Void> deleteChild(@PathVariable Long childId) {
        Long memberId = 1L;

        childService.deleteChild(memberId, childId);
        return ResponseEntity.noContent().build();
    }

}
