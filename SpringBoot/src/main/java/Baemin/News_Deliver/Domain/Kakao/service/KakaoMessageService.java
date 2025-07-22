package Baemin.News_Deliver.Domain.Kakao.service;

import Baemin.News_Deliver.Domain.Auth.Entity.User;
import Baemin.News_Deliver.Domain.Auth.Repository.UserRepository;
import Baemin.News_Deliver.Domain.Kakao.Exception.KakaoException;
import Baemin.News_Deliver.Domain.Kakao.entity.History;
import Baemin.News_Deliver.Domain.Kakao.repository.HistoryRepository;
import Baemin.News_Deliver.Domain.Kakao.repository.NewsRepository;
import Baemin.News_Deliver.Domain.Mypage.DTO.SettingDTO;
import Baemin.News_Deliver.Domain.Mypage.Entity.Setting;
import Baemin.News_Deliver.Domain.Mypage.Repository.SettingRepository;
import Baemin.News_Deliver.Domain.Mypage.service.SettingService;
import Baemin.News_Deliver.Global.Exception.ErrorCode;
import Baemin.News_Deliver.Global.Kakao.KakaoTokenProvider;
import Baemin.News_Deliver.Global.News.Batch.entity.News;
import Baemin.News_Deliver.Global.News.ElasticSearch.dto.NewsEsDocument;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * KakaoMessageService는 사용자의 키워드 기반으로 뉴스를 조회하고,
 * 해당 뉴스를 카카오톡 메시지로 전송하는 기능을 담당하는 서비스입니다.
 *
 * 주요 기능:
 * <ul>
 *     <li>유저의 AccessToken 갱신 및 조회</li>
 *     <li>뉴스 검색 및 사용자 맞춤 필터링</li>
 *     <li>카카오톡 템플릿 메시지 전송</li>
 *     <li>전송된 뉴스에 대한 히스토리 저장</li>
 * </ul>
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoMessageService {

    private final KakaoTokenProvider provider;
    private final RestTemplate restTemplate = new RestTemplate();
    private final KakaoNewsService newsService;
    private final SettingService settingService;
    private final NewsRepository newsRepository;
    private final SettingRepository settingRepository;
    private final HistoryRepository historyRepository;

    private static final String KAKAO_SEND_TOME_URL = "https://kapi.kakao.com/v2/api/talk/memo/send";

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;
    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String kakaoClientSecret;


    /**
     * 카카오 사용자 Access Token을 Refresh Token을 이용해 갱신 후 반환합니다.
     *
     * @param refreshAccessToken 사용자의 카카오 Refresh Token
     * @param userId             사용자 고유 ID
     * @return 갱신된 Access Token 문자열
     * @throws RuntimeException Access Token을 가져오지 못했을 경우
     */
    public String getKakaoUserAccessToken(String refreshAccessToken, Long userId) {

        //유저의 accesstoken을 가져 올 것
        String accessToken = provider.refreshAccessToken(refreshAccessToken);
        if (accessToken == null || accessToken.isEmpty()) {
            throw new KakaoException(ErrorCode.KAKAO_TOKEN_ACCESS_FAILED);
        }

        getNewsEsDocumentList_Fixed(userId);
        return accessToken;
    }


    /**
     * 카카오 메시지 전송 메서드
     *
     * @param refreshAccessToken 유저의 리프레시 토큰
     * @param userId 유저의 고유 번호
     * @return T,F
     */
    public boolean sendKakaoMessage(String refreshAccessToken, Long userId) {

        /* 유저의 세팅 리스트 반환 */
        log.info("refreshAccessToken 발급 결과 :{}", refreshAccessToken);
        String accessToken = getKakaoUserAccessToken(refreshAccessToken, userId);
        log.info("accessToken 발급 결과:{}", accessToken);
        List<SettingDTO> settings = settingService.getAllSettingsByUserId(userId);
        boolean anySuccess = false;

        /* Setting을 순회하며 뉴스 리스트 저장&전송 */
        for (SettingDTO setting : settings) {
            List<NewsEsDocument> newsList = newsService.searchNews(
                    setting.getSettingKeywords(), setting.getBlockKeywords());

            if (newsList == null || newsList.isEmpty()) {
                log.info("세팅 ID {}에 해당하는 뉴스가 없음", setting.getId());
                continue;
            }

            if (newsList.size() > 5) {
                newsList = newsList.subList(0, 5);
            }

            saveHistory(newsList, List.of(setting));
            boolean success = sendSingleKakaoMessage(accessToken, newsList);
            anySuccess = anySuccess || success;
        }

        return anySuccess;
    }

    /**
     * 개별 카카오톡 전송 메섣,
     *
     * @param accessToken 유저 엑세스 토큰
     * @param newsList 뉴스 리스트
     * @return T,F
     */
    private boolean sendSingleKakaoMessage(String accessToken, List<NewsEsDocument> newsList) {
        try {

            /*  헤더 설정 */
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Bearer " + accessToken);

            /* 템플릿 설정 */
            Map<String, String> templateArgs = createTemplateData(newsList);
            ObjectMapper objectMapper = new ObjectMapper();
            String templateArgsJson = objectMapper.writeValueAsString(templateArgs);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("template_id", "122080");
            params.add("template_args", templateArgsJson);

            /* 세팅 별 개별 메시지 전송 */
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(KAKAO_SEND_TOME_URL, entity, String.class);
            log.info("카카오 메시지 전송 응답: {}", response.getBody());

            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.error("카카오 메시지 전송 실패: ", e);
            return false;
        }
    }

    /*
     * 사용자의 키워드를 바탕으로 뉴스를 검색하여 리스트로 반환하는 메서드
     *
     * Edit By : 성열
     * When : 2025-07-20
     * Why : 히스토리 DB에 2개의 세팅이 합쳐져서 뉴스가 발송되는 오류 해결
     *
     * Deprecated된 메서드는 하단에 정리하였습니다.
     *
     * @param userId 유저의 고유 번호
     * @return 각 세팅에 맞는 뉴스 기사 리스트
     */
    private List<NewsEsDocument> getNewsEsDocumentList_Fixed(Long userId) {
        List<SettingDTO> settings = settingService.getAllSettingsByUserId(userId);
        List<NewsEsDocument> totalNewsList = new ArrayList<>();

        for (SettingDTO setting : settings) {
            log.info("셋팅값 확인용 코드 : " + setting.getSettingKeywords());
            log.info("셋팅 제외 확인용 코드 : " + setting.getBlockKeywords());

            List<String> keywords = setting.getSettingKeywords();  // 예: [이재명]
            List<String> blockKeywords = setting.getBlockKeywords(); // 예: [한국, 중국]

            if (keywords == null || keywords.isEmpty()) {
                log.warn("세팅에 키워드가 없습니다. 스킵합니다.");
                continue;
            }

            List<NewsEsDocument> newsList = newsService.searchNews(keywords, blockKeywords);

            log.info(">> 세팅당 검색된 뉴스 수: {}", newsList.size());

            // 세팅당 5개만 취하고 싶다면 limit 적용
            if (newsList.size() > 5) {
                newsList = newsList.subList(0, 5);
            }

            // 뉴스 히스토리 저장
            saveHistory(newsList, List.of(setting)); // 단일 setting 기준

            totalNewsList.addAll(newsList);
        }

        log.info("✅ 전체 검색된 뉴스 총합: {}", totalNewsList.size());
        return totalNewsList;
    }

    /**
     * 뉴스 리스트를 템플릿 전송용 파라미터(Map)로 변환합니다.
     * 최대 5개의 뉴스만 포함됩니다.
     *
     * @param newsList 뉴스 리스트
     * @return 템플릿에 들어갈 파라미터 Map
     */
    private static Map<String, String> createTemplateData(List<NewsEsDocument> newsList) {

        log.info("뉴스 전체 리스트 확인:" + newsList);
        Map<String, String> templateArgs = new HashMap<>();

        //메세지 5개 고정
        for (int i = 0; i < Math.min(5, newsList.size()); i++) {
            NewsEsDocument news = newsList.get(i);
            templateArgs.put("SUMMARY" + (i + 1), news.getSummary());
            templateArgs.put("PUBLISHER" + (i + 1), news.getPublisher());
        }
        return templateArgs;

    }

    /**
     * 전송된 뉴스 정보를 히스토리로 저장합니다. 중복 뉴스는 저장하지 않습니다.
     *
     * @param newsList  뉴스 리스트
     * @param settings  해당 뉴스에 적용된 사용자 설정들
     * @return 저장이 이루어진 경우 true, 아무 것도 저장되지 않았으면 false
     */
    private boolean saveHistory(List<NewsEsDocument> newsList, List<SettingDTO> settings) {
        if (newsList == null || newsList.isEmpty()) {
            log.warn("해당 키워드로 검색된 뉴스가 없습니다.");
            throw new KakaoException(ErrorCode.NO_NEWS_DATA);
        }

        //중복 저장 방지용 플래그
        boolean saved = false;

        for (NewsEsDocument newsDoc : newsList) {
//            News newsitem = newsRepository.findById(Long.parseLong(newsDoc.getId()))
//                    .orElseThrow(() -> new RuntimeException("뉴스가 존재하지 않습니다: " + newsDoc.getId()));

            /* DB와 ES 동기화 되어있지 않을 시, 예외 */
            News newsitem = newsRepository.findById(Long.parseLong(newsDoc.getId()))
                    .orElse(null);
            if (newsitem == null) {
                log.warn("DB에 존재하지 않는 뉴스입니다. id={}", newsDoc.getId());
                continue; // 이 뉴스는 히스토리 저장 생략
            }

            for (SettingDTO settingDTO : settings) {
                Setting setting = settingRepository.findById(settingDTO.getId())
                        .orElseThrow(() -> new KakaoException(ErrorCode.SETTING_NOT_FOUND));

                // 중복 저장 방지용 코드
                boolean exists = historyRepository.existsBySettingAndNews(setting, newsitem);
                if (exists) {
                    log.info("이미 저장된 뉴스입니다. (settingId={}, newsId={})", setting.getId(), newsitem.getId());
                    continue;
                }

                History history = History.builder()
                        .publishedAt(LocalDateTime.now())
                        .setting(setting)
                        .news(newsitem)
                        .settingKeyword(String.join(",", settingDTO.getSettingKeywords()))
                        .blockKeyword(settingDTO.getBlockKeywords() != null ?
                                String.join(",", settingDTO.getBlockKeywords()) : null)
                        .build();

                historyRepository.save(history);
                saved = true;
            }
        }

        return saved;
    }

    // ======================= Deprecated =========================

//    /**
//     * 사용자의 키워드에 맞는 뉴스를 검색한 후, 카카오 메시지를 전송합니다.
//     *
//     * @param refreshAccessToken 사용자 카카오 Refresh Token
//     * @param userId             사용자 고유 ID
//     * @return 메시지 전송 성공 여부 (true: 성공, false: 실패)
//     */
//    public boolean sendKakaoMessage(String refreshAccessToken, Long userId) {
//        try {
//
//            /**
//             * 문제 정의 : 세팅 1번에 대해서만 메시지가 전송된다.
//             */
//
//            /* 유저에게 맞는 뉴스 리스트 검색*/
//            String accessToken = getKakaoUserAccessToken(refreshAccessToken, userId);
//            List<NewsEsDocument> newsList = getNewsEsDocumentList_Fixed(userId);
//            if (newsList == null) new KakaoException(ErrorCode.NO_NEWS_DATA);;
//
//            /* Http 요청 헤더 설정 */
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//            headers.set("Authorization", "Bearer " + accessToken);
//
//            /* 템플릿 설정(ES로 검색한 뉴스 리스트 넘겨받음) */
//            Map<String, String> templateArgs = createTemplateData(newsList);
//
//            /* JSON 문자열로 변환 */
//            ObjectMapper objectMapper = new ObjectMapper();
//            String templateArgsJson = objectMapper.writeValueAsString(templateArgs);
//
//            /* 요청 파라미터 구성 */
//            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
//            params.add("template_id", "122080");
//            params.add("template_args", templateArgsJson);
//
//            /* 카카오 메시지 전송 */
//            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
//            ResponseEntity<String> response = restTemplate.postForEntity(KAKAO_SEND_TOME_URL, entity, String.class);
//            log.info("카카오 메시지 전송 응답: {}", response.getBody());
//
//            return response.getStatusCode() == HttpStatus.OK;
//
//        } catch (Exception e) {
//            log.error("카카오 메시지 전송 실패: ", e);
//            throw new KakaoException(ErrorCode.MESSAGE_SEND_FAILED);
//        }
//    }

//    /**
//     * 사용자의 Setting 정보를 기반으로 키워드에 해당하는 뉴스를 검색하여 반환합니다.
//     * 뉴스는 히스토리에 저장되며, 최대 5개까지 템플릿으로 전송됩니다.
//     *
//     * @param userId 사용자 고유 ID
//     * @return 뉴스 리스트 {@code List<NewsEsDocument>}, 키워드가 없거나 오류 시 {@code null}
//     */
//    private List<NewsEsDocument> getNewsEsDocumentList(Long userId) {
//
//        //유저 정보를 기준으로 Settig값 가져오기
//        List<SettingDTO> settings = settingService.getAllSettingsByUserId(userId);
//
//        List<String> keywords = new ArrayList<>();
//        List<String> blockKeywords = new ArrayList<>();
//
//        for (SettingDTO setting : settings) {
//            log.info("셋팅값 확인용 코드 : " + setting.getSettingKeywords());
//            log.info("셋팅 제외 확인용 코드 : " + setting.getBlockKeywords());
//
//            // 키워드리스트의 null 값 체크
//            if (setting.getSettingKeywords() != null) {
//                keywords.add(setting.getSettingKeywords().toString());
//            }
//
//            blockKeywords.add(setting.getBlockKeywords().toString());
//        }
//
//        if (keywords.isEmpty()) {
//            log.error("설정된 키워드가 없습니다.");
//            throw new KakaoException(ErrorCode.SETTING_NOT_FOUND);
//        }
//
//        //키워드별 뉴스 검색
//        List<NewsEsDocument> newsList = newsService.searchNews(keywords, blockKeywords);
//
//        log.info("검색된 뉴스 수: {}", newsList.size());
//        newsList.forEach(n -> log.info("뉴스: {} - {}", n.getPublisher(), n.getSummary()));
//
//        // 검색된 뉴스를 히스토리로 보내는 코드
//        if (saveHistory(newsList, settings)) return null;
//        return newsList;
//    }

}