package micro.payment_service.presentation.controllers;


import lombok.RequiredArgsConstructor;
import micro.payment_service.application.service.DLQReprocessorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/payments/dlq")
@RequiredArgsConstructor
public class DLQController {

    private final DLQReprocessorService dlqReprocessorService;

    @RequestMapping("/reprocess")
    public ResponseEntity<String> reprocessDLQ(@RequestParam(defaultValue = "10") int limit){

        int processedCount = dlqReprocessorService.reprocessDLQMessages(limit);

        return ResponseEntity.ok(Map.of("message", "Reprocessamento concluído com sucesso", "processedCount", processedCount).toString());
    }
}
