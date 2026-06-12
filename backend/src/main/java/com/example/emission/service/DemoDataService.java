package com.example.emission.service;

import com.example.emission.dto.AuditRequest;
import com.example.emission.dto.StationStatus;
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

  public Optional<UserAccount> findUser(String username, String password) {
    if (!"123456".equals(password)) {
      return Optional.empty();
    }
    return Optional.ofNullable(users.get(username));
  }

  public Optional<Vehicle> searchVehicle(String keyword) {
    String normalized = keyword == null ? "" : keyword.trim().toUpperCase();
    return vehicles.stream()
        .filter(vehicle -> vehicle.plateNumber().equalsIgnoreCase(normalized)
            || vehicle.vin().equalsIgnoreCase(normalized)
            || inspections.stream().anyMatch(record -> record.getInspectionNo().equalsIgnoreCase(normalized)
                && record.getPlateNumber().equals(vehicle.plateNumber())))
        .findFirst();
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

  public List<WarningRecord> warnings() {
    return List.of(
        new WarningRecord("京B67890", "NOx", "高", "氮氧化物检测值超过限值，需复检"),
        new WarningRecord("京D13579", "烟度", "中", "柴油车烟度值接近预警阈值")
    );
  }

  public Map<String, Object> dashboard() {
    long pendingCount = inspections.stream()
        .filter(r -> "待审核".equals(r.getReportStatus()))
        .count();

    return Map.of(
        "todayInspections", 126,
        "passedVehicles", 112,
        "failedVehicles", 14,
        "exceedRate", 11.1,
        "pendingAudit", pendingCount,
        "stationCount", stations().size(),
        "trend", List.of(
            Map.of("date", LocalDate.now().minusDays(6).toString(), "count", 91),
            Map.of("date", LocalDate.now().minusDays(5).toString(), "count", 105),
            Map.of("date", LocalDate.now().minusDays(4).toString(), "count", 98),
            Map.of("date", LocalDate.now().minusDays(3).toString(), "count", 121),
            Map.of("date", LocalDate.now().minusDays(2).toString(), "count", 117),
            Map.of("date", LocalDate.now().minusDays(1).toString(), "count", 132),
            Map.of("date", LocalDate.now().toString(), "count", 126)
        ),
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

  public List<AuditRecord> getAuditRecords(String inspectionNo) {
    return auditRecords.stream()
        .filter(r -> r.inspectionNo().equals(inspectionNo))
        .sorted((a, b) -> b.auditTime().compareTo(a.auditTime()))
        .collect(Collectors.toList());
  }
}
