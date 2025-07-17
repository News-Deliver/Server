package Baemin.News_Deliver.Global.News.ElasticSearch.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

/**
 * Elasticsearch 색인용 뉴스 문서 (DTO 겸용)
 *
 * <p>해당 객체는 뉴스 데이터를 Elasticsearch에 색인할 때 사용되며,
 * 검색 및 분석을 위한 필드 설정과 Swagger 문서화를 모두 포함합니다.</p>
 *
 * 사용 용도:
 * <ul>
 *   <li>Elasticsearch 저장 및 검색</li>
 *   <li>API 문서화 시 DTO로 사용 (Swagger)</li>
 * </ul>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Elasticsearch 뉴스 색인 도큐먼트")
public class NewsEsDocument {

    @Id
    @Schema(description = "Elasticsearch 문서 ID", example = "b123f1d0-8456-4a20-8e31-a1eaf6c82971")
    private String id;

    @Field(type = FieldType.Keyword)
    @Schema(description = "뉴스 섹션", example = "politics")
    private String sections;

    @Field(type = FieldType.Text, analyzer = "korean_nori")
    @Schema(description = "뉴스 제목", example = "윤석열 대통령, 미국 방문 일정 발표")
    private String title;

    @Field(type = FieldType.Keyword)
    @Schema(description = "언론사", example = "연합뉴스")
    private String publisher;

    @Field(type = FieldType.Text, analyzer = "korean_nori")
    @Schema(description = "뉴스 요약", example = "윤석열 대통령이 미국 순방 일정을 발표하며 경제 협력에 초점을 맞췄다.")
    private String summary;

    @Field(type = FieldType.Keyword)
    @Schema(description = "뉴스 원문 URL", example = "https://news.example.com/articles/abc123")
    private String content_url;

    @Field(
            type = FieldType.Date,
            format = DateFormat.date_time,
            pattern = "yyyy-MM-dd'T'HH:mm:ss"
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "발행 시각", example = "2025-07-15T09:00:00")
    private LocalDateTime published_at;

    @Field(type = FieldType.Text, analyzer = "korean_nori")
    @Schema(description = "색인용 통합 토큰 (제목 + 요약 병합 결과)", example = "윤석열 미국 방문 경제 협력")
    private String combinedTokens;
}