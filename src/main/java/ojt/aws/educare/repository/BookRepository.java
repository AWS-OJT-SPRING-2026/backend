package ojt.aws.educare.repository;

import ojt.aws.educare.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {
    @Query("""
            SELECT DISTINCT b
            FROM Book b
            LEFT JOIN FETCH b.chapters c
            LEFT JOIN FETCH c.lessons l
            LEFT JOIN FETCH l.sections s
            LEFT JOIN FETCH s.subsections ss
            LEFT JOIN FETCH ss.contentBlocks cb
            WHERE b.id = :bookId
            """)
    Optional<Book> findFullHierarchyById(@Param("bookId") Integer bookId);
}
