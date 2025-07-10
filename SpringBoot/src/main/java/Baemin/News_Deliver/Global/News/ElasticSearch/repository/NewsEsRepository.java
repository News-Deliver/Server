package Baemin.News_Deliver.Global.News.ElasticSearch.repository;

import Baemin.News_Deliver.Global.News.ElasticSearch.dto.NewsEsDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface NewsEsRepository extends ElasticsearchRepository<NewsEsDocument, String> {
}
