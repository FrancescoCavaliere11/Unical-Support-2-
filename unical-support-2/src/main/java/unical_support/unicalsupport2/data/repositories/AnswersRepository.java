package unical_support.unicalsupport2.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import unical_support.unicalsupport2.data.entities.Answers;

@Repository
public interface AnswersRepository extends JpaRepository<Answers, String> {
}
