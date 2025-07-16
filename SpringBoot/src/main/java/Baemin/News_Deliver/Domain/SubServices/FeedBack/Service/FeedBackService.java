package Baemin.News_Deliver.Domain.SubServices.FeedBack.Service;

import Baemin.News_Deliver.Domain.Kakao.entity.History;
import Baemin.News_Deliver.Domain.Kakao.repository.HistoryRepository;
import Baemin.News_Deliver.Domain.SubServices.Exception.SubServicesException;
import Baemin.News_Deliver.Domain.SubServices.FeedBack.DTO.FeedbackRequest;
import Baemin.News_Deliver.Domain.SubServices.FeedBack.Entity.Feedback;
import Baemin.News_Deliver.Domain.SubServices.FeedBack.Repository.FeedbackRepository;
import Baemin.News_Deliver.Global.Exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedBackService {

    private final FeedbackRepository feedbackRepository;
    private final HistoryRepository historyRepository;

    /**
     * 키워드 반영도 피드백 메서드
     *
     * @param request 피드백 요청 DTO
     * @return 피드백 결과 반환
     */
    public Long keywordFeedBack(FeedbackRequest request){

        // 임시 하드코딩
        // Long userId = 1L;

        /* 피드백 객체 반환 */
        Optional<Feedback> optionalFeedback = feedbackRepository.findById(request.getHistoryId());

        /* 히스토리 객체 반환 */
        History history = historyRepository.findById(request.getHistoryId())
                .orElseThrow(() -> new SubServicesException(ErrorCode.HISTORY_NOT_FOUND));

        if (optionalFeedback.isPresent()) {

            /* 키워드 반영도에 따른 Actoin 분류*/
            Feedback feedback = optionalFeedback.get();
            Long currentValue = feedback.getKeywordReflection(); // 현재 저장된 값
            Long requestValue = request.getFeedbackValue(); // 유저가 누른 값 (1 또는 -1)

            if (currentValue == null)
                currentValue = 0L;
            if (currentValue.equals(requestValue))
                feedback.setKeywordReflection(0L); // 같은 값이면 취소 → 0
             else
                feedback.setKeywordReflection(requestValue); // 반대 값이거나 기존이 0 → 새 값 반영

            feedbackRepository.save(feedback); // 저장

            return feedback.getKeywordReflection();
        } else {

            /* null일때, 눌렀으면 입력된 값으로 수정 */
            Feedback feedback = Feedback.builder()
                    .history(history)
                    .keywordReflection(request.getFeedbackValue())
                    .contentQuality(null)
                    .build();
            feedbackRepository.save(feedback);
            return feedback.getKeywordReflection();
        }

    }
}
