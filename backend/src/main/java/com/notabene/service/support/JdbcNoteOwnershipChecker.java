package com.notabene.service.support;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class JdbcNoteOwnershipChecker implements NoteOwnershipChecker {

    private final JdbcTemplate jdbc;

    public JdbcNoteOwnershipChecker(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public boolean isOwnedByUser(Long noteId, Long userId) {
        Integer cnt = jdbc.queryForObject(
            "select count(1) from notes where id = ? and user_id = ?",
            Integer.class, noteId, userId
        );
        return cnt != null && cnt > 0;
    }
}
