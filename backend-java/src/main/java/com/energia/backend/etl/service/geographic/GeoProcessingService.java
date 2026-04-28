package com.energia.backend.etl.service.geographic;

import java.io.File;
import java.io.IOException;

import org.springframework.stereotype.Service;

@Service
public class GeoProcessingService {
    public File converterParaGeoJson(File gdbDir) throws IOException, InterruptedException, java.io.IOException {
        File geoJson = new File(gdbDir.getParent(), "output.json");

        ProcessBuilder pb = new ProcessBuilder(
                "ogr2ogr",
                "-f", "GeoJSON",
                geoJson.getAbsolutePath(),
                gdbDir.getAbsolutePath(),
                "CONJ");
        pb.redirectErrorStream(true);
        Process process = pb.start();
        String output = new String(process.getInputStream().readAllBytes());
        int exitCode = process.waitFor();
        System.out.println("OGR2OGR OUTPUT:\n" + output);

        if (exitCode != 0) {
            throw new RuntimeException("Erro ao converter GDB para GeoJSON");
        }

        return geoJson;
    }

}
