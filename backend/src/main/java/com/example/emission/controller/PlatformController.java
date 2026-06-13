package com.example.emission.controller;

import com.example.emission.dto.ApiResponse;
import com.example.emission.dto.AuditRequest;
import com.example.emission.dto.StationStatus;
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
  public Map<String, Object> dashboard() {
    return demoDataService.dashboard();
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
  public List<Station> stations() {
    return demoDataService.stations();
  }

  @GetMapping("/stations/status")
  public List<StationStatus> stationStatuses() {
    return demoDataService.stationStatuses();
  }

  @GetMapping("/announcements")
  public List<Announcement> announcements() {
    return demoDataService.announcements();
  }

  @GetMapping("/warnings")
  public List<WarningRecord> warnings() {
    return demoDataService.warnings();
  }
}
