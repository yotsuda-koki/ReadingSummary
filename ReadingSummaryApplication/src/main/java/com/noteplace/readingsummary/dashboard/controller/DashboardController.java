package com.noteplace.readingsummary.dashboard.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.noteplace.readingsummary.auth.security.AuthUser;
import com.noteplace.readingsummary.dashboard.dto.DashboardResponse;
import com.noteplace.readingsummary.dashboard.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

  private final DashboardService dashboardService;

  public DashboardController(DashboardService dashboardService) {
    this.dashboardService = dashboardService;
  }

  @GetMapping
  public DashboardResponse get(@AuthenticationPrincipal AuthUser user) {
    return dashboardService.getDashboard(user.id());
  }
}
