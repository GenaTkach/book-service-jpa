package telran.java2022.book.service;

import telran.java2022.book.dto.AuthorDto;
import telran.java2022.book.dto.BookDto;

public interface BookService {
    
    boolean addBook(BookDto bookDto);
    
    BookDto findBookByIsbn(String isbn);
    
    BookDto removeBook(String isbn);
    
    BookDto updateBook(String isbn, String title);
    
    Iterable<BookDto> findBooksByAuthor(String authorName);
    
    Iterable<BookDto> findBooksByPublisher(String publisherName);
    
    Iterable<AuthorDto> findAuthorsByBook(String isbn);
    
    Iterable<String> findPublishersByAuthor(String authorName);

    AuthorDto removeAuthor(String authorName);

}
