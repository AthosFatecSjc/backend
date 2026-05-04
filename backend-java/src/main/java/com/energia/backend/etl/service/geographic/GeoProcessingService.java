package com.energia.backend.etl.service.geographic;

import java.io.IOException;

import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@Service
public class GeoProcessingService {

    public String converterParaGeoJson(InMemoryGdb gdb) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                "ogr2ogr",
                "-f", "GeoJSON",
                "/vsistdout/",
                gdb.gdalSourcePath(),
                "CONJ"
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (OutputStream os = process.getOutputStream()) {
            os.write(gdb.zipBytes());
            os.flush();
        }

        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Erro ao converter GDB para GeoJSON. Saída do ogr2ogr:\n" + output);
        }
        System.out.println("GEOJSON É: " + output);

        return output;
    }
}