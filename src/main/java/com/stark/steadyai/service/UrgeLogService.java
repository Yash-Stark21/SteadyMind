package com.stark.steadyai.service;

import com.stark.steadyai.dto.UrgeLogRequest;
import com.stark.steadyai.dto.UrgeLogResponse;
import com.stark.steadyai.entity.UrgeLog;
import com.stark.steadyai.entity.User;
import com.stark.steadyai.exception.ResourceNotFoundException;
import com.stark.steadyai.repository.UrgeLogRepository;
import com.stark.steadyai.repository.UserRepository;
import com.stark.steadyai.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UrgeLogService {

    private final UrgeLogRepository urgeLogRepository;
    private final UserRepository userRepository;

    public UrgeLogService(UrgeLogRepository urgeLogRepository, UserRepository userRepository) {
        this.urgeLogRepository = urgeLogRepository;
        this.userRepository = userRepository;
    }



    public UrgeLogResponse createUrgeLog(UrgeLogRequest request) {
        User user = SecurityUtils.getCurrentUser();

        UrgeLog urgeLog = new UrgeLog();
        urgeLog.setUser(user);
        urgeLog.setTriggerText(request.getTriggerText());
        urgeLog.setObsessionText(request.getObsessionText());
        urgeLog.setCompulsionUrge(request.getCompulsionUrge());
        urgeLog.setIntensityBefore(request.getIntensityBefore());
        urgeLog.setDelayMinutes(request.getDelayMinutes());
        urgeLog.setIntensityAfter(request.getIntensityAfter());
        urgeLog.setCompulsionPerformed(request.getCompulsionPerformed());

        UrgeLog savedLog = urgeLogRepository.save(urgeLog);
        return mapToResponse(savedLog);
    }

    public List<UrgeLogResponse> getAllUrgeLogs() {
        User user = SecurityUtils.getCurrentUser();
        List<UrgeLog> logs = urgeLogRepository.findByUserOrderByCreatedAtDesc(user);
        return logs.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public UrgeLogResponse getUrgeLogById(Long id) {
        User user = SecurityUtils.getCurrentUser();
        UrgeLog log = urgeLogRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Urge log not found with id " + id));
        return mapToResponse(log);
    }

    public UrgeLogResponse updateUrgeLog(Long id, UrgeLogRequest request) {
        User user = SecurityUtils.getCurrentUser();
        UrgeLog log = urgeLogRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Urge log not found with id " + id));

        log.setTriggerText(request.getTriggerText());
        log.setObsessionText(request.getObsessionText());
        log.setCompulsionUrge(request.getCompulsionUrge());
        log.setIntensityBefore(request.getIntensityBefore());
        log.setDelayMinutes(request.getDelayMinutes());
        log.setIntensityAfter(request.getIntensityAfter());
        log.setCompulsionPerformed(request.getCompulsionPerformed());

        UrgeLog updatedLog = urgeLogRepository.save(log);
        return mapToResponse(updatedLog);
    }

    public void deleteUrgeLog(Long id) {
        User user = SecurityUtils.getCurrentUser();
        UrgeLog log = urgeLogRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Urge log not found with id " + id));
        urgeLogRepository.delete(log);
    }

    private UrgeLogResponse mapToResponse(UrgeLog log) {
        UrgeLogResponse response = new UrgeLogResponse();
        response.setId(log.getId());
        response.setTriggerText(log.getTriggerText());
        response.setObsessionText(log.getObsessionText());
        response.setCompulsionUrge(log.getCompulsionUrge());
        response.setIntensityBefore(log.getIntensityBefore());
        response.setDelayMinutes(log.getDelayMinutes());
        response.setIntensityAfter(log.getIntensityAfter());
        response.setCompulsionPerformed(log.getCompulsionPerformed());
        response.setCreatedAt(log.getCreatedAt());
        return response;
    }
}
