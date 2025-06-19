//package com.luti.travel.controller;
//
//import com.luti.travel.service.NaverLocalSearchService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/luti")
//@RequiredArgsConstructor
//public class NaverLocalSearchController {
//
//    private final NaverLocalSearchService naverLocalSearchService;
//
//    @GetMapping("/naver/search")
//    public String searchLocal(@RequestParam String keyword) {
//        return naverLocalSearchService.localSearch(keyword);
//    }
//}
