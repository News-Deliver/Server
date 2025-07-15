package Baemin.News_Deliver.Domain.SubServices.MoreNews.Service;

import Baemin.News_Deliver.Domain.Kakao.entity.History;
import Baemin.News_Deliver.Domain.Kakao.repository.HistoryRepository;
import Baemin.News_Deliver.Domain.SubServices.Exception.SubServicesException;
import Baemin.News_Deliver.Domain.SubServices.MoreNews.DTO.MoreNewsResponse;
import Baemin.News_Deliver.Global.Exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MoreNewsService {

    private final HistoryRepository historyRepository;

    /* 뉴스 추가 검색 메서드 */
    public List<MoreNewsResponse> getMoreNews(Long historyId) {

        /* 히스토리 객체 반환 */
        History history = historyRepository.findById(historyId)
                .orElseThrow(() -> new SubServicesException(ErrorCode.HISTORY_NOT_FOUND));

        /* 히스토리 세부 정보 반환 */
        String settingKeyword = history.getSettingKeyword(); // 설정 키워드 반환
        String blockKeyword = history.getBlockKeyword(); // 제외 키워드
        LocalDateTime publishedAt = history.getPublishedAt(); // 날짜



        return null;
    }


}
