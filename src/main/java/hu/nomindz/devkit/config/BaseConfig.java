package hu.nomindz.devkit.config;

import jakarta.validation.constraints.Min;

public interface BaseConfig {
    @Min(1) int config_version();
    DatabaseConfig database();
}
