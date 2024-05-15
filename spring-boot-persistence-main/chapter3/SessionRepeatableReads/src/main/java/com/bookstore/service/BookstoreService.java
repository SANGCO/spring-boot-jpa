package com.bookstore.service;

import com.bookstore.dao.Dao;
import com.bookstore.entity.Author;
import com.bookstore.repository.AuthorRepository;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import java.util.Optional;

@Service
public class BookstoreService {
    private final AuthorRepository authorRepository;
    private final TransactionTemplate template;
    private final Dao dao;
    private final EntityManager entityManager;

    public BookstoreService(AuthorRepository authorRepository, TransactionTemplate template, Dao dao, EntityManager entityManager) {
        this.authorRepository = authorRepository;
        this.template = template;
        this.dao = dao;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly=true)
    public void directFetching() {
        // direct fetching via Spring Data
        Optional<Author> resultSD = authorRepository.findById(1L);
        System.out.println("Direct fetching via Spring Data result: " + resultSD.get());

        // direct fetching via EntityManager
        Optional<Author> resultEM = dao.find(Author.class, 1L);
        System.out.println("Direct fetching via EntityManager result: " + resultEM.get());

        // direct fetching via Session
        Optional<Author> resultHS = dao.findViaSession(Author.class, 1L);
        System.out.println("Direct fetching via Session result: " + resultHS.get());
    }

    @Transactional(readOnly=true)
    public void directFetching2() {
        // direct fetching via Spring Data
        Optional<Author> resultSD = authorRepository.findById(1L);
        System.out.println("Direct fetching via Spring Data: " + resultSD.get());

        // direct fetching via EntityManager
        Optional<Author> resultJPQL = authorRepository.fetchById(1L);
        System.out.println("Explicit JPQL: " + resultJPQL.get());
    }

    public void process() {
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        // We use MySQL 8 InnoDB.
        // The default isolation level for InnoDB is REPEATABLE READ, therefore
        // we need to switch to READ_COMMITTED to reveal how Hibernate session-level
        // repeatable reads works
        template.setIsolationLevel(Isolation.READ_COMMITTED.value());

        // Transaction A
        template.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                Author authorA1 = authorRepository.findById(1L).orElseThrow();
                System.out.println("Author A1: " + authorA1.getName() + "\n");
                // TODO   Author A1: Mark Janel

                // Transaction B
                template.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {

                        Author authorB = authorRepository.findById(1L).orElseThrow();
                        authorB.setName("Alicia Tom");
                        System.out.println("Author B: " + authorB.getName() + "\n");
                        // TODO   Author B: Alicia Tom
                    }
                });


                // Print flush mode
                printFlushMode();

                // JPQL query projections always load the latest database state
                String nameViaJpql = authorRepository.fetchNameByIdJpql(1L);
                System.out.println("Author name via JPQL: " + nameViaJpql + "\n");
                // TODO   Author name via JPQL: Alicia Tom

                // SQL query projections always load the latest database state
                String nameViaSql = authorRepository.fetchNameByIdSql(1L);
                System.out.println("Author name via SQL: " + nameViaSql + "\n");
                // TODO   Author name via SQL: Alicia Tom

                // TODO 변경사항이 감지되는게 없기 때문에 JPQL 쿼리로 플러시 자동 호출되지 않는다.
                Author authorViaJpql = authorRepository.fetchByIdJpql(1L);
                System.out.println("Author via JPQL: " + authorViaJpql.getName() + "\n");
                // TODO   Author via JPQL: Mark Janel

                // Direct fetching via findById(), find() and get() doesn't trigger a SELECT
                // It loads the author directly from Persistence Context
                Author authorA2 = authorRepository.findById(1L).orElseThrow();
                System.out.println("\nAuthor A2: " + authorA2.getName() + ", " + "\n");
                // TODO   Author A2: Mark Janel

                // SQL entity queries take advantage of session-level repeatable reads
                // The data snapshot returned by the triggered SELECT is ignored
                Author authorViaSql = authorRepository.fetchByIdSql(1L);
                System.out.println("Author via SQL: " + authorViaSql.getName() + ", " + "\n");
                // TODO   Author via SQL: Mark Janel

                // JPQL entity queries take advantage of session-level repeatable reads
                // The data snapshot returned by the triggered SELECT is ignored
                // TODO 수정이 감지되면 JPQL 쿼리가 플러시 자동 호출하는지 테스트
                authorA1.setAge(35);
                // .setName("SANGCO") 안했으면 .setAge(35); 변경 하면서 name은 다시 Mark Janel로 변경되었다.
                authorA1.setName("SANGCO");
                authorViaJpql = authorRepository.fetchByIdJpql(1L);
                System.out.println("Author via JPQL: " + authorViaJpql.getName() + ", " + authorViaJpql.getAge() + "\n");
                // TODO   Author via JPQL: SANGCO, 35

                // JPQL query projections always load the latest database state
                nameViaJpql = authorRepository.fetchNameByIdJpql(1L);
                System.out.println("Author name via JPQL: " + nameViaJpql + "\n");
                // TODO   Author name via JPQL: SANGCO

                // SQL query projections always load the latest database state
                nameViaSql = authorRepository.fetchNameByIdSql(1L);
                System.out.println("Author name via SQL: " + nameViaSql + "\n");
                // TODO   Author name via SQL: SANGCO
            }
        });
    }

    private void printFlushMode() {
        FlushModeType flushMode = entityManager.getFlushMode();
        FlushModeType hibernateFlushMode = entityManager.unwrap(Session.class).getFlushMode();
        System.out.println("Flush mode, Hibernate JPA (EntityManager#getFlushMode()): " + flushMode);
        System.out.println("Flush mode, Hibernate JPA (Session#getFlushMode()): " + hibernateFlushMode);
    }

}


