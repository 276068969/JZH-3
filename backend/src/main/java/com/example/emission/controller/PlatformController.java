package com.example.emission.controller;

import com.example.emission.dto.StationStatus;
import com.example.emission.model.Announcement;
import com.example.emission.model.InspectionRecord;
import com.example.emission.model.Station;
import com.example.emission.model.Vehicle;
import com.example.emission.model.WarningRecord;
import com.example.emission.service.DemoDataService;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
  public ResponseEntity<Vehicle> searchVehicle(@RequestParam String keyword) {
    return demoDataService.searchVehicle(keyword)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping("/inspections")
  public List<InspectionRecord> inspections(@RequestParam(required = false) String plateNumber) {
    return demoDataService.inspections(plateNumber);
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
