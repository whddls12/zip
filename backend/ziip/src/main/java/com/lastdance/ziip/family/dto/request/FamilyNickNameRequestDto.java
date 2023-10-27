package com.lastdance.ziip.family.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FamilyNickNameRequestDto {

    private Long familyId;
    private String nickname;
}
