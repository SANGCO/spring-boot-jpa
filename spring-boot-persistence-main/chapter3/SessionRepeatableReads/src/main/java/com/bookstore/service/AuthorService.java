package com.bookstore.service;

import com.bookstore.entity.Author;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthorService {

    @Autowired
    private EntityManager entityManager;

    @Transactional
    public void testFlushBehavior() {
        // 엔티티를 로드하고 수정합니다.
        Author author = entityManager.find(Author.class, 1L);
        author.setName("New Name");

        // FlushModeType.AUTO 설정 (기본값)
        entityManager.setFlushMode(FlushModeType.AUTO);

        // JPQL 쿼리 실행 전 flush가 호출됩니다.
        TypedQuery<Author> jpqlQuery = entityManager.createQuery("SELECT a FROM Author a WHERE a.id = :id", Author.class);
        jpqlQuery.setParameter("id", 1L);
        Author authorViaJpql = jpqlQuery.getSingleResult();
        System.out.println("Author via JPQL (AUTO): " + authorViaJpql.getName());

        authorViaJpql.setName("Another Name");

        // TODO 변경 사항이 있어야 flush가 호출되는군.
        jpqlQuery = entityManager.createQuery("SELECT a FROM Author a WHERE a.id = :id", Author.class);
        jpqlQuery.setParameter("id", 1L);
        authorViaJpql = jpqlQuery.getSingleResult();
        System.out.println("Author via JPQL (AUTO): " + authorViaJpql.getName());


        // FlushModeType.COMMIT 설정
        entityManager.setFlushMode(FlushModeType.COMMIT);

        // JPQL 쿼리 실행 전 flush가 호출되지 않습니다.
        TypedQuery<Author> jpqlQueryCommit = entityManager.createQuery("SELECT a FROM Author a WHERE a.id = :id", Author.class);
        jpqlQueryCommit.setParameter("id", 1L);
        Author authorViaJpqlCommit = jpqlQueryCommit.getSingleResult();
        System.out.println("Author via JPQL (COMMIT): " + authorViaJpqlCommit.getName());
    }

}

