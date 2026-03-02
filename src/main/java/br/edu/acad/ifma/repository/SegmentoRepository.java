package br.edu.acad.ifma.repository;

import br.edu.acad.ifma.domain.Segmento;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SegmentoRepository extends JpaRepository<Segmento, Long> {
    Optional<Segmento> findByNome(String nome);
}
