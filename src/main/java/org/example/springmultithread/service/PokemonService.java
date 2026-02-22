package org.example.springmultithread.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.springmultithread.entity.PokemonEntity;
import org.example.springmultithread.exceptions.GlobalExceptionHandler;
import org.example.springmultithread.exceptions.PokemonNotFoundException;
import org.example.springmultithread.repository.PokemonRepository;

import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
        simulateSlowService();
        return pokemonRepository.findById(pokemonId)
                .orElseThrow(() -> new PokemonNotFoundException(pokemonId));
    }

    private static void simulateSlowService() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Async("pokemonExecutor")
    public CompletableFuture<PokemonEntity> getPokemonDetailsAsync(@PathVariable int pokemonId) {
        log.info(">>>> [THREAD: {}] Async loading for Id: {}",
                Thread.currentThread().getName(), pokemonId);
        PokemonEntity pokemon = getPokemonDetails(pokemonId);
        return CompletableFuture.completedFuture(pokemon);

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

    @Transactional
    @CacheEvict(value = "pokemonCache", key = "#pokemonId")
    public PokemonEntity trainPowerLevel(int pokemonId, int intensity) {
        log.info(">>>>> Train your Pokemon {}. It must occur once!", pokemonId);
        PokemonEntity pokemon = pokemonRepository.findById(pokemonId)
                .orElseThrow(() -> new PokemonNotFoundException(pokemonId));
        if (intensity > 100) {
            log.warn("Too intensive training!");
            return pokemon;
        } else {
            if (pokemon.getPower() <= intensity) {
                pokemon.setPower(pokemon.getPower() + intensity);
            }
            if (intensity == 100) {
                pokemon.setLevel(pokemon.getLevel() + 1);
                log.info("Max intensity! Level Up!");
            }
        }
        pokemonRepository.save(pokemon);
        return pokemon;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "pokemonCache", key = "#attackerId"),
            @CacheEvict(value = "pokemonCache", key = "#defenderId")
    })
    public PokemonEntity battle(int attackerId, int defenderId) {
        log.info("The battle has started! {} attacking {}!", attackerId, defenderId);
        PokemonEntity attackingPokemon = pokemonRepository.findById(attackerId)
                .orElseThrow(() -> new PokemonNotFoundException(attackerId));
        PokemonEntity defendingPokemon = pokemonRepository.findById(defenderId)
                .orElseThrow(() -> new PokemonNotFoundException(defenderId));
        PokemonEntity winnerPokemon;
        if (attackingPokemon.getPower() == defendingPokemon.getPower()) {
            log.info("Nothing happened!");
            return null;
        }
        if (attackingPokemon.getPower() > defendingPokemon.getPower()) {
            attackingPokemon.setPower(attackingPokemon.getPower() + 10);
            attackingPokemon.setLevel(attackingPokemon.getLevel() + 1);
            winnerPokemon = pokemonRepository.save(attackingPokemon);
            pokemonRepository.delete(defendingPokemon);
        } else {
            defendingPokemon.setPower(defendingPokemon.getPower() + 10);
            defendingPokemon.setLevel(defendingPokemon.getLevel() + 1);
            winnerPokemon = pokemonRepository.save(defendingPokemon);
            pokemonRepository.delete(attackingPokemon);
        }
        return winnerPokemon;
    }

    @Transactional
    @CacheEvict(value = "pokemonCache", allEntries = true)
    public List<PokemonEntity> boostPokemonByTypes(String type, int powerBoost) {
        log.info(">>>>> Boost pokemons by type: {}, by {}", type, powerBoost);
        List<PokemonEntity> pokemonsByType = pokemonRepository.findByType(type);
        if (pokemonsByType.isEmpty()) {
            log.warn("No pokemon found for type: {}", type);
            return pokemonsByType;
        }
        pokemonsByType.forEach(pokemon -> {
            pokemon.setPower(pokemon.getPower() + powerBoost);
        });

        return pokemonRepository.saveAll(pokemonsByType);
    }

}
