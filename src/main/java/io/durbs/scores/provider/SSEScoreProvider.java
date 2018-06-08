package io.durbs.scores.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.durbs.scores.domain.Score;
import io.durbs.scores.repo.ScoreRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Slf4j
@Component
public class SSEScoreProvider implements ScoreProvider {

    private static final String DATA_LINE_INDICATOR = "data: ";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ScoreRepository scoreRepository;

    @Value("${scores.provider.endpoint}")
    private String providerEndpoint;

    @Override
    @Async
    public void loadScores() {

        restTemplate.execute(providerEndpoint, HttpMethod.GET, request -> {}, response -> {

            val reader = new BufferedReader(new InputStreamReader(response.getBody()));

            String line;

            try {

                while ((line = reader.readLine()) != null) {

                    log.debug("received line '{}'", line);

                    if (line.startsWith(DATA_LINE_INDICATOR)) {

                        val jsonPayload = StringUtils.substringAfter(line, DATA_LINE_INDICATOR);
                        log.debug("received data '{}'", jsonPayload);

                        scoreRepository.saveScore(objectMapper.readValue(jsonPayload, Score.class));
                    }
                }

            } catch (Exception exception) {

                log.error("an error occurred ingesting data from feed", exception);
            }
            return response;
        });

        log.error("YOU SHOULD NEVER SEE THIS");
    }
}
