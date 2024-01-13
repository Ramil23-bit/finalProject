package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.statistics.StatisticPageResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.StatisticsSiteResponse;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.StatisticSiteServiceImpl;
import searchengine.services.StatisticsService;

import javax.validation.Valid;
import java.io.IOException;

@RestController
@Configuration
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final StatisticsService statisticsService;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final StatisticSiteServiceImpl siteServiceImpl;
    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<StatisticsSiteResponse> startIndexing() throws IOException {
        return ResponseEntity.ok(siteServiceImpl.roundSites());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<StatisticsSiteResponse> stopIndexing(){
        return ResponseEntity.ok(siteServiceImpl.stopIndexing());
    }

    @PostMapping("/indexPage")
    public ResponseEntity<StatisticPageResponse> indexPage(String url) throws IOException {
        return ResponseEntity.ok(siteServiceImpl.addPageToIndex("https://radiomv.ru/"));
    }

}

