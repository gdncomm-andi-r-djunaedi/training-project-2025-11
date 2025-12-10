package com.gdn.project.waroenk.gateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for serving the dashboard HTML page.
 */
@Controller
@Tag(name = "Dashboard", description = "Dashboard UI endpoints")
public class DashboardController {

    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard page", description = "Serves the monitoring dashboard HTML page")
    public String dashboard() {
        return "forward:/dashboard.html";
    }
}


