package searchengine.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "site")
@Getter
@Setter
public class Site {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SiteIndexStatusType status;
    @Column(name = "status_time")
    @UpdateTimestamp
    private Instant status_time;
    @Column(name = "last_error", columnDefinition = "TEXT")
    private String last_error;
    @Column(name = "url", columnDefinition = "VARCHAR(255)" )
    private String url;
    @Column(name = "name_site", columnDefinition = "VARCHAR(255)" )
    private String nameSite;
    @OneToMany(mappedBy = "siteId", fetch = FetchType.LAZY)
    private List<Page> pageList = new ArrayList<>();
    @OneToMany(mappedBy = "siteId", fetch = FetchType.LAZY)
    private List<Lemma> lemmaList = new ArrayList<>();

}
