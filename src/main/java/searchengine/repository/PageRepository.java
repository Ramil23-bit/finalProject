package searchengine.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;


@Repository
public interface PageRepository extends CrudRepository<Page, Integer> {
    Long deleteByPath(String url);
}
