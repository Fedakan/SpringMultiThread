package org.example.springmultithread.repository;

import org.example.springmultithread.entity.PokemonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PokemonRepository extends JpaRepository<PokemonEntity, Integer> {

    List<PokemonEntity> findAll();

    List<PokemonEntity> findByType(String type);
}
