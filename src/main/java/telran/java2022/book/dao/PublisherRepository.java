package telran.java2022.book.dao;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import telran.java2022.book.model.Publisher;

public interface PublisherRepository extends CrudRepository<Publisher, String> {
    @Query("SELECT DISTINCT p.publisherName FROM Book b JOIN b.authors a JOIN b.publisher p WHERE a.name=?1")
    List<String> findPublishersByAuthor(String name);
    
    Stream<Publisher> findDistinctByBooksAuthorsName(String name);
}
