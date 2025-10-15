package hu.nomindz.devkit.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public final class AutoConfigBinder {

    private final ObjectMapper mapper;
    private final Validator validator; // may be null

    public AutoConfigBinder(ObjectMapper mapper, Validator validator) {
        this.mapper = mapper;
        this.validator = validator;
    }

    public <T> T bind(FileConfiguration yaml, Class<T> type) {
        Map<String, Object> nested = sectionToMap(yaml);
        // Jackson convert map -> typed object
        T obj = mapper.convertValue(nested, type);

        // optional validation
        if (validator != null) {
            Set<ConstraintViolation<T>> violations = validator.validate(obj);
            if (!violations.isEmpty()) {
                StringBuilder sb = new StringBuilder("Invalid config:\n");
                for (var v : violations) {
                    sb.append("- ").append(v.getPropertyPath())
                      .append(": ").append(v.getMessage()).append("\n");
                }
                throw new IllegalArgumentException(sb.toString());
            }
        }
        return obj;
    }

    /** Recursively converts Bukkit's YAML to a nested Map for Jackson. */
    private Map<String, Object> sectionToMap(ConfigurationSection section) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (String key : section.getKeys(false)) {
            Object val = section.get(key);
            if (val instanceof ConfigurationSection cs) {
                map.put(key, sectionToMap(cs));
            } else if (val instanceof List<?> list) {
                map.put(key, normalizeList(list));
            } else {
                map.put(key, val);
            }
        }
        return map;
    }

    private Object normalizeList(List<?> list) {
        List<Object> out = new ArrayList<>(list.size());
        for (Object o : list) {
            if (o instanceof ConfigurationSection cs) {
                out.add(sectionToMap(cs));
            } else if (o instanceof List<?> l) {
                out.add(normalizeList(l));
            } else {
                out.add(o);
            }
        }
        return out;
    }

    public static ObjectMapper defaultMapper() {
        return new ObjectMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }
}
