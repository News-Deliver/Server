package Baemin.News_Deliver.Global.News.ElasticSearch.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsEsDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String sections;

    @Field(type = FieldType.Text, analyzer = "korean_nori")
    private String title;

    @Field(type = FieldType.Keyword)
    private String publisher;

    @Field(type = FieldType.Text, analyzer = "korean_nori")
    private String summary;

    @Field(type = FieldType.Keyword)
    private String content_url;

    @Field(
            type = FieldType.Date,
            format = DateFormat.date_time,
            pattern = "yyyy-MM-dd'T'HH:mm:ss"
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime published_at;
}
