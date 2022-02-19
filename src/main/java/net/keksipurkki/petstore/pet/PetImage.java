package net.keksipurkki.petstore.pet;

import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.FileUpload;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

public record PetImage(Buffer bytes, String filename, String contentType, String metadata) {

    public static PetImage from(FileUpload upload, String metadata) throws IOException {
        var bytes = Files.readAllBytes(Path.of(upload.uploadedFileName()));
        return new PetImage(
            Buffer.buffer(bytes),
            upload.fileName(),
            upload.contentType(),
            metadata
        );
    }

    public URI toUri() {
        var str = String.format("http://localhost:%d/static/%s", 3000, filename);
        return URI.create(str);
    }
}
