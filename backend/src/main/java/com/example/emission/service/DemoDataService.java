package com.example.emission.service;

import com.example.emission.dto.ApiResponse;
import com.example.emission.dto.AuditRequest;
import com.example.emission.dto.ErrorCode;
import com.example.emission.dto.StationStatus;
import com.example.emission.dto.WarningHandleRequest;
import com.example.emission.model.Announcement;
import com.example.emission.model.AuditRecord;
import com.example.emission.model.InspectionRecord;
import com.example.emission.model.Station;
import com.example.emission.model.UserAccount;
import com.example.emission.model.Vehicle;
import com.example.emission.model.WarningRecord;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class DemoDataService {
  private final Map<String, UserAccount> users = Map.of(
      "admin", new UserAccount("admin", "平台管理员", "平台管理员"),
      "regulator", new UserAccount("regulator", "监管人员", "东城区监管员"),
      "station", new UserAccount("station", "检测站工作人员", "朝阳检测站"),
      "user", new UserAccount("user", "普通用户", "车主用户")
  );

  private final List<Vehicle> vehicles = List.of(
      new Vehicle("京A12345", "LHGCM82633A004352", "小型轿车", "汽油", "国六", "张先生", "2021-06-18", "合格"),
      new Vehicle("京B67890", "LSVNV2187N2039456", "轻型货车", "柴油", "国五", "北京绿运物流", "2019-03-12", "待复检"),
      new Vehicle("京C24680", "LFPH4ACC9N1A20458", "小型客车", "混合动力", "国六", "李女士", "2022-10-09", "合格")
  );

  private final List<InspectionRecord> inspections = new ArrayList<>(List.of(
      new InspectionRecord("JC20260611001", "京A12345", "朝阳机动车环保检测站", "2026-06-11 09:12", 0.18, 18.4, 32.8, 0.11, "合格", "王工", "已审核"),
      new InspectionRecord("JC20251215023", "京A12345", "海淀机动车检测中心", "2025-12-15 14:20", 0.20, 20.1, 35.2, 0.13, "合格", "赵工", "已审核"),
      new InspectionRecord("JC20250620018", "京A12345", "朝阳机动车环保检测站", "2025-06-20 10:45", 0.22, 22.5, 38.0, 0.15, "合格", "王工", "已审核"),
      new InspectionRecord("JC20241210031", "京A12345", "亦庄机动车检测站", "2024-12-10 16:08", 0.25, 25.3, 41.2, 0.18, "合格", "陈工", "已审核"),

      new InspectionRecord("JC20260611002", "京B67890", "海淀机动车检测中心", "2026-06-11 10:05", 0.42, 39.7, 88.1, 0.38, "不合格", "赵工", "待审核"),
      new InspectionRecord("JC20251218045", "京B67890", "朝阳机动车环保检测站", "2025-12-18 11:30", 0.38, 35.2, 75.6, 0.32, "合格", "王工", "已审核"),
      new InspectionRecord("JC20250615067", "京B67890", "海淀机动车检测中心", "2025-06-15 09:50", 0.40, 38.0, 82.4, 0.35, "合格", "赵工", "已审核"),
      new InspectionRecord("JC20241205089", "京B67890", "亦庄机动车检测站", "2024-12-05 15:22", 0.36, 33.5, 70.1, 0.30, "合格", "陈工", "已审核"),

      new InspectionRecord("JC20260610008", "京C24680", "亦庄机动车检测站", "2026-06-10 15:46", 0.16, 15.3, 28.6, 0.09, "合格", "陈工", "已审核"),
      new InspectionRecord("JC20251220012", "京C24680", "朝阳机动车环保检测站", "2025-12-20 13:15", 0.18, 17.2, 30.4, 0.10, "合格", "王工", "已审核"),
      new InspectionRecord("JC20250625005", "京C24680", "海淀机动车检测中心", "2025-06-25 08:40", 0.15, 14.8, 26.9, 0.08, "合格", "赵工", "已审核"),
      new InspectionRecord("JC20241212022", "京C24680", "亦庄机动车检测站", "2024-12-12 14:55", 0.17, 16.5, 29.1, 0.09, "合格", "陈工", "已审核")
  ));

  private final List<AuditRecord> auditRecords = new ArrayList<>();
  private final AtomicLong auditRecordIdGenerator = new AtomicLong(1);

  private WarningRecord createWarning(Long id, String plateNumber, String pollutant,
                                      String level, String description, String status, String createdAt) {
    WarningRecord w = new WarningRecord(id, plateNumber, pollutant, level, description, status, createdAt);
    return w;
  }

  public Optional<UserAccount> findUser(String username, String password) {
    if (!"123456".equals(password)) {
      return Optional.empty();
    }
    return Optional.ofNullable(users.get(username));
  }

  public Optional<Vehicle> searchVehicle(String keyword) {
    String normalized = normalizeKeyword(keyword);
    if (normalized.isEmpty()) {
      return Optional.empty();
    }
    return vehicles.stream()
        .filter(vehicle -> vehicle.plateNumber().equalsIgnoreCase(normalized)
            || vehicle.vin().equalsIgnoreCase(normalized)
            || inspections.stream().anyMatch(record -> record.getInspectionNo().equalsIgnoreCase(normalized)
                && record.getPlateNumber().equalsIgnoreCase(vehicle.plateNumber())))
        .findFirst();
  }

  private String normalizeKeyword(String keyword) {
    if (keyword == null) {
      return "";
    }
    String trimmed = keyword.trim()
        .replaceAll("\\s+", "")
        .replaceAll("　", "")
        .replaceAll("\u00A0", "")
        .replaceAll("\\u200B", "")
        .replaceAll("\\uFEFF", "");
    if (trimmed.isEmpty()) {
      return "";
    }
    return trimmed.toUpperCase();
  }

  private String detectKeywordType(String normalized) {
    if (normalized.isEmpty()) {
      return "UNKNOWN";
    }
    if (normalized.startsWith("JC") && normalized.length() >= 8) {
      return "INSPECTION_NO";
    }
    if (normalized.length() == 17 && normalized.matches("^[A-HJ-NPR-Z0-9]+$")) {
      return "VIN";
    }
    if (normalized.length() >= 6 && normalized.length() <= 9) {
      return "PLATE_NUMBER";
    }
    return "UNKNOWN";
  }

  public ApiResponse<Vehicle> searchVehicleWithValidation(String keyword) {
    String normalized = normalizeKeyword(keyword);

    if (normalized.isEmpty()) {
      return ApiResponse.error(ErrorCode.EMPTY_KEYWORD.getCode(), ErrorCode.EMPTY_KEYWORD.getMessage());
    }

    if (normalized.length() < 2) {
      return ApiResponse.error(ErrorCode.KEYWORD_TOO_SHORT.getCode(), ErrorCode.KEYWORD_TOO_SHORT.getMessage());
    }

    if (normalized.length() > 30) {
      return ApiResponse.error(ErrorCode.KEYWORD_TOO_LONG.getCode(), ErrorCode.KEYWORD_TOO_LONG.getMessage());
    }

    String keywordType = detectKeywordType(normalized);

    Optional<Vehicle> vehicleByPlate = vehicles.stream()
        .filter(vehicle -> vehicle.plateNumber().equalsIgnoreCase(normalized))
        .findFirst();
    if (vehicleByPlate.isPresent()) {
      return ApiResponse.success("通过车牌号查询成功", vehicleByPlate.get());
    }

    Optional<Vehicle> vehicleByVin = vehicles.stream()
        .filter(vehicle -> vehicle.vin().equalsIgnoreCase(normalized))
        .findFirst();
    if (vehicleByVin.isPresent()) {
      return ApiResponse.success("通过 VIN 查询成功", vehicleByVin.get());
    }

    Optional<InspectionRecord> inspectionRecord = inspections.stream()
        .filter(record -> record.getInspectionNo().equalsIgnoreCase(normalized))
        .findFirst();
    if (inspectionRecord.isPresent()) {
      String plateNumber = inspectionRecord.get().getPlateNumber();
      Optional<Vehicle> vehicleByInspection = vehicles.stream()
          .filter(vehicle -> vehicle.plateNumber().equalsIgnoreCase(plateNumber))
          .findFirst();
      if (vehicleByInspection.isPresent()) {
        return ApiResponse.success("通过检测报告编号关联查询成功，关联车牌号：" + plateNumber, vehicleByInspection.get());
      } else {
        return ApiResponse.error(ErrorCode.VEHICLE_NOT_FOUND_BY_INSPECTION.getCode(),
            "检测报告编号「" + normalized + "」存在，但未找到关联车辆「" + plateNumber + "」的信息");
      }
    }

    String notFoundMessage;
    String errorCode;
    switch (keywordType) {
      case "PLATE_NUMBER":
        errorCode = ErrorCode.PLATE_NOT_FOUND.getCode();
        notFoundMessage = "未找到车牌号「" + normalized + "」对应的车辆，请核对车牌号是否正确后重试";
        break;
      case "VIN":
        errorCode = ErrorCode.VIN_NOT_FOUND.getCode();
        notFoundMessage = "未找到 VIN「" + normalized + "」对应的车辆，请核对车辆识别代号是否正确后重试";
        break;
      case "INSPECTION_NO":
        errorCode = ErrorCode.INSPECTION_NOT_FOUND.getCode();
        notFoundMessage = "未找到检测报告编号「" + normalized + "」对应的记录，请核对报告编号是否正确后重试";
        break;
      default:
        errorCode = ErrorCode.VEHICLE_NOT_FOUND.getCode();
        notFoundMessage = "未找到匹配的车辆或检测记录，请检查输入是否正确。支持输入车牌号（如京A12345）、17位VIN码或检测报告编号（如JC20260611001）进行查询";
    }
    return ApiResponse.error(errorCode, notFoundMessage);
  }

  public List<InspectionRecord> inspections(String plateNumber) {
    List<InspectionRecord> result;
    if (plateNumber == null || plateNumber.isBlank()) {
      result = new ArrayList<>(inspections);
    } else {
      result = inspections.stream()
          .filter(record -> record.getPlateNumber().equalsIgnoreCase(plateNumber.trim()))
          .collect(Collectors.toList());
    }
    return result.stream()
        .sorted((a, b) -> b.getInspectionTime().compareTo(a.getInspectionTime()))
        .collect(Collectors.toList());
  }

  public List<Station> stations() {
    return List.of(
        new Station(1, "朝阳机动车环保检测站", "朝阳区", "北京市朝阳区环保科技园 18 号", "010-61112222", "正常"),
        new Station(2, "海淀机动车检测中心", "海淀区", "北京市海淀区清河路 66 号", "010-62223333", "正常"),
        new Station(3, "亦庄机动车检测站", "经开区", "北京市经济技术开发区荣华南路 9 号", "010-63334444", "正常")
    );
  }

  public List<Announcement> announcements() {
    return List.of(
        new Announcement(1, "关于加强重型柴油车尾气监管的通知", "2026-06-10"),
        new Announcement(2, "机动车环保定期检测服务时间调整", "2026-06-02"),
        new Announcement(3, "国六排放标准车辆登记资料核验提醒", "2026-05-28")
    );
  }

  private final List<WarningRecord> warningRecords = new ArrayList<>(List.of(
      createWarning(1L, "京B67890", "NOx", "高", "氮氧化物检测值超过限值，需复检", "待处置", "2026-06-11 10:30"),
      createWarning(2L, "京D13579", "烟度", "中", "柴油车烟度值接近预警阈值", "待处置", "2026-06-10 15:20"),
      createWarning(3L, "京A12345", "CO", "低", "一氧化碳检测值略超限值", "待处置", "2026-06-09 09:15"),
      createWarning(4L, "京E88888", "HC", "高", "碳氢化合物严重超标，需立即处置", "待处置", "2026-06-08 14:45")
  ));

  private final AtomicLong warningIdGenerator = new AtomicLong(5);

  public Map<String, Object> dashboard(Integer days) {
    if (days == null || days <= 0) {
      days = 7;
    }

    long pendingCount = inspections.stream()
        .filter(r -> "待审核".equals(r.getReportStatus()))
        .count();

    List<Map<String, Object>> trend = new ArrayList<>();
    for (int i = days - 1; i >= 0; i--) {
      LocalDate date = LocalDate.now().minusDays(i);
      int baseCount = 90 + (int) (Math.sin(i * 0.8) * 20) + (days > 14 ? (int) (Math.random() * 30) : 0);
      trend.add(Map.of("date", date.toString(), "count", baseCount));
    }

    int totalInspections = trend.stream().mapToInt(item -> (int) item.get("count")).sum();
    int failedVehicles = (int) (totalInspections * 0.11);
    int passedVehicles = totalInspections - failedVehicles;
    double exceedRate = Math.round(failedVehicles * 1000.0 / totalInspections) / 10.0;

    return Map.of(
        "days", days,
        "totalInspections", totalInspections,
        "passedVehicles", passedVehicles,
        "failedVehicles", failedVehicles,
        "exceedRate", exceedRate,
        "pendingAudit", pendingCount,
        "stationCount", stations().size(),
        "trend", trend,
        "emissionStandards", List.of(
            Map.of("name", "国六", "value", 54),
            Map.of("name", "国五", "value", 32),
            Map.of("name", "国四及以下", "value", 14)
        )
    );
  }

  public List<StationStatus> stationStatuses() {
    String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    return stations().stream()
        .map(station -> {
          List<InspectionRecord> stationRecords = inspections.stream()
              .filter(record -> record.getStationName().equals(station.name()))
              .collect(Collectors.toList());

          List<InspectionRecord> todayRecords = stationRecords.stream()
              .filter(record -> record.getInspectionTime().startsWith(today))
              .collect(Collectors.toList());

          int todayCount = todayRecords.size();
          int passedCount = (int) todayRecords.stream()
              .filter(record -> "合格".equals(record.getResult()))
              .count();
          int failedCount = todayCount - passedCount;
          double passRate = todayCount > 0
              ? Math.round(passedCount * 1000.0 / todayCount) / 10.0
              : 0.0;

          String lastInspectionTime = stationRecords.stream()
              .max(Comparator.comparing(InspectionRecord::getInspectionTime))
              .map(InspectionRecord::getInspectionTime)
              .orElse("");

          String runningStatus;
          if (!"正常".equals(station.status())) {
            runningStatus = "停运";
          } else if (todayCount > 0) {
            runningStatus = "运行中";
          } else {
            runningStatus = "空闲";
          }

          return new StationStatus(
              station.id(),
              station.name(),
              station.district(),
              station.address(),
              station.phone(),
              todayCount,
              passedCount,
              failedCount,
              passRate,
              lastInspectionTime,
              runningStatus
          );
        })
        .collect(Collectors.toList());
  }

  public synchronized Map<String, Object> audit(AuditRequest request, String auditor) {
    String inspectionNo = request.getInspectionNo();
    String action = request.getAction();
    String opinion = request.getOpinion();

    Optional<InspectionRecord> recordOpt = inspections.stream()
        .filter(r -> r.getInspectionNo().equals(inspectionNo))
        .findFirst();

    if (recordOpt.isEmpty()) {
      return Map.of("success", false, "message", "检测记录不存在");
    }

    InspectionRecord record = recordOpt.get();
    if (!"待审核".equals(record.getReportStatus())) {
      return Map.of("success", false, "message", "该记录状态不是待审核");
    }

    String newStatus = "PASS".equals(action) ? "已审核" : "已退回";
    String actionName = "PASS".equals(action) ? "通过" : "退回";
    String auditTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    record.setReportStatus(newStatus);
    record.setAuditor(auditor);
    record.setAuditTime(auditTime);
    record.setAuditOpinion(opinion);

    AuditRecord auditRecord = new AuditRecord(
        auditRecordIdGenerator.getAndIncrement(),
        inspectionNo,
        actionName,
        opinion,
        auditor,
        auditTime
    );
    auditRecords.add(auditRecord);

    return Map.of(
        "success", true,
        "message", "审核" + actionName + "成功",
        "record", record
    );
  }

  public Optional<InspectionRecord> getInspectionDetail(String inspectionNo) {
    return inspections.stream()
        .filter(r -> r.getInspectionNo().equalsIgnoreCase(inspectionNo == null ? "" : inspectionNo.trim()))
        .findFirst();
  }

  public List<AuditRecord> getAuditRecords(String inspectionNo) {
    return auditRecords.stream()
        .filter(r -> r.inspectionNo().equals(inspectionNo))
        .sorted((a, b) -> b.auditTime().compareTo(a.auditTime()))
        .collect(Collectors.toList());
  }

  public List<WarningRecord> warnings() {
    return new ArrayList<>(warningRecords);
  }

  public Optional<WarningRecord> getWarningById(Long id) {
    return warningRecords.stream()
        .filter(w -> w.getId().equals(id))
        .findFirst();
  }

  private boolean isValidTransition(String current, String target, boolean reinspectRequired) {
    if ("已复检".equals(current)) {
      return false;
    }
    if ("已处置".equals(current)) {
      return reinspectRequired && "已复检".equals(target);
    }
    if ("处置中".equals(current)) {
      return "已处置".equals(target) || "处置中".equals(target);
    }
    if ("待处置".equals(current)) {
      return "处置中".equals(target) || "已处置".equals(target);
    }
    return false;
  }

  public synchronized Map<String, Object> handleWarning(WarningHandleRequest request, String handler) {
    Long warningId = request.getWarningId();

    Optional<WarningRecord> warningOpt = warningRecords.stream()
        .filter(w -> w.getId().equals(warningId))
        .findFirst();

    if (warningOpt.isEmpty()) {
      return Map.of("success", false, "message", "预警记录不存在");
    }

    WarningRecord warning = warningOpt.get();

    String targetStatus = request.getStatus();
    if (targetStatus == null || targetStatus.isBlank()) {
      targetStatus = "处置中";
    }

    if (!isValidTransition(warning.getStatus(), targetStatus, warning.isReinspectRequired())) {
      return Map.of("success", false, "message", "当前状态「" + warning.getStatus() + "」不允许转换到「" + targetStatus + "」");
    }

    String status = targetStatus;

    String handleTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    warning.setStatus(status);
    warning.setHandler(handler);
    warning.setHandleTime(handleTime);
    warning.setHandleOpinion(request.getHandleOpinion());
    warning.setReinspectRequired(request.isReinspectRequired());
    if (request.isReinspectRequired() && request.getReinspectDeadline() != null && !request.getReinspectDeadline().isBlank()) {
      warning.setReinspectDeadline(request.getReinspectDeadline());
    }

    return Map.of(
        "success", true,
        "message", "预警处置成功",
        "record", warning
    );
  }

  public List<InspectionRecord> getInspectionsByPlate(String plateNumber) {
    if (plateNumber == null || plateNumber.isBlank()) {
      return List.of();
    }
    return inspections.stream()
        .filter(r -> r.getPlateNumber().equalsIgnoreCase(plateNumber.trim()))
        .sorted((a, b) -> b.getInspectionTime().compareTo(a.getInspectionTime()))
        .collect(Collectors.toList());
  }
}
