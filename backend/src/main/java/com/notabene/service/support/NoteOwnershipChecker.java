package com.notabene.service.support;

public interface NoteOwnershipChecker {
    boolean isOwnedByUser(Long noteId, Long userId);
}
