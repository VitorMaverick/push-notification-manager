package br.edu.acad.ifma.adapters.auth.repository;

import br.edu.acad.ifma.adapters.auth.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {}
