package com.medtracker.controller;

import com.medtracker.model.Analytics;
import com.medtracker.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping
    public Analytics getAnalytics() {
        return analyticsService.getAnalytics();
    }
}