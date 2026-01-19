package org.example.springmultithread.exceptions;

public class PokemonNotFoundException extends RuntimeException {

    public PokemonNotFoundException(int id) {
        super("Pokemon with id " + id + " not found");
    }
}
