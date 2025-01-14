package com.fashionNav.controller;

import com.fashionNav.common.api.Api;
import com.fashionNav.common.error.UserErrorCode;
import com.fashionNav.common.success.SuccessCode;
import com.fashionNav.exception.ApiException;
import com.fashionNav.model.dto.request.UserSurveyRequest;
import com.fashionNav.model.dto.response.UserSurveyResponse;
import com.fashionNav.model.entity.Brand;
import com.fashionNav.model.entity.Style;
import com.fashionNav.model.entity.User;
import com.fashionNav.model.entity.UserSurvey;
import com.fashionNav.service.UserSurveyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
/**
 * UserSurveyController 클래스는 사용자 설문조사와 관련된 API를 제공합니다.
 * 이 클래스는 사용자 설문조사를 생성, 조회, 업데이트, 삭제하는 기능을 포함합니다.
 * 또한 인증된 사용자의 설문조사 목록을 조회할 수 있습니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
public class UserSurveyController {
    private final UserSurveyService userSurveyService;

    @GetMapping
    public Api<List<UserSurveyResponse>> getAllUserSurveys() {
        List<UserSurvey> userSurveys = userSurveyService.getAllUserSurveys();
        List<UserSurveyResponse> responses = userSurveys.stream()
                .map(survey -> new UserSurveyResponse(survey,
                        userSurveyService.findStylesBySurveyId(survey.getSurveyId()),
                        userSurveyService.findBrandsBySurveyId(survey.getSurveyId())))
                .collect(Collectors.toList());
        return Api.OK(responses, SuccessCode.OK);
    }

    @PostMapping
    public Api<Void> createUserSurvey(@RequestBody UserSurveyRequest request, Authentication authentication) {
        Long userId = ((User) authentication.getPrincipal()).getUserId();

        // Check if the user already has a survey
        if (userSurveyService.userHasSurvey(userId)) {
            throw new ApiException(UserErrorCode.USER_NOT_ALLOWED);

        }

        userSurveyService.createUserSurvey(request.getUserSurvey(), request.getStyleIds(), request.getBrandIds());
        return Api.OK(SuccessCode.OK);
    }




    @GetMapping("/{surveyId}")
    public Api<UserSurveyResponse> getUserSurveyById(@PathVariable("surveyId") Long surveyId) {

        UserSurvey userSurvey = userSurveyService.getUserSurveyById(surveyId);
        List<Style> styles = userSurveyService.findStylesBySurveyId(surveyId);
        List<Brand> brands = userSurveyService.findBrandsBySurveyId(surveyId);
        UserSurveyResponse response = new UserSurveyResponse(userSurvey, styles, brands);


        return Api.OK(response);
    }

    @PutMapping("/{surveyId}")
    public Api<Void> updateUserSurvey(@PathVariable("surveyId") Long surveyId, @RequestBody UserSurveyRequest request) {
        UserSurvey userSurvey = request.getUserSurvey();
        userSurvey.setSurveyId(surveyId); // Set the surveyId to the correct value
        userSurveyService.updateUserSurvey(userSurvey, request.getStyleIds(), request.getBrandIds());
        return Api.OK(SuccessCode.OK);
    }

    @DeleteMapping("/{surveyId}")
    public Api<Void> deleteUserSurvey(@PathVariable("surveyId") Long surveyId) {
        userSurveyService.deleteUserSurvey(surveyId);
        return Api.OK(SuccessCode.OK);
    }

    @GetMapping("/user")
    public Api<List<UserSurveyResponse>> getUserSurveysByUserId(Authentication authentication) {
        Long userId = ((User) authentication.getPrincipal()).getUserId();
        List<UserSurvey> userSurveys = userSurveyService.getUserSurveysByUserId(userId);
        List<UserSurveyResponse> responses = userSurveys.stream()
                .map(survey -> new UserSurveyResponse(survey,
                        userSurveyService.findStylesBySurveyId(survey.getSurveyId()),
                        userSurveyService.findBrandsBySurveyId(survey.getSurveyId())))
                .collect(Collectors.toList());
        return Api.OK(responses);
    }
}
