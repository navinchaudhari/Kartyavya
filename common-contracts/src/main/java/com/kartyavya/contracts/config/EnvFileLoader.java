package com.kartyavya.contracts.config;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads the single project-root .env file before Spring Boot reads application
 * configuration. Values are promoted to JVM system properties so they are
 * available to Spring Cloud Config placeholders as well as local properties.
 */
public final class EnvFileLoader {
    private static final String FILE_NAME = ".env";
    private static final Pattern REFERENCE = Pattern.compile("\\$\\{([A-Za-z_][A-Za-z0-9_]*)}");

    private EnvFileLoader() {
    }

    public static Path load() {
        Path envFile = locate();
        Map<String, String> rawValues = parse(envFile);
        Map<String, String> resolvedValues = new LinkedHashMap<>();

        for (String key : rawValues.keySet()) {
            resolve(key, rawValues, resolvedValues, new HashSet<>());
        }

        // .env is the local source of truth. JVM properties have higher
        // precedence than operating-system environment variables in Spring.
        resolvedValues.forEach(System::setProperty);
        System.setProperty("KARTYAVYA_ENV_FILE", envFile.toAbsolutePath().normalize().toString());
        return envFile;
    }

    private static Map<String, String> parse(Path envFile) {
        List<String> lines;
        try {
            lines = Files.readAllLines(envFile, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read " + envFile, exception);
        }

        Map<String, String> values = new LinkedHashMap<>();
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index).trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            if (line.startsWith("export ")) {
                line = line.substring("export ".length()).trim();
            }

            int separator = line.indexOf('=');
            if (separator <= 0) {
                throw new IllegalStateException(
                        "Invalid .env entry at line " + (index + 1) + ": expected KEY=value");
            }

            String key = line.substring(0, separator).trim();
            validateKey(key, index + 1);
            String value = removeMatchingQuotes(line.substring(separator + 1).trim());
            if (values.putIfAbsent(key, value) != null) {
                throw new IllegalStateException("Duplicate .env key at line " + (index + 1) + ": " + key);
            }
        }
        return values;
    }

    private static String resolve(
            String key,
            Map<String, String> rawValues,
            Map<String, String> resolvedValues,
            Set<String> resolving) {
        if (resolvedValues.containsKey(key)) {
            return resolvedValues.get(key);
        }
        if (!resolving.add(key)) {
            throw new IllegalStateException("Circular .env reference detected for " + key);
        }

        String rawValue = rawValues.get(key);
        Matcher matcher = REFERENCE.matcher(rawValue);
        StringBuffer output = new StringBuffer();
        while (matcher.find()) {
            String referencedKey = matcher.group(1);
            if (!rawValues.containsKey(referencedKey)) {
                throw new IllegalStateException(
                        ".env variable " + key + " references missing variable " + referencedKey);
            }
            String replacement = resolve(referencedKey, rawValues, resolvedValues, resolving);
            matcher.appendReplacement(output, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(output);

        resolving.remove(key);
        String resolved = output.toString();
        resolvedValues.put(key, resolved);
        return resolved;
    }

    private static Path locate() {
        Set<Path> startingPoints = new LinkedHashSet<>();
        startingPoints.add(Paths.get(System.getProperty("user.dir", ".")));

        CodeSource source = EnvFileLoader.class.getProtectionDomain().getCodeSource();
        if (source != null && source.getLocation() != null) {
            try {
                Path codePath = Paths.get(source.getLocation().toURI());
                startingPoints.add(Files.isDirectory(codePath) ? codePath : codePath.getParent());
            } catch (URISyntaxException | IllegalArgumentException ignored) {
                // user.dir search remains available.
            }
        }

        for (Path startingPoint : startingPoints) {
            Path found = searchParents(startingPoint);
            if (found != null) {
                return found;
            }
        }

        throw new IllegalStateException(
                "Kartyavya .env file was not found. Keep exactly one .env file in the Kartyavya project root.");
    }

    private static Path searchParents(Path startingPoint) {
        Path current = startingPoint.toAbsolutePath().normalize();
        for (int depth = 0; current != null && depth < 8; depth++) {
            Path candidate = current.resolve(FILE_NAME);
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
            current = current.getParent();
        }
        return null;
    }

    private static void validateKey(String key, int lineNumber) {
        if (!key.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new IllegalStateException("Invalid .env key at line " + lineNumber + ": " + key);
        }
    }

    private static String removeMatchingQuotes(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
}
