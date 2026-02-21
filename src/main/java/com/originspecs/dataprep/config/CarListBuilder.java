package com.originspecs.dataprep.config;

import com.originspecs.dataprep.model.CarBrand;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class CarListBuilder {

    public static List<CarBrand> populateBrandList(String fileName) {

        Path path = Path.of(fileName);

        try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {
            return lines
                    .skip(1) //skip header row
                    .filter(line -> !line.isBlank())
                    .map(line -> {
                        String[] parts = line.split(",");
                        return new CarBrand(parts[0].trim(), parts[1].trim());
                    })
                    .toList();
        } catch (IOException e){
            log.error("Error reading file {}", path);
        }
        return List.of();
    }
}
