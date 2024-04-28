package searchengine.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Entity
@Table(name = "search")
@Getter
@Setter
public class Search {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "page_id",  foreignKey = @ForeignKey(name = "FK_search_page"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Page pageId;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "lemma_id", foreignKey = @ForeignKey(name = "FK_search_lemma"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Lemma lemmaId;
    @Column(name = "percent_lemma")
    private float percentLemma;
}
