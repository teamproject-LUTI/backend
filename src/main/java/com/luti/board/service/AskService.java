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
     *
     * @param page            1-based page 번호
     * @param size            페이지 사이즈
     * @param userId   현재 로그인된 사용자의 ID
     */
    public MultiResponseDto<AskResponseDto> getAsks(int page, int size, Long userId) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Ask> asks = askRepository.findAll(pageable);

        // owner 플래그를 셋팅하면서 DTO 변환
        var dtos = asks.stream()
                .map(ask -> AskResponseDto.of(ask, userId))
                .collect(Collectors.toList());

        return new MultiResponseDto<>(dtos, asks);
    }
    /**
     * 단일 문의글 조회
     *
     * @param askId           조회할 문의글 ID
     * @param userId   현재 로그인된 사용자의 ID
     */
    public SingleResponseDto<AskResponseDto> getAsk(Long askId, Long userId) {
        Ask ask = askRepository.findById(askId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의글입니다. id=" + askId));

        return new SingleResponseDto<>(AskResponseDto.of(ask, userId));
    }


    /**
     * 문의글 등록
     *
     * @param userId  작성자 User ID (현재 로그인된 사용자)
     * @param dto     요청 DTO
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
        // 등록 후에도 본인이 작성자이므로 owner=true
        return new SingleResponseDto<>(AskResponseDto.of(saved, userId));
    }

    /**
     * 문의글 수정
     *
     * @param askId           수정할 문의글 ID
     * @param dto             수정할 데이터 DTO
     * @param userId   현재 로그인된 사용자의 ID
     */
    @Transactional
    public SingleResponseDto<AskResponseDto> updateAsk(Long askId,
                                                       AskRequestDto dto,
                                                       Long userId) {
        Ask ask = askRepository.findById(askId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의글입니다. id=" + askId));

        // (권한 체크는 컨트롤러나 AOP로 분리하셔도 좋습니다)
        if (!ask.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인 글만 수정할 수 있습니다.");
        }

        ask.setTitle(dto.getTitle());
        ask.setContent(dto.getContent());
        // Auditable이 자동으로 modifiedAt 갱신

        return new SingleResponseDto<>(AskResponseDto.of(ask, userId));
    }

    /**
     * 문의글 삭제
     *
     * @param askId           삭제할 문의글 ID
     * @param userId   현재 로그인된 사용자의 ID
     */
    @Transactional
    public void deleteAsk(Long askId, Long userId) {
        Ask ask = askRepository.findById(askId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의글입니다. id=" + askId));

        if (!ask.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인 글만 삭제할 수 있습니다.");
        }

        askRepository.delete(ask);
    }
}
