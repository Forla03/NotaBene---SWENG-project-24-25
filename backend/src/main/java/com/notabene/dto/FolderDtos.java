package com.notabene.dto;

import java.util.List;

public class FolderDtos {
  public record FolderSummary(Long id, String name) {}
  public record CreateFolderRequest(String name) {}
  public record FolderNoteRef(Long id) {}
  public record FolderDetail(Long id, String name, List<FolderNoteRef> notes) {}
}

