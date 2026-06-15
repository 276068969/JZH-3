package com.example.emission.controller;

import com.example.emission.dto.ApiResponse;
import com.example.emission.dto.AuditRequest;
import com.example.emission.dto.EnvironmentalJudgmentResult;
import com.example.emission.dto.StationStatus;
import com.example.emission.dto.WarningHandleRequest;
import com.example.emission.model.Announcement;
import com.example.emission.model.AuditRecord;
import com.example.emission.model.InspectionRecord;
import com.example.emission.model.PollutantLimitRule;
import com.example.emission.model.Station;
import com.example.emission.model.UserAccount;
import com.example.emission.model.Vehicle;
import com.example.emission.model.WarningRecord;
import com.example.emission.service.DemoDataService;
import com.example.emission.service.EmissionJudgmentService;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class PlatformController {
  private final DemoDataService demoDataService;
  private final EmissionJudgmentService emissionJudgmentService;

  public PlatformController(DemoDataService demoDataService, EmissionJudgmentService emissionJudgmentService) {
    this.demoDataService = demoDataService;
    this.emissionJudgmentService = emissionJudgmentService;
  }

  private String resolveUsername(Authentication authentication) {
    if (authentication != null && authentication.getDetails() instanceof UserAccount user) {
      return user.displayName();
    }
    return "system";
  }

  private String resolveUsernameFromAuth(Authentication authentication) {
    if (authentication != null && authentication.getDetails() instanceof UserAccount user) {
      return user.username();
    }
    return "";
  }

  @GetMapping("/dashboard")
  @PreAuthorize("hasAnyRole('平台管理员', '监管人员', '检测站工作人员')")
  public Map<String, Object> dashboard(@RequestParam(required = false, defaultValue = "7") Integer days) {
    return demoDataService.dashboard(days);
  }

  @GetMapping("/vehicles/search")
  public ApiResponse<Vehicle> searchVehicle(@RequestParam(required = false) String keyword) {
    return demoDataService.searchVehicleWithValidation(keyword);
  }

  @GetMapping("/user/vehicle-center")
  public Map<String, Object> userVehicleCenter(Authentication authentication) {
    String username = resolveUsernameFromAuth(authentication);
    return demoDataService.getUserVehicleCenter(username);
  }

  @GetMapping("/user/vehicles")
  public List<Vehicle> userVehicles(Authentication authentication) {
    String username = resolveUsernameFromAuth(authentication);
    return demoDataService.getUserVehicles(username);
  }

  @GetMapping("/user/vehicles/inspections")
  public List<InspectionRecord> userVehicleInspections(
      @RequestParam String plateNumber,
      Authentication authentication) {
    String username = resolveUsernameFromAuth(authentication);
    return demoDataService.getUserVehicleInspections(username, plateNumber);
  }

  @GetMapping("/inspections")
  @PreAuthorize("hasAnyRole('平台管理员', '监管人员', '检测站工作人员')")
  public List<InspectionRecord> inspections(@RequestParam(required = false) String plateNumber) {
    return demoDataService.inspections(plateNumber);
  }

  @PostMapping("/inspections/audit")
  @PreAuthorize("hasAnyRole('平台管理员', '监管人员')")
  public Map<String, Object> audit(@RequestBody AuditRequest request, Authentication authentication) {
    return demoDataService.audit(request, resolveUsername(authentication));
  }

  @PostMapping("/inspections/create")
  @PreAuthorize("hasAnyRole('平台管理员', '检测站工作人员')")
  public Map<String, Object> createInspection(@RequestBody InspectionRecord record, Authentication authentication) {
    return demoDataService.createInspection(record, resolveUsername(authentication));
  }

  @PostMapping("/inspections/judge")
  @PreAuthorize("hasAnyRole('平台管理员', '监管人员', '检测站工作人员')")
  public ResponseEntity<EnvironmentalJudgmentResult> judgeEnvironmentalStatus(
          @RequestBody InspectionRecord record,
          @RequestParam(required = false) String fuelType,
          @RequestParam(required = false) String emissionStandard) {
    EnvironmentalJudgmentResult result = emissionJudgmentService.judge(record, fuelType, emissionStandard);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/inspections/judge")
  public ResponseEntity<EnvironmentalJudgmentResult> judgeEnvironmentalStatusByNo(
          @RequestParam String inspectionNo,
          @RequestParam(required = false) String fuelType,
          @RequestParam(required = false) String emissionStandard,
          Authentication authentication) {
    String username = resolveUsernameFromAuth(authentication);
    if (!demoDataService.canAccessInspection(inspectionNo, username)) {
      return ResponseEntity.notFound().build();
    }
    return demoDataService.getInspectionDetail(inspectionNo)
        .map(record -> ResponseEntity.ok(emissionJudgmentService.judge(record, fuelType, emissionStandard)))
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping("/inspections/detail")
  public ResponseEntity<InspectionRecord> inspectionDetail(
      @RequestParam String inspectionNo,
      Authentication authentication) {
    String username = resolveUsernameFromAuth(authentication);
    return demoDataService.getInspectionDetailAccessible(inspectionNo, username)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping("/inspections/audit-records")
  public List<AuditRecord> auditRecords(
      @RequestParam String inspectionNo,
      Authentication authentication) {
    String username = resolveUsernameFromAuth(authentication);
    if (!demoDataService.canAccessInspection(inspectionNo, username)) {
      return List.of();
    }
    return demoDataService.getAuditRecords(inspectionNo);
  }

  @GetMapping("/stations")
  @PreAuthorize("hasAnyRole('平台管理员', '监管人员')")
  public List<Station> stations(
      @RequestParam(required = false) String district,
      @RequestParam(required = false) String status) {
    return demoDataService.stations(district, status);
  }

  @GetMapping("/stations/status")
  @PreAuthorize("hasAnyRole('平台管理员', '监管人员')")
  public List<StationStatus> stationStatuses() {
    return demoDataService.stationStatuses();
  }

  @GetMapping("/announcements")
  public List<Announcement> announcements() {
    return demoDataService.announcements();
  }

  @GetMapping("/announcements/list")
  public Map<String, Object> announcementList(
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer pageSize,
      @RequestParam(required = false) String title,
      @RequestParam(required = false) String type,
      @RequestParam(required = false) String publishStatus,
      @RequestParam(required = false) String publisher,
      @RequestParam(required = false) String startTime,
      @RequestParam(required = false) String endTime) {
    return demoDataService.getAnnouncementList(page, pageSize, title, type, publishStatus, publisher, startTime, endTime);
  }

  @GetMapping("/announcements/detail")
  public ResponseEntity<Announcement> announcementDetail(@RequestParam Long id) {
    return demoDataService.getAnnouncementById(id)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping("/announcements/create")
  @PreAuthorize("hasRole('平台管理员')")
  public Map<String, Object> createAnnouncement(@RequestBody Announcement announcement, Authentication authentication) {
    return demoDataService.createAnnouncement(announcement, resolveUsername(authentication));
  }

  @PostMapping("/announcements/update")
  @PreAuthorize("hasRole('平台管理员')")
  public Map<String, Object> updateAnnouncement(@RequestBody Announcement announcement, Authentication authentication) {
    return demoDataService.updateAnnouncement(announcement, resolveUsername(authentication));
  }

  @PostMapping("/announcements/delete")
  @PreAuthorize("hasRole('平台管理员')")
  public Map<String, Object> deleteAnnouncement(@RequestParam Long id) {
    return demoDataService.deleteAnnouncement(id);
  }

  @PostMapping("/announcements/publish")
  @PreAuthorize("hasRole('平台管理员')")
  public Map<String, Object> publishAnnouncement(@RequestParam Long id, Authentication authentication) {
    return demoDataService.publishAnnouncement(id, resolveUsername(authentication));
  }

  @PostMapping("/announcements/offline")
  @PreAuthorize("hasRole('平台管理员')")
  public Map<String, Object> offlineAnnouncement(@RequestParam Long id, Authentication authentication) {
    return demoDataService.offlineAnnouncement(id, resolveUsername(authentication));
  }

  @GetMapping("/warnings")
  @PreAuthorize("hasAnyRole('平台管理员', '监管人员')")
  public List<WarningRecord> warnings() {
    return demoDataService.warnings();
  }

  @GetMapping("/warnings/detail")
  @PreAuthorize("hasAnyRole('平台管理员', '监管人员')")
  public ResponseEntity<WarningRecord> warningDetail(@RequestParam Long id) {
    return demoDataService.getWarningById(id)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping("/warnings/handle")
  @PreAuthorize("hasAnyRole('平台管理员', '监管人员')")
  public Map<String, Object> handleWarning(@RequestBody WarningHandleRequest request, Authentication authentication) {
    return demoDataService.handleWarning(request, resolveUsername(authentication));
  }

  @GetMapping("/warnings/inspections")
  @PreAuthorize("hasAnyRole('平台管理员', '监管人员')")
  public List<InspectionRecord> warningInspections(@RequestParam String plateNumber) {
    return demoDataService.getInspectionsByPlate(plateNumber);
  }

  @GetMapping("/pollutant-limit-rules/list")
  @PreAuthorize("hasAnyRole('平台管理员', '监管人员')")
  public Map<String, Object> pollutantLimitRuleList(
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer pageSize,
      @RequestParam(required = false) String fuelType,
      @RequestParam(required = false) String emissionStandard,
      @RequestParam(required = false) String status) {
    return demoDataService.getPollutantLimitRuleList(page, pageSize, fuelType, emissionStandard, status);
  }

  @GetMapping("/pollutant-limit-rules/all")
  @PreAuthorize("hasAnyRole('平台管理员', '监管人员')")
  public List<PollutantLimitRule> allPollutantLimitRules() {
    return demoDataService.getAllPollutantLimitRules();
  }

  @GetMapping("/pollutant-limit-rules/detail")
  @PreAuthorize("hasAnyRole('平台管理员', '监管人员')")
  public ResponseEntity<PollutantLimitRule> pollutantLimitRuleDetail(@RequestParam Long id) {
    return demoDataService.getPollutantLimitRuleById(id)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping("/pollutant-limit-rules/query")
  public ResponseEntity<PollutantLimitRule> queryPollutantLimitRule(
      @RequestParam String fuelType,
      @RequestParam String emissionStandard) {
    return demoDataService.getPollutantLimitRule(fuelType, emissionStandard)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping("/pollutant-limit-rules/create")
  @PreAuthorize("hasRole('平台管理员')")
  public Map<String, Object> createPollutantLimitRule(@RequestBody PollutantLimitRule rule) {
    return demoDataService.createPollutantLimitRule(rule);
  }

  @PostMapping("/pollutant-limit-rules/update")
  @PreAuthorize("hasRole('平台管理员')")
  public Map<String, Object> updatePollutantLimitRule(@RequestBody PollutantLimitRule rule) {
    return demoDataService.updatePollutantLimitRule(rule);
  }

  @PostMapping("/pollutant-limit-rules/delete")
  @PreAuthorize("hasRole('平台管理员')")
  public Map<String, Object> deletePollutantLimitRule(@RequestParam Long id) {
    return demoDataService.deletePollutantLimitRule(id);
  }

  @GetMapping("/pollutant-limit-rules/fuel-types")
  @PreAuthorize("hasAnyRole('平台管理员', '监管人员')")
  public List<String> fuelTypes() {
    return demoDataService.getAllFuelTypes();
  }

  @GetMapping("/pollutant-limit-rules/emission-standards")
  @PreAuthorize("hasAnyRole('平台管理员', '监管人员')")
  public List<String> emissionStandards() {
    return demoDataService.getAllEmissionStandards();
  }
}
