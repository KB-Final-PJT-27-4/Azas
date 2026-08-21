package com.azas.domain.child.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChildFeaturePermission {

    private Long childId;
    private boolean allowanceRequestEnabled;
    private boolean usageLimitViewEnabled;
}
