package com.medtracker.controller;

import com.medtracker.model.UsageHistory;
import com.medtracker.service.UsageHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usage-history")
public class UsageHistoryController {

    @Autowired
    private UsageHistoryService usageHistoryService;

    @GetMapping
    public List<UsageHistory> getAll() {
        return usageHistoryService.getAll();
    }
}