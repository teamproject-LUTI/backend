package com.luti.board.service;

import com.luti.auth.entity.User;
import com.luti.auth.repository.UserRepository;
import com.luti.board.dto.AskRequestDto;
import com.luti.board.dto.AskResponseDto;
import com.luti.board.entity.Ask;
import com.luti.board.repository.AskRepository;
import com.luti.dto.MultiResponseDto;
import com.luti.dto.SingleResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AskService {

    private final AskRepository askRepository;
    private final UserRepository userRepository;

    /**
     * 문의글 목록 조회 (페이징)
     */
    public MultiResponseDto<AskResponseDto> getAsks(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Ask> asks = askRepository.findAll(pageable);

        var dtos = asks.stream()
                .map(AskResponseDto::of)
                .collect(Collectors.toList());

        return new MultiResponseDto<>(dtos, asks);
    }

    /**
     * 단일 문의글 조회
     */
    public SingleResponseDto<AskResponseDto> getAsk(Long askId) {
        Ask ask = askRepository.findById(askId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의글입니다. id=" + askId));

        return new SingleResponseDto<>(AskResponseDto.of(ask));
    }

    /**
     * 문의글 등록
     */
    @Transactional
    public SingleResponseDto<AskResponseDto> createAsk(Long userId, AskRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. id=" + userId));

        Ask ask = Ask.builder()
                .user(user)
                .title(dto.getTitle())
                .content(dto.getContent())
                .build();

        Ask saved = askRepository.save(ask);
        return new SingleResponseDto<>(AskResponseDto.of(saved));
    }

    /**
     * 문의글 수정
     */
    @Transactional
    public SingleResponseDto<AskResponseDto> updateAsk(Long askId, AskRequestDto dto) {
        Ask ask = askRepository.findById(askId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의글입니다. id=" + askId));

        ask.setTitle(dto.getTitle());
        ask.setContent(dto.getContent());
        // Auditable이 자동으로 modifiedAt 갱신

        return new SingleResponseDto<>(AskResponseDto.of(ask));
    }

    /**
     * 문의글 삭제
     */
    @Transactional
    public void deleteAsk(Long askId) {
        Ask ask = askRepository.findById(askId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의글입니다. id=" + askId));
        askRepository.delete(ask);
    }
}
