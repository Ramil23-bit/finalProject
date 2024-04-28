package searchengine.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "lemma")
@Getter
@Setter
public class Lemma implements Comparable<Lemma> {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;
        @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
        @JoinColumn(name = "site_id", foreignKey = @ForeignKey(name = "FK_lemma_site"))
        @OnDelete(action = OnDeleteAction.CASCADE)
        private Site siteId;
        @Column(name = "lemma", columnDefinition = "VARCHAR(255)")
        private String lemma;
        @Column(name = "frequency")
        private Integer frequency;
        @OneToMany(mappedBy = "lemmaId", fetch = FetchType.LAZY)
        private List<Search> searchList;

        @Override
        public int compareTo(Lemma l) {
                return frequency.compareTo(l.getFrequency());
        }
}
