package com.example.emission.service;

import com.example.emission.dto.EnvironmentalJudgmentResult;
import com.example.emission.dto.EnvironmentalJudgmentResult.ExceededItem;
import com.example.emission.model.EmissionStandard;
import com.example.emission.model.InspectionRecord;
import com.example.emission.model.PollutantLimitRule;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class EmissionJudgmentService {

    private static final Logger log = LoggerFactory.getLogger(EmissionJudgmentService.class);

    private final DemoDataService demoDataService;

    public EmissionJudgmentService(@Lazy DemoDataService demoDataService) {
        this.demoDataService = demoDataService;
    }

    public EnvironmentalJudgmentResult judge(InspectionRecord record, String fuelType, String emissionStandardLabel) {
        double coLimit, hcLimit, noxLimit, opacityLimit;
        String appliedStandard;
        String resolvedFuelType = fuelType != null && !fuelType.isBlank() ? fuelType : "汽油";
        String resolvedStandard = emissionStandardLabel != null && !emissionStandardLabel.isBlank() ? emissionStandardLabel : "国四";

        Optional<PollutantLimitRule> ruleOpt = demoDataService.getPollutantLimitRule(resolvedFuelType, resolvedStandard);
        if (ruleOpt.isPresent()) {
            PollutantLimitRule rule = ruleOpt.get();
            coLimit = rule.getCoLimit();
            hcLimit = rule.getHcLimit();
            noxLimit = rule.getNoxLimit();
            opacityLimit = rule.getOpacityLimit();
            appliedStandard = resolvedFuelType + "/" + resolvedStandard;
            log.debug("使用污染物限值规则：fuelType={}, emissionStandard={}, coLimit={}, hcLimit={}, noxLimit={}, opacityLimit={}",
                    resolvedFuelType, resolvedStandard, coLimit, hcLimit, noxLimit, opacityLimit);
        } else {
            log.warn("未找到匹配的污染物限值规则：fuelType={}, emissionStandard={}，使用默认枚举", resolvedFuelType, resolvedStandard);
            EmissionStandard standard = EmissionStandard.fromLabel(resolvedStandard);
            coLimit = standard.getCoLimit();
            hcLimit = standard.getHcLimit();
            noxLimit = standard.getNoxLimit();
            opacityLimit = standard.getOpacityLimit();
            appliedStandard = standard.getLabel() + "(默认)";
        }

        List<ExceededItem> exceededItems = new ArrayList<>();
        checkExceedance(exceededItems, "CO", record.getCoValue(), coLimit);
        checkExceedance(exceededItems, "HC", record.getHcValue(), hcLimit);
        checkExceedance(exceededItems, "NOx", record.getNoxValue(), noxLimit);
        checkExceedance(exceededItems, "烟度", record.getOpacityValue(), opacityLimit);

        String environmentalStatus;
        String statusLevel;
        String suggestion;

        if (exceededItems.isEmpty()) {
            environmentalStatus = "环保合格";
            statusLevel = "合格";
            suggestion = "车辆排放指标均在限值范围内，环保状态正常，可正常使用。";
        } else if (exceededItems.size() == 1) {
            ExceededItem item = exceededItems.get(0);
            if (item.getExceedRatio() <= 0.2) {
                environmentalStatus = "轻微超标";
                statusLevel = "低";
                suggestion = buildSuggestion(exceededItems, "车辆存在轻微超标，建议短期内进行检修维护并安排复检。");
            } else if (item.getExceedRatio() <= 0.5) {
                environmentalStatus = "超标";
                statusLevel = "中";
                suggestion = buildSuggestion(exceededItems, "车辆排放超标，需进行维修治理后在限期内复检。");
            } else {
                environmentalStatus = "严重超标";
                statusLevel = "高";
                suggestion = buildSuggestion(exceededItems, "车辆排放严重超标，需立即停运整改，维修治理合格后方可上路。");
            }
        } else {
            double maxRatio = exceededItems.stream()
                    .mapToDouble(ExceededItem::getExceedRatio)
                    .max()
                    .orElse(0);
            if (maxRatio <= 0.3) {
                environmentalStatus = "超标";
                statusLevel = "中";
                suggestion = buildSuggestion(exceededItems, "车辆多项指标超标，需进行综合维修治理并在限期内复检。");
            } else {
                environmentalStatus = "严重超标";
                statusLevel = "高";
                suggestion = buildSuggestion(exceededItems, "车辆多项指标严重超标，需立即停运整改，全面维修治理合格后方可上路。");
            }
        }

        if ("不合格".equals(record.getResult()) && "环保合格".equals(environmentalStatus)) {
            environmentalStatus = "待确认";
            statusLevel = "低";
            suggestion = "检测结论为不合格，但排放数值在限值范围内，建议人工复核确认。";
        }

        log.debug("环保状态判定完成：record={}, fuelType={}, standard={}, status={}, level={}, exceeded={}",
                record.getInspectionNo(), resolvedFuelType, resolvedStandard, environmentalStatus, statusLevel, exceededItems.size());

        EnvironmentalJudgmentResult result = new EnvironmentalJudgmentResult(
                environmentalStatus,
                statusLevel,
                exceededItems,
                suggestion,
                appliedStandard
        );
        result.setFuelType(resolvedFuelType);
        result.setEmissionStandard(resolvedStandard);
        result.setCoLimit(coLimit);
        result.setHcLimit(hcLimit);
        result.setNoxLimit(noxLimit);
        result.setOpacityLimit(opacityLimit);
        return result;
    }

    public EnvironmentalJudgmentResult judge(InspectionRecord record, String emissionStandardLabel) {
        return judge(record, null, emissionStandardLabel);
    }

    public EnvironmentalJudgmentResult judge(InspectionRecord record) {
        return judge(record, null, null);
    }

    private void checkExceedance(List<ExceededItem> items, String pollutant,
                                  double value, double limit) {
        if (value > limit) {
            double exceedRatio = Math.round((value - limit) / limit * 1000.0) / 1000.0;
            items.add(new ExceededItem(pollutant, value, limit, exceedRatio));
        }
    }

    private String buildSuggestion(List<ExceededItem> exceededItems, String base) {
        StringBuilder sb = new StringBuilder(base);
        sb.append(" 超标项：");
        for (int i = 0; i < exceededItems.size(); i++) {
            ExceededItem item = exceededItems.get(i);
            if (i > 0) {
                sb.append("；");
            }
            sb.append(item.getPollutant())
                    .append(" 检测值=").append(item.getValue())
                    .append(" 限值=").append(item.getLimit())
                    .append(" 超标比=").append(String.format("%.1f%%", item.getExceedRatio() * 100));
        }
        return sb.toString();
    }
}
