package ojt.aws.educare.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/quotes")
public class QuoteController {

    @GetMapping("/today")
    public Object getTodayQuote() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-API-KEY", "qd_live_d9YDRNfezr7ut1qZWN6GhycjO1d6MRDv");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            Object quote = restTemplate.exchange("https://quoteday.dev/api/quote/today", HttpMethod.GET, entity, Object.class).getBody();
            
            if (quote instanceof List) {
                return quote;
            } else {
                return List.of(quote);
            }
        } catch (Exception e) {
            return List.of(Map.of(
                "q", "Every morning you have two choices: continue to sleep with your dreams, or wake up and chase them.",
                "a", "Carmelo Anthony"
            ));
        }
    }
}
