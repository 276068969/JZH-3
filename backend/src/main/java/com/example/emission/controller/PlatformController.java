package com.example.emission.controller;

import com.example.emission.dto.ApiResponse;
import com.example.emission.dto.AuditRequest;
import com.example.emission.dto.StationStatus;
import com.example.emission.dto.WarningHandleRequest;
import com.example.emission.model.Announcement;
import com.example.emission.model.AuditRecord;
import com.example.emission.model.InspectionRecord;
import com.example.emission.model.Station;
import com.example.emission.model.Vehicle;
import com.example.emission.model.WarningRecord;
import com.example.emission.service.DemoDataService;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
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

  public PlatformController(DemoDataService demoDataService) {
    this.demoDataService = demoDataService;
  }

  @GetMapping("/dashboard")
  public Map<String, Object> dashboard(@RequestParam(required = false, defaultValue = "7") Integer days) {
    return demoDataService.dashboard(days);
  }

  @GetMapping("/vehicles/search")
  public ApiResponse<Vehicle> searchVehicle(@RequestParam(required = false) String keyword) {
    return demoDataService.searchVehicleWithValidation(keyword);
  }

  @GetMapping("/inspections")
  public List<InspectionRecord> inspections(@RequestParam(required = false) String plateNumber) {
    return demoDataService.inspections(plateNumber);
  }

  @PostMapping("/inspections/audit")
  public Map<String, Object> audit(@RequestBody AuditRequest request, Authentication authentication) {
    String auditor = authentication != null ? authentication.getName() : "system";
    return demoDataService.audit(request, auditor);
  }

  @PostMapping("/inspections/create")
  public Map<String, Object> createInspection(@RequestBody InspectionRecord record, Authentication authentication) {
    String operator = authentication != null ? authentication.getName() : "station";
    return demoDataService.createInspection(record, operator);
  }

  @GetMapping("/inspections/detail")
  public ResponseEntity<InspectionRecord> inspectionDetail(@RequestParam String inspectionNo) {
    return demoDataService.getInspectionDetail(inspectionNo)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping("/inspections/audit-records")
  public List<AuditRecord> auditRecords(@RequestParam String inspectionNo) {
    return demoDataService.getAuditRecords(inspectionNo);
  }

  @GetMapping("/stations")
  public List<Station> stations(
      @RequestParam(required = false) String district,
      @RequestParam(required = false) String status) {
    return demoDataService.stations(district, status);
  }

  @GetMapping("/stations/status")
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
  public Map<String, Object> createAnnouncement(@RequestBody Announcement announcement, Authentication authentication) {
    String operator = authentication != null ? authentication.getName() : "system";
    return demoDataService.createAnnouncement(announcement, operator);
  }

  @PostMapping("/announcements/update")
  public Map<String, Object> updateAnnouncement(@RequestBody Announcement announcement, Authentication authentication) {
    String operator = authentication != null ? authentication.getName() : "system";
    return demoDataService.updateAnnouncement(announcement, operator);
  }

  @PostMapping("/announcements/delete")
  public Map<String, Object> deleteAnnouncement(@RequestParam Long id) {
    return demoDataService.deleteAnnouncement(id);
  }

  @PostMapping("/announcements/publish")
  public Map<String, Object> publishAnnouncement(@RequestParam Long id, Authentication authentication) {
    String operator = authentication != null ? authentication.getName() : "system";
    return demoDataService.publishAnnouncement(id, operator);
  }

  @PostMapping("/announcements/offline")
  public Map<String, Object> offlineAnnouncement(@RequestParam Long id, Authentication authentication) {
    String operator = authentication != null ? authentication.getName() : "system";
    return demoDataService.offlineAnnouncement(id, operator);
  }

  @GetMapping("/warnings")
  public List<WarningRecord> warnings() {
    return demoDataService.warnings();
  }

  @GetMapping("/warnings/detail")
  public ResponseEntity<WarningRecord> warningDetail(@RequestParam Long id) {
    return demoDataService.getWarningById(id)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping("/warnings/handle")
  public Map<String, Object> handleWarning(@RequestBody WarningHandleRequest request, Authentication authentication) {
    String handler = authentication != null ? authentication.getName() : "system";
    return demoDataService.handleWarning(request, handler);
  }

  @GetMapping("/warnings/inspections")
  public List<InspectionRecord> warningInspections(@RequestParam String plateNumber) {
    return demoDataService.getInspectionsByPlate(plateNumber);
  }
}
