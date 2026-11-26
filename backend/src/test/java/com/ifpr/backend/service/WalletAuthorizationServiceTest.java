package com.ifpr.backend.service;

import com.ifpr.backend.model.CarteiraMembro;
import com.ifpr.backend.model.PapelCarteira;
import com.ifpr.backend.model.Usuario;
import com.ifpr.backend.repository.CarteiraMembroRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletAuthorizationServiceTest {
    private final CarteiraMembroRepository repository = mock(CarteiraMembroRepository.class);
    private final CurrentUserService current = mock(CurrentUserService.class);
    private final WalletAuthorizationService auth = new WalletAuthorizationService(repository, current);
    @BeforeEach void user() {
        var user = new Usuario(); user.setId(7L);
        when(current.get()).thenReturn(user);
    }
    @Test void rejectsNonMember() {
        when(repository.findByCarteiraIdAndUsuarioId(2L, 7L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> auth.requireMember(2L)).isInstanceOf(AccessDeniedException.class);
    }
    @Test void rejectsPendingInvitation() {
        member(PapelCarteira.EDITOR, true);
        assertThatThrownBy(() -> auth.requireMember(2L)).isInstanceOf(AccessDeniedException.class);
    }
    @Test void viewerCannotEdit() {
        member(PapelCarteira.VIEWER, false);
        assertThatThrownBy(() -> auth.requireEditor(2L)).isInstanceOf(AccessDeniedException.class);
    }
    @Test void editorCannotManageOwners() {
        member(PapelCarteira.EDITOR, false);
        assertThat(auth.requireEditor(2L).getPapel()).isEqualTo(PapelCarteira.EDITOR);
        assertThatThrownBy(() -> auth.requireOwner(2L)).isInstanceOf(AccessDeniedException.class);
    }
    private void member(PapelCarteira role, boolean pending) {
        var member = new CarteiraMembro(); member.setPapel(role); member.setConvitePendente(pending);
        when(repository.findByCarteiraIdAndUsuarioId(2L, 7L)).thenReturn(Optional.of(member));
    }
}
