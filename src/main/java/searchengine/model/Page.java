package searchengine.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "page")
@Getter
@Setter
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", foreignKey = @ForeignKey(name = "FK_page_site"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Site siteId;
    @Column(name = "path",columnDefinition = "VARCHAR(512)")
    private String path;
    @Column(name = "code", columnDefinition = "integer")
    private Integer code;
    @Column(name = "content", columnDefinition = "MEDIUMTEXT")
    private String content;
    @OneToMany(mappedBy = "pageId", fetch = FetchType.LAZY)
    private List<Search> searchList = new ArrayList<>();
}
