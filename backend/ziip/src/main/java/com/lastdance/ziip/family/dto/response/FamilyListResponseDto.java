package com.lastdance.ziip.family.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FamilyListResponseDto {

    List<FamilyListDetailResponseDto> familyListDetailResponseDtoList;

}
