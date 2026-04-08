package com.gustavo.prioriza.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gustavo.prioriza.entity.Atividade;
import com.gustavo.prioriza.entity.PrioridadeAtividade;
import com.gustavo.prioriza.entity.StatusAtividade;

public interface AtividadeRepository extends JpaRepository<Atividade, Long> {

    @Query("""
        select a from Atividade a
        where
          (:status is null or a.status = :status)
          and (:prioridade is null or a.prioridade = :prioridade)
          and (
            lower(a.titulo) like :q
            or lower(coalesce(a.descricao, '')) like :q
          )
    """)
    Page<Atividade> buscar(
            @Param("q") String q,
            @Param("status") StatusAtividade status,
            @Param("prioridade") PrioridadeAtividade prioridade,
            Pageable pageable
    );

    @Query("""
        select a from Atividade a
        where
          (:status is null or a.status = :status)
          and (:prioridade is null or a.prioridade = :prioridade)
    """)
    Page<Atividade> filtrarApenas(
            @Param("status") StatusAtividade status,
            @Param("prioridade") PrioridadeAtividade prioridade,
            Pageable pageable
    );
}
