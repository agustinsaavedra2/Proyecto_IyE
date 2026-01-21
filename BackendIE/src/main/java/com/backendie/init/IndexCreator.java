package com.backendie.init;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IndexCreator {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void createIndexes() {
        try {
            // token_hash index
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token_hash ON refresh_tokens (token_hash);");
            // user_id index
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens (user_id);");
        } catch (Exception e) {
            // Log and continue; DB may not support IF NOT EXISTS syntax on index - ignore errors
            System.err.println("Warning: could not create refresh_tokens indexes: " + e.getMessage());
        }
    }
}

