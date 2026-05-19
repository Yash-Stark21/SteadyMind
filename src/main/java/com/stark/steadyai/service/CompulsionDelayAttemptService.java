package com.stark.steadyai.service;

import com.stark.steadyai.dto.CompleteDelayAttemptRequest;
import com.stark.steadyai.dto.CompulsionDelayAttemptRequest;
import com.stark.steadyai.dto.CompulsionDelayAttemptResponse;
import com.stark.steadyai.dto.CompulsionDelayAttemptUpdateRequest;

import java.util.List;

public interface CompulsionDelayAttemptService {

    CompulsionDelayAttemptResponse createDelayAttempt(CompulsionDelayAttemptRequest request);

    List<CompulsionDelayAttemptResponse> getAllDelayAttemptsForCurrentUser();

    CompulsionDelayAttemptResponse getDelayAttemptById(Long id);

    CompulsionDelayAttemptResponse updateDelayAttempt(Long id, CompulsionDelayAttemptUpdateRequest request);

    CompulsionDelayAttemptResponse completeDelayAttempt(Long id, CompleteDelayAttemptRequest request);

    CompulsionDelayAttemptResponse cancelDelayAttempt(Long id);

    void deleteDelayAttempt(Long id);
}
