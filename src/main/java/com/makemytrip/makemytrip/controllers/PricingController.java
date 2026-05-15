package com.makemytrip.makemytrip.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.makemytrip.makemytrip.models.PriceFreeze;
import com.makemytrip.makemytrip.models.PriceHistory;
import com.makemytrip.makemytrip.models.SeasonalRule;
import com.makemytrip.makemytrip.repositories.PriceHistoryRepository;
import com.makemytrip.makemytrip.repositories.SeasonalRuleRepository;
import com.makemytrip.makemytrip.services.PriceFreezeService;

@RestController
@RequestMapping("/api/pricing")
@CrossOrigin(origins = "*")
public class PricingController {

    @Autowired
    private PriceHistoryRepository priceHistoryRepository;

    @Autowired
    private SeasonalRuleRepository seasonalRuleRepository;

    @Autowired
    private PriceFreezeService priceFreezeService;

    /**
     * GET /api/pricing/history?entityId={id}&entityType={FLIGHT|HOTEL}
     * Returns chronological price history for the chart.
     */
    @GetMapping("/history")
    public ResponseEntity<List<PriceHistory>> getPriceHistory(
            @RequestParam String entityId,
            @RequestParam String entityType) {
        List<PriceHistory> history = priceHistoryRepository
                .findByEntityIdAndEntityTypeOrderByTimestampAsc(entityId, entityType);
        return ResponseEntity.ok(history);
    }

    /**
     * POST /api/pricing/freeze?userId={}&entityId={}&entityType={}&quantity={}
     * Creates a 24-hour price freeze with tiered fee.
     */
    @PostMapping("/freeze")
    public ResponseEntity<?> freezePrice(
            @RequestParam String userId,
            @RequestParam String entityId,
            @RequestParam String entityType,
            @RequestParam(defaultValue = "1") int quantity) {
        try {
            PriceFreeze freeze = priceFreezeService.freezePrice(userId, entityId, entityType, quantity);
            return ResponseEntity.ok(freeze);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * GET /api/pricing/freeze/check?userId={}&entityId={}
     * Returns the active freeze if it exists, or 404.
     */
    @GetMapping("/freeze/check")
    public ResponseEntity<?> checkFreeze(
            @RequestParam String userId,
            @RequestParam String entityId) {
        PriceFreeze freeze = priceFreezeService.getActiveFreeze(userId, entityId);
        if (freeze != null) {
            return ResponseEntity.ok(freeze);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * GET /api/pricing/freeze/fee?quantity={}
     * Returns the freeze fee for a given quantity (preview before freezing).
     */
    @GetMapping("/freeze/fee")
    public ResponseEntity<Double> getFreezeFee(@RequestParam(defaultValue = "1") int quantity) {
        return ResponseEntity.ok(priceFreezeService.calculateFreezeFee(quantity));
    }

    /**
     * GET /api/pricing/rules
     * Returns all seasonal pricing rules (admin use).
     */
    @GetMapping("/rules")
    public ResponseEntity<List<SeasonalRule>> getAllRules() {
        return ResponseEntity.ok(seasonalRuleRepository.findAll());
    }

    /**
     * POST /api/pricing/rules
     * Creates a new seasonal pricing rule (admin use).
     */
    @PostMapping("/rules")
    public ResponseEntity<SeasonalRule> addRule(@RequestBody SeasonalRule rule) {
        return ResponseEntity.ok(seasonalRuleRepository.save(rule));
    }
}
