package unical_support.unicalsupport2.data.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;
import unical_support.unicalsupport2.data.embeddables.SingleAnswer;

import java.util.List;

@Data
@Entity
@Table(name = "answers")
public class Answers {
    @Id
    @UuidGenerator
    private String id;

    @Column(name = "is_answered", nullable = false)
    private Boolean isAnswered;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "single_answer",
            joinColumns = @JoinColumn(name = "email_id")
    )
    private List<SingleAnswer> singleAnswers;

    @OneToOne(mappedBy = "answers", cascade = CascadeType.ALL, optional = false)
    private Email email;
}
