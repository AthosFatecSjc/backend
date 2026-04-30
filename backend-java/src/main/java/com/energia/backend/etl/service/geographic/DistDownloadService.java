package com.energia.backend.etl.service.geographic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DistDownloadService {
    private static final String ITEM_DATA_URL = "https://www.arcgis.com/sharing/rest/content/items/%s/data";

    private final RestTemplate restTemplate = new RestTemplate();

    public File downloadGdb(String itemId) throws IOException, java.io.IOException {
        String url = String.format(ITEM_DATA_URL, itemId);

        byte[] data = restTemplate.getForObject(url, byte[].class);

        File zipFile = Files.createTempFile(itemId, ".zip").toFile();
        Files.write(zipFile.toPath(), data);

        return zipFile;

    }

    public File unzip(File zipFile) throws IOException {
        File destDir = new File(zipFile.getParent(), zipFile.getName() + "_dir");

        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));

        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            File newFile = new File(destDir, zipEntry.getName());

            if (zipEntry.isDirectory()) {
                newFile.mkdirs();
            } else {
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();

        File gdb = encontrarGdb(destDir);

        log.info("GDB encontrado em: {}", gdb.getAbsolutePath());

        return gdb; 
    }

    private File encontrarGdb(File dir) {
        File[] files = dir.listFiles();

        if (files == null) {
            throw new RuntimeException("Diretório vazio: " + dir.getAbsolutePath());
        }

        for (File file : files) {
            if (file.isDirectory() && file.getName().endsWith(".gdb")) {
                return file;
            }
        }
        throw new RuntimeException("Nenhum .gdb encontrado em: " + dir.getAbsolutePath());
    }

}
