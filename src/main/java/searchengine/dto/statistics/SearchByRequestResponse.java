package searchengine.dto.statistics;

import lombok.Data;

import java.util.List;

@Data
public class SearchByRequestResponse {
    private Boolean result;
    private Integer count;
    private List<String> data;

}
