package telran.java2022.book.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import telran.java2022.book.dao.AuthorRepository;
import telran.java2022.book.dao.BookRepository;
import telran.java2022.book.dao.PublisherRepository;
import telran.java2022.book.dto.AuthorDto;
import telran.java2022.book.dto.BookDto;
import telran.java2022.book.dto.exceptions.EntityNotFoundException;
import telran.java2022.book.model.Author;
import telran.java2022.book.model.Book;
import telran.java2022.book.model.Publisher;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    // Все репозитории под каждый entity
    final BookRepository bookRepository;
    final AuthorRepository authorRepository;
    final PublisherRepository publisherRepository;

    // Модел маппер для создания разных объектов
    final ModelMapper mapper;

    @Override
    @Transactional
    public boolean addBook(BookDto bookDto) {
	if (bookRepository.existsById(bookDto.getIsbn())) {
	    return false;
	}

	// Проверяем есть ли такие авторы, если нет, то создаем и сохраняем их
	Set<Author> authors = bookDto.getAuthors()
		.stream()
		.map(a -> authorRepository.findById(a.getName())
			.orElse(authorRepository.save(new Author(a.getName(), a.getBirthDate()))))
		.collect(Collectors.toSet());

	// Проверяем есть ли такой паблишер, если нет, то создаем и сохраняем новый
	Publisher publisher = publisherRepository.findById(bookDto.getPublisher())
		.orElse(publisherRepository.save(new Publisher(bookDto.getPublisher())));

	// Создаем и сохраняем книгу в bookRepository
	Book book = new Book(bookDto.getIsbn(), bookDto.getTitle(), authors, publisher);
	bookRepository.save(book);
	return true;
    }

    @Override
    public BookDto findBookByIsbn(String isbn) {
	Book book = bookRepository.findById(isbn)
		.orElseThrow(() -> new EntityNotFoundException());
	BookDto bookDto = mapper.map(book, BookDto.class);
	bookDto.setPublisher(book.getPublisher()
		.getPublisherName());
	return bookDto;
    }

    @Override
    @Transactional
    public BookDto removeBook(String isbn) {
	Book book = bookRepository.findById(isbn)
		.orElseThrow(() -> new EntityNotFoundException());
	BookDto bookDto = mapper.map(book, BookDto.class);
	bookDto.setPublisher(book.getPublisher()
		.getPublisherName());
	bookRepository.deleteById(isbn);
	return bookDto;
    }

    @Override
    public BookDto updateBook(String isbn, String title) {
	Book book = bookRepository.findById(isbn)
		.orElseThrow(() -> new EntityNotFoundException());
	book.setTitle(title);
	BookDto bookDto = mapper.map(book, BookDto.class);
	bookDto.setPublisher(book.getPublisher()
		.getPublisherName());
	return bookDto;
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<BookDto> findBooksByAuthor(String authorName) {
	List<Book> books = bookRepository.findBooksByAuthorsName(authorName)
		.collect(Collectors.toList());
	List<BookDto> booksDto = bookRepository.findBooksByAuthorsName(authorName)
		.map(b -> mapper.map(b, BookDto.class))
		.collect(Collectors.toList());

	// Добавление к каждому BookDto название publisher.
	return addToDtosPublisherName(books, booksDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<BookDto> findBooksByPublisher(String publisherName) {
	List<Book> books = bookRepository.findBooksByPublisherPublisherName(publisherName)
		.collect(Collectors.toList());
	List<BookDto> booksDto = bookRepository.findBooksByPublisherPublisherName(publisherName)
		.map(b -> mapper.map(b, BookDto.class))
		.collect(Collectors.toList());

	// Добавление к каждому BookDto название publisher.
	return addToDtosPublisherName(books, booksDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<AuthorDto> findAuthorsByBook(String isbn) {
	Book book = bookRepository.findById(isbn)
		.orElseThrow(() -> new EntityNotFoundException());
	Set<Author> authors = book.getAuthors();
	return authors.stream()
		.map(a -> mapper.map(a, AuthorDto.class))
		.collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<String> findPublishersByAuthor(String authorName) {
	return bookRepository.findBooksByAuthorsName(authorName)
		.map(b -> b.getPublisher()
			.getPublisherName())
		.collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public AuthorDto removeAuthor(String name) {
	// Проверка что такой автор существует
	Author author = authorRepository.findById(name)
		.orElseThrow(() -> new EntityNotFoundException());

	// Удалить все книги связанные с этим автором
	bookRepository.deleteAll(bookRepository.findBooksByAuthorsName(name)
		.collect(Collectors.toList()));
	// Если publisher выпускал только книги удаленного автора, то удалить publisher
	List<Book> books = (List<Book>) bookRepository.findAll();
	List<String> publishers = books.stream()
		.distinct()
		.map(b -> b.getPublisher()
			.getPublisherName())
		.collect(Collectors.toList());
	for (int i = 0; i < publishers.size(); i++) {
	    if (bookRepository.findBooksByPublisherPublisherName(publishers.get(i)) == null) {
		publisherRepository.deleteById(publishers.get(i));
	    }
	}
	// Удаление автора
	authorRepository.delete(author);
	return mapper.map(author, AuthorDto.class);
    }

    private Iterable<BookDto> addToDtosPublisherName(List<Book> books, List<BookDto> booksDto) {
	for (int i = 0; i < books.size(); i++) {
	    booksDto.get(i)
		    .setPublisher(books.get(i)
			    .getPublisher()
			    .getPublisherName());
	}
	return booksDto;
    }
}
