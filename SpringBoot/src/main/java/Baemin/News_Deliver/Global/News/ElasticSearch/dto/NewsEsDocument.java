package Baemin.News_Deliver.Global.News.ElasticSearch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Document(indexName = "news-index")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsEsDocument {

    @Id
    private String id; // ES에서는 문자열 ID로 처리하는 게 일반적 (BigInt도 string으로 보낼 것)

    @Field(type = FieldType.Keyword)
    private String sections; // 분석기 미적용 (정확한 검색용)

    @Field(type = FieldType.Text)
    private String title; // 검색 가능한 필드

    @Field(type = FieldType.Keyword)
    private String publisher; // 발행처 (정확 일치 검색 목적)

    @Field(type = FieldType.Text)
    private String summary; // 기사 요약 (Full Text 검색 대상)

    @Field(type = FieldType.Keyword)
    private String content_url; // URL은 분석기 필요 없음

    @Field(type = FieldType.Date)
    private LocalDateTime published_at; // 날짜 필드 (검색, 정렬, range 조건 가능)

}