package com.bookstore;

import com.bookstore.service.AuthorService;
import com.bookstore.service.BookstoreService;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MainApplication {
    private final BookstoreService bookstoreService;
    private final AuthorService authorService;

    public MainApplication(BookstoreService bookstoreService, AuthorService authorService) {
        this.bookstoreService = bookstoreService;
        this.authorService = authorService;
    }

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

    @Bean
    public ApplicationRunner init() {
        return args -> {
            //bookstoreService.directFetching();
            //bookstoreService.directFetching2();
//            bookstoreService.process();
            authorService.testFlushBehavior();
        };
    }
}