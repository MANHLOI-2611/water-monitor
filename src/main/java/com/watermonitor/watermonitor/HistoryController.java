
package com.watermonitor.watermonitor;

import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api")
public class HistoryController {

    private final List<HistoryEvent> history = new ArrayList<>();
    private String lastStatus = "";

    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public record StatusRequest(String status) {}

    public record HistoryEvent(
            String time,
            String status,
            String message
    ) {}

    @PostMapping("/status")
    public synchronized String receiveStatus(
            @RequestBody StatusRequest request) {

        if (request == null || request.status() == null) {
            return "Invalid status";
        }

        String status = request.status().toUpperCase();

        if (!status.equals("SAFE")
                && !status.equals("FLOOD")
                && !status.equals("LANDSLIDE")
                && !status.equals("BOTH")) {
            return "Unknown status";
        }

        if (!status.equals(lastStatus)) {

            String message = switch (status) {
                case "SAFE" -> "An toàn";
                case "FLOOD" -> "Ngập";
                case "LANDSLIDE" -> "Sạt lở";
                case "BOTH" -> "Ngập và sạt lở";
                default -> status;
            };

            String time = LocalDateTime.now(
                    ZoneId.of("Asia/Ho_Chi_Minh")
            ).format(formatter);

            history.add(new HistoryEvent(time, status, message));

            if (history.size() > 500) {
                history.remove(0);
            }

            lastStatus = status;
        }

        return "OK";
    }

    @GetMapping("/history")
    public synchronized List<HistoryEvent> getHistory() {
        List<HistoryEvent> result = new ArrayList<>(history);
        Collections.reverse(result);
        return result;
    }
}
