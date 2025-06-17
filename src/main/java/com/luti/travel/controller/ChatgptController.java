package com.luti.travel.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.luti.travel.service.ChatgptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController               // @ResponseBody 반복 제거
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatgptController {

    private final ChatgptService chatgptService;

    /** GPT에게 프롬프트를 보내고 “함수-호출 JSON”만 돌려받는다 */
    @PostMapping("/ask")
    public String askChat(@RequestBody String prompt) throws JsonProcessingException {
        return chatgptService.getChatResponse(prompt);
    }
}
