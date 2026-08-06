package com.azas.domain.child.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

// 자녀 목록 조회 응답 DTO
@Getter
@AllArgsConstructor
public class ChildListResponse {
    private List<ChildSummaryResponse> items;
}
