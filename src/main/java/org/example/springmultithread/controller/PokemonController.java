package org.example.springmultithread.controller;


import org.example.springmultithread.entity.PokemonEntity;
import org.example.springmultithread.service.PokemonService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/pokemon")
public class PokemonController {

    private final PokemonService pokemonService;


    public PokemonController(PokemonService pokemonService) {
        this.pokemonService = pokemonService;
    }

    @GetMapping("{id}/async")
    public CompletableFuture<ResponseEntity<String>> getPokemonAsync(@PathVariable int id) {
        long startTime = System.currentTimeMillis();

        return pokemonService.getPokemonDetailsAsync(id)
                .thenApply(pokemon -> {
                    long duration = System.currentTimeMillis() - startTime;
                    String message = String.format(
                            "Async Find | Thread: %s | ID: %d, Name: %s | Time: %d ms.",
                            Thread.currentThread().getName(), pokemon.getId(), pokemon.getName(), duration
                    );
                    return ResponseEntity.ok(message);
                });
    }

    @PostMapping("/addPokemon")
    public ResponseEntity<PokemonEntity> addPokemon(@RequestBody PokemonEntity pokemon) {
        PokemonEntity saved = pokemonService.addPokemon(pokemon);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getPokemon(@PathVariable int id) {
        long startTime = System.currentTimeMillis();
        PokemonEntity pokemon = pokemonService.getPokemonDetails(id);
        long duration = System.currentTimeMillis() - startTime;

        String responseMessage = String.format(
                "Find Pokemon | ID: %d, Name: %s, Type: %s, Level: %d, Power: %d. | Time: %d ms.",
                pokemon.getId(), pokemon.getName(), pokemon.getType(), pokemon.getLevel(), pokemon.getPower(), duration
        );
        return ResponseEntity.ok(responseMessage);
    }

    @PutMapping("/{id}/level")
    public ResponseEntity<PokemonEntity> updateLevel(@PathVariable int id, @RequestParam int newLevel) {
        PokemonEntity updated = pokemonService.updateLevel(id, newLevel);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePokemon(@PathVariable int id) {
        pokemonService.deletePokemon(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<PokemonEntity>> findByType(@PathVariable String type) {
        return ResponseEntity.ok(pokemonService.findByType(type));
    }

    @PutMapping("/{id}/power")
    public ResponseEntity<PokemonEntity> updatePower(@PathVariable int id, @RequestParam int newPower) {
        PokemonEntity updated = pokemonService.givePowerLevel(id, newPower);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/battle")
    public ResponseEntity<String> battlePokemon(@RequestParam int attackingPokemon, @RequestParam int defendingPokemon) {
        PokemonEntity winnerPokemon = pokemonService.battle(attackingPokemon, defendingPokemon);
        if (winnerPokemon == null) {
            return ResponseEntity.ok("It`s a draw!");
        }
        return ResponseEntity.ok("The battle is over! Winner: " + winnerPokemon.getName() +
                " (Level: " + winnerPokemon.getLevel() + ")");
    }

    @PutMapping("/powerByType")
    public ResponseEntity<List<PokemonEntity>> updatePowerByType(@RequestParam String type, @RequestParam int newPower) {
        List<PokemonEntity> updated = pokemonService.boostPokemonByTypes(type, newPower);
        if (updated.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(updated);
    }

}

