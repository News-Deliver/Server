package Baemin.News_Deliver.Global.News.ElasticSearch.service;

import Baemin.News_Deliver.Global.News.ElasticSearch.dto.NewsEsDocument;
import Baemin.News_Deliver.Global.News.JPAINSERT.dto.NewsItemDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsEsService {

    private final JdbcTemplate jdbcTemplate;

    public List<NewsItemDTO> loadNewsFromDB() {
        String sql = "SELECT * FROM news";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            return new NewsItemDTO(
                    String.valueOf(rs.getLong("id")), // ES에서 id는 문자열이 좋음
                    List.of(rs.getString("sections").split(",")), // 쉼표로 나눠서 List 변환
                    rs.getString("title"),
                    rs.getString("publisher"),
                    rs.getString("summary"),
                    rs.getString("content_url"),
                    rs.getTimestamp("published_at").toLocalDateTime()
            );
        });
    }

    public List<NewsEsDocument> convertToEsDocuments(List<NewsItemDTO> dtoList) {
        return dtoList.stream()
                .map(dto -> NewsEsDocument.builder()
                        .id(dto.getId())
                        .sections(dto.getSections().get(0))
                        .title(dto.getTitle())
                        .publisher(dto.getPublisher())
                        .summary(dto.getSummary())
                        .content_url(dto.getContent_url())
                        .published_at(dto.getPublished_at())
                        .build())
                .toList();
    }

}
