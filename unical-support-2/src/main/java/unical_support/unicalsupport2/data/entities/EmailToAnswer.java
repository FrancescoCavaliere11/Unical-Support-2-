package unical_support.unicalsupport2.data.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import unical_support.unicalsupport2.data.embeddables.SingleAnswer;

import java.util.List;

@Data
@Entity
@DiscriminatorValue("to_answer")
@EqualsAndHashCode(callSuper = true)
public class EmailToAnswer extends Email {
    @Column(name = "is_answered", nullable = false)
    private Boolean isAnswered;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "single_answer",
            joinColumns = @JoinColumn(name = "email_id")
    )
    private List<SingleAnswer> singleAnswers;
}
