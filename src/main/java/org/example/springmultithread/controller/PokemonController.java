package org.example.springmultithread.controller;


import org.example.springmultithread.entity.PokemonEntity;
import org.example.springmultithread.service.PokemonService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pokemon")
public class PokemonController {

    private final PokemonService pokemonService;


    public PokemonController(PokemonService pokemonService) {
        this.pokemonService = pokemonService;
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

        if (pokemon == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(String.format("Pokemon not found with id %d. Time: %d ms.", id, duration));
        }

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
}