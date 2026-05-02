package com.energia.backend.etl.service.geographic;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.util.LinkedHashSet;
import java.util.Set;

record InMemoryZip(String itemId, byte[] bytes) {}

record InMemoryGdb(String itemId, byte[] zipBytes, String gdbDirectoryName) {
    public String gdalSourcePath() {
        // o zip entra pelo stdin, e o GDAL lê o conteúdo sem gravar em disco
        return "/vsizip/{/vsistdin?buffer_limit=-1}/" + gdbDirectoryName;
    }
}

@Service
public class DistDownloadService {
    private static final String ITEM_DATA_URL = "https://www.arcgis.com/sharing/rest/content/items/%s/data";

    private final RestTemplate restTemplate = new RestTemplate();

    public InMemoryZip downloadGdb(String itemId) {
        String url = String.format(ITEM_DATA_URL, itemId);
        byte[] data = restTemplate.getForObject(url, byte[].class);

        if (data == null || data.length == 0) {
            throw new IllegalStateException("O ZIP veio vazio para o itemId: " + itemId);
        }

        return new InMemoryZip(itemId, data);
    }

    public InMemoryGdb unzip(InMemoryZip zip) throws IOException {
        Set<String> gdbRoots = new LinkedHashSet<>();

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zip.bytes()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();

                // Espera algo como "alguma_coisa.gdb/CONJ/..." ou "alguma_coisa.gdb/..."
                String root = name;
                int slash = name.indexOf('/');
                if (slash > 0) {
                    root = name.substring(0, slash);
                }

                if (root.endsWith(".gdb")) {
                    gdbRoots.add(root);
                }

                zis.closeEntry();
            }
        }

        if (gdbRoots.isEmpty()) {
            throw new IllegalStateException("Nenhuma pasta .gdb encontrada dentro do ZIP do itemId: " + zip.itemId());
        }
        if (gdbRoots.size() > 1) {
            throw new IllegalStateException("Foi encontrada mais de uma pasta .gdb dentro do ZIP do itemId: " + zip.itemId());
        }

        return new InMemoryGdb(zip.itemId(), zip.bytes(), gdbRoots.iterator().next());
    }
}