package com.lastdance.ziip.plan.exception.validator;

import com.lastdance.ziip.plan.exception.NoExistPlan;
import com.lastdance.ziip.plan.exception.NoMatchingManager;
import com.lastdance.ziip.plan.repository.entity.Plan;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Component
public class PlanValidator {

    public void checkPlanExist(Optional<Plan> plan){
        if(plan.isEmpty()){
            throw new NoExistPlan("해당 계획이 존재하지 않습니다.");
        }
    }

    public void checkPlanManager(Plan plan, Long memberId){
        if(plan.getMember().getId() != memberId){
            throw new NoMatchingManager("해당 계획 담당자가 아닙니다.");
        }
    }
}
