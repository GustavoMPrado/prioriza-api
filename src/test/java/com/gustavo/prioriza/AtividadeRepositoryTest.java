package com.gustavo.prioriza;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.gustavo.prioriza.entity.Atividade;
import com.gustavo.prioriza.repository.AtividadeRepository;

@DataJpaTest
class AtividadeRepositoryTest {

    @Autowired
    private AtividadeRepository atividadeRepository;

    @Test
    void salvarEBuscarPorId_deveFuncionar() {
        Atividade atividade = new Atividade();
        atividade.setTitulo("Repo test");
        atividade.setDescricao("ok");

        Atividade salva = atividadeRepository.save(atividade);

        var encontrada = atividadeRepository.findById(salva.getId());
        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().getTitulo()).isEqualTo("Repo test");
    }
}