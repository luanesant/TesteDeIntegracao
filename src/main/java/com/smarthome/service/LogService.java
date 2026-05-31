package com.smarthome.service;

import com.smarthome.entity.LogEntry;
import com.smarthome.repository.LogEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class LogService {

    private final LogEntryRepository logEntryRepository;

    public LogService(LogEntryRepository logEntryRepository) {
        this.logEntryRepository = logEntryRepository;
    }

    @Transactional
    public LogEntry logAction(String action) {
        LogEntry logEntry = new LogEntry(action, LocalDateTime.now());
        return logEntryRepository.save(logEntry);
    }

    public boolean existsByAction(String action) {
        return logEntryRepository.existsByAction(action);
    }
}
