package io.durbs.scores.provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ScoreProviderInvoker {

    @Autowired
    ScoreProvider scoreProvider;

    @PostConstruct
    void invoke() {

        scoreProvider.loadScores();
    }
}
