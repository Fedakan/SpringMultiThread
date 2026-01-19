package org.example.springmultithread.service;

import lombok.RequiredArgsConstructor;
import org.example.springmultithread.entity.PokemonEntity;
import org.example.springmultithread.exceptions.GlobalExceprionHandler;
import org.example.springmultithread.exceptions.PokemonNotFoundException;
import org.example.springmultithread.repository.PokemonRepository;

import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PokemonService {

    private static final Logger log = LoggerFactory.getLogger(PokemonService.class);
    private final PokemonRepository pokemonRepository;

    public PokemonEntity addPokemon(PokemonEntity pokemon) {
        log.info(">>>>Saving pokemon: {}", pokemon.getName());
        pokemon.setId(null);
        return pokemonRepository.save(pokemon);
    }

    @Cacheable(value = "pokemonCache", key = "#pokemonId")
    public PokemonEntity getPokemonDetails(int pokemonId) {
        log.info(">>>>>Loading pokemon details for Id: {}. It must occur once!", pokemonId);

        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return pokemonRepository.findById(pokemonId)
                .orElseThrow(() -> new PokemonNotFoundException(pokemonId));
    }

    @CacheEvict(value = "pokemonCache", key = "#pokemonId")
    public PokemonEntity updateLevel(int pokemonId, int newLevel) {
        log.info(">>>>> Updating level for ID: {}. Cache will be cleaned!", pokemonId);
        PokemonEntity pokemon = pokemonRepository.findById(pokemonId)
                .orElseThrow(() -> new PokemonNotFoundException(pokemonId));
        pokemon.setLevel(newLevel);
        return pokemonRepository.save(pokemon);
    }

    @CacheEvict(value = "pokemonCache", key = "#pokemonId")
    public void deletePokemon(int pokemonId) {
        log.info(">>>>> Deleting pokemon: {}. Cache cleaned.", pokemonId);
        if (!pokemonRepository.existsById(pokemonId)) {
            throw new PokemonNotFoundException(pokemonId);
        }
        pokemonRepository.deleteById(pokemonId);
    }

    public List<PokemonEntity> findByType(String type) {
        log.info(">>>>> Finding pokemons by type: {}", type);
        return pokemonRepository.findByType(type);
    }

    @CacheEvict(value = "pokemonCache", key = "#pokemonId")
    public PokemonEntity givePowerLevel(int pokemonId, int power) {
        log.info(">>>>> Updating power for ID: {}. It must occur once!", pokemonId);
        PokemonEntity pokemon = pokemonRepository.findById(pokemonId)
                .orElseThrow(() -> new PokemonNotFoundException(pokemonId));
        pokemon.setPower(power);
        return pokemonRepository.save(pokemon);
    }


}
