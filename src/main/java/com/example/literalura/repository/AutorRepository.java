package com.example.literalura.repository;

import com.example.literalura.model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AutorRepository extends JpaRepository<Autor, Long> {

    // Buscar autor por nombre
    Optional<Autor> findByNombre(String nombre);

    // Buscar autores vivos en un año específico
    @Query("SELECT a FROM Autor a " +
            "WHERE (a.anioNacimiento IS NULL OR a.anioNacimiento <= :anio) " +
            "AND (a.anioFallecimiento IS NULL OR a.anioFallecimiento >= :anio)")
    List<Autor> findAutoresVivosEnAnio(Integer anio);

    List<Autor> findByNombreContainingIgnoreCase(String nombre);



}
