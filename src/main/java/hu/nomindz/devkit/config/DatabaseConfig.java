package hu.nomindz.devkit.config;

import jakarta.validation.constraints.NotBlank;

public record DatabaseConfig(
        @NotBlank String file) {
}