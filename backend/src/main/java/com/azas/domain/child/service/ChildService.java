package com.azas.domain.child.service;

import com.azas.domain.child.dto.ChildCreateRequest;
import com.azas.domain.child.dto.ChildListResponse;
import com.azas.domain.child.dto.ChildResponse;
import com.azas.domain.child.dto.ChildUpdateRequest;

public interface ChildService {
    ChildResponse createChild(Long memberId, ChildCreateRequest request);
    ChildListResponse getChildren(Long memberId);
    ChildResponse getChild(Long memberId, Long childId);
    ChildResponse updateChild(Long memberId,Long childId, ChildUpdateRequest request);
    void deleteChild(Long memberId, Long childId);
}
