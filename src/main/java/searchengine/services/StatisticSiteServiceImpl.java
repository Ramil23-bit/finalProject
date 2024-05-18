package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import searchengine.config.Sites;
import searchengine.dto.statistics.SearchByRequestResponse;
import searchengine.dto.statistics.StatisticPageResponse;
import searchengine.dto.statistics.StatisticsSiteResponse;
import searchengine.exception.StatisticSiteException;
import searchengine.model.*;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SearchRepository;
import searchengine.repository.SiteRepository;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class StatisticSiteServiceImpl {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final Sites sitesList;
    private final SearchRepository searchRepository;
    private final LemmaFinderService lemmaFinderService;
    private final LemmaRepository lemmaRepository;
    private ExecutorService executorService;

    @PostConstruct
    private void postConstruct() {
        executorService = Executors.newCachedThreadPool();
    }


    public void createEntry() {
        Site site = new Site();
        site.setStatus(SiteIndexStatusType.INDEXING);
        siteRepository.save(site);
    }

    public void createDataInTablePage(String url) {

        try {
            controlSite(url);
        } catch (IllegalArgumentException | IOException e) {
            throw new IllegalArgumentException("Data entered incorrectly");
        }
    }

    @Transactional
    public void deleteSite(String url) {

        if (!url.isEmpty()) {
            throw new IllegalArgumentException("Can't delete file");
        }
        Long deleteDataSite = siteRepository.deleteByUrl(url);
        Long deleteDataPage = pageRepository.deleteByPath(url);
    }

    public StatisticsSiteResponse roundSites() {
        StatisticsSiteResponse siteResponse = new StatisticsSiteResponse();

        if (!executorService.isShutdown()) {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            sitesList.getSites().forEach(site -> {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> roundSite(site.getUrl())
                        , executorService);
                futures.add(future);
            });
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            siteResponse.setResult(true);
        } else {
            siteResponse.setError("result: Индексация уже запущена");
            siteResponse.setResult(false);
        }
        return siteResponse;
    }

    private boolean roundSite(String url) {
        Site site = new Site();
        try {
            controlSite(url);
            throw new StatisticSiteException("HTML code not found");
        } catch (Exception e) {
            site.setStatus(SiteIndexStatusType.INDEXING);
        }
        return true;
    }

    public StatisticPageResponse urlRegex(String url) throws IOException {
        Search search = new Search();
        Page page = new Page();
        Lemma lemma = new Lemma();

        StatisticPageResponse pageResponse = new StatisticPageResponse();

        String regex = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

        if (!siteCheck(url, regex)) {
            pageResponse.setResult(false);
            pageResponse.setUrl("Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
        }
        roundSite(url);
        search.setPageId(page);
        search.setLemmaId(lemma);
        search.setPercentLemma(4.02f);
        searchRepository.save(search);
        return pageResponse;
    }

    public SearchByRequestResponse searchForPagesByRequest(String query, String site) throws IOException {
        SearchByRequestResponse requestResponse = new SearchByRequestResponse();
        List<Lemma> searchLemma = lemmaFinderService.collectLemmas(site);
        Lemma lemma = new Lemma();
        int count = 0;
        for (int i = 0; i <= searchLemma.size(); i++) {
            if (lemma.getFrequency() >= 5) {
                lemmaRepository.delete(lemma);
            }
            if (lemma.getLemma().contains(query)) {
                count++;
                requestResponse.setCount(count);
            }
        }
        return requestResponse;
    }

    @Transactional
    public void controlSite(String url) throws IOException {
        Site site = new Site();
        Page page = new Page();

        Document document = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows; Windows NT 6.3; x64) AppleWebKit/537.1 (KHTML, like Gecko)" +
                        "Chrome/47.0.1083.353 Safari/535")
                .referrer("https://www.google.com")
                .get();
        String title = document.title();
        Element elementSite = document.select("a").first();
        String attribute = Objects.requireNonNull(elementSite).absUrl("href");

        URL urlCode = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) urlCode.openConnection();
        int responseCode = connection.getResponseCode();

        String html = document.html();

        if (!attribute.startsWith(url)) {
            throw new StatisticSiteException("Incorrect data entered");
        }
        page.setContent(String.valueOf(attribute.replaceFirst("https://", "").split("/").length));

        site.setStatus_time(Instant.now());
        site.setUrl(attribute);
        site.setNameSite(title);
        site.setStatus(SiteIndexStatusType.INDEXED);
        siteRepository.save(site);

        page.setPath(attribute);
        page.setCode(responseCode);
        page.setContent(html);
        page.setSiteId(site);
        pageRepository.save(page);

        lemmaFinderService.collectLemmas(html);
    }

    public StatisticsSiteResponse stopIndexing() {
        StatisticsSiteResponse siteResponse = new StatisticsSiteResponse();
        if (!roundSites().isResult()) {
            siteResponse.setResult(true);
        }
        siteResponse.setError("Индксация уже запущена");
        return siteResponse;
    }

    public static boolean siteCheck(String url, String pattern1) {
        try {
            Pattern patt = Pattern.compile(pattern1);
            Matcher matcher = patt.matcher(url);
            return matcher.matches();
        } catch (RuntimeException e) {
            return false;
        }
    }
}
