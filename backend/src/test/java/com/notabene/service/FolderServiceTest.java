package com.notabene.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.web.server.ResponseStatusException;

import com.notabene.dto.FolderDtos.CreateFolderRequest;
import com.notabene.entity.Folder;
import com.notabene.model.FolderNoteId;
import com.notabene.repository.FolderNoteRepository;
import com.notabene.repository.FolderRepository;
import com.notabene.service.support.CurrentUserResolver;
import com.notabene.service.support.NoteOwnershipChecker;

class FolderServiceTest {

    FolderRepository folderRepo = mock(FolderRepository.class);
    FolderNoteRepository linkRepo = mock(FolderNoteRepository.class);
    CurrentUserResolver current = mock(CurrentUserResolver.class);
    NoteOwnershipChecker ownership = mock(NoteOwnershipChecker.class);

    FolderService service;

    @BeforeEach
    void setUp() {
        service = new FolderService(folderRepo, linkRepo, current, ownership);
        when(current.currentUserId()).thenReturn(42L);
    }

    @Test
    void createFolder_crea_cartella_di_utente_corrente() {
        var req = new CreateFolderRequest("Ideas");
        when(folderRepo.existsByOwnerIdAndName(42L, "Ideas")).thenReturn(false);
        var saved = new Folder(42L, "Ideas");
        saved.setId(10L);
        when(folderRepo.save(any())).thenReturn(saved);

        var res = service.createFolder(req);

        assertThat(res.id()).isEqualTo(10L);
        assertThat(res.name()).isEqualTo("Ideas");
        ArgumentCaptor<Folder> cap = ArgumentCaptor.forClass(Folder.class);
        verify(folderRepo).save(cap.capture());
        assertThat(cap.getValue().getOwnerId()).isEqualTo(42L);
    }

    @Test
    void createFolder_rifiuta_nome_vuoto() {
        assertThatThrownBy(() -> service.createFolder(new CreateFolderRequest("  ")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Name required");
    }

    @Test
    void createFolder_rifiuta_nome_duplicato_per_utente() {
        when(folderRepo.existsByOwnerIdAndName(42L, "Ideas")).thenReturn(true);
        assertThatThrownBy(() -> service.createFolder(new CreateFolderRequest("Ideas")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void addNote_rifiuta_se_nota_non_e_mia() {
        when(folderRepo.existsByIdAndOwnerId(99L, 42L)).thenReturn(true);
        when(ownership.isOwnedByUser(7L, 42L)).thenReturn(false);

        assertThatThrownBy(() -> service.addNote(99L, 7L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("only add your own note");
    }

    @Test
    void addNote_ok_se_folder_mio_e_nota_mia() {
        when(folderRepo.existsByIdAndOwnerId(3L, 42L)).thenReturn(true);
        when(ownership.isOwnedByUser(5L, 42L)).thenReturn(true);
        when(linkRepo.existsById(new FolderNoteId(3L, 5L))).thenReturn(false);

        service.addNote(3L,5L);

        verify(linkRepo).save(any());
    }
}

