package searchengine.services;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.exception.StatisticSiteException;
import searchengine.model.EnumForTable;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import javax.transaction.Transactional;
import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class StatisticSiteServiceImpl  {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;

    public void createEntry() {
        Site site = new Site();
        site.setStatus(EnumForTable.INDEXING);
        siteRepository.save(site);
    }

    public void createDataInTablePage(String url) {

        try {
            roundSite(url);
        }catch (IllegalArgumentException e){
            throw new IllegalArgumentException("Data entered incorrectly");
        }
    }

    @Transactional
    public void deleteSite(String url) {

        if (!url.isEmpty()) {
            throw new IllegalArgumentException();
        }
        Optional<Site> deleteDataSite = siteRepository.deleteDataBySite(url);
        Optional<Page> deleteDataPage = pageRepository.deleteDataByPage(url);
    }

    private boolean roundSite(String url) {
        Document doc = null;

        try {
            doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 6.2;) AppleWebKit/602.40 " +
                            "(KHTML, like Gecko) Chrome/52.0.1538.324 Safari/602")
                    .referrer("http://www.google.com")
                    .get();
        } catch (Exception e) {
            throw new StatisticSiteException(e.getMessage());
        }

        String title = doc.title();
        Site site = new Site();
        Page page = new Page();

        try {
            controlSite(url);
            if (!page.getContent().isEmpty()) {
                siteRepository.save(site);
                pageRepository.save(page);
            }
            throw new StatisticSiteException("HTML code not found");
        } catch (Exception e) {
            site.setStatus(EnumForTable.INDEXING);
        }
        return true;
    }

    private void controlSite(String url){
        Element elementSite = new Element(url);
        Page page = new Page();
        Site site = new Site();
        String attribute = elementSite.attr("href");
        System.out.printf("Execute task on thread %s%n", Thread.currentThread());

        if(!attribute.startsWith(url)){
            throw new StatisticSiteException("Incorrect data entered");
        }
        page.setContent(String.valueOf(attribute.replaceFirst("https://", "").split("/").length));
        site.setStatus_time(Instant.now());
        page.setPath(attribute);
    }

    public void roundSites() throws ExecutionException, InterruptedException {
        List<Site> urlsSite = new ArrayList<>();
        SitesList sitesList = new SitesList();
        urlsSite.add((Site) sitesList.getSites());

        CompletableFuture<String> future = new CompletableFuture<>();
        for(int i = 0; i <= urlsSite.size(); i++){
            future = CompletableFuture.supplyAsync(urlsSite::toString);
        }
        future.get();
    }

}

/*
Document doc = Jsoup.connect("http://example.com")
  .data("query", "Java")
  .userAgent("Mozilla")
  .cookie("auth", "token")
  .timeout(3000)
  .post();

 */