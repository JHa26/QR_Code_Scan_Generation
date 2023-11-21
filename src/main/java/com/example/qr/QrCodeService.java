package com.example.qr;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumMap;
import java.util.Map;

@Service
public class QrCodeService {

    public String readQrCode(BufferedImage image) throws NotFoundException {
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

        Result result = new MultiFormatReader().decode(binaryBitmap, hints);
        return result.getText();
    }

    public Path generateQrCode(String content, String fileName) throws WriterException, IOException {
        int width = 300;
        int height = 300;

        // Speicherpfad definieren
        Path directoryPath = Paths.get("generated-qr-codes");

        try {
            // Verzeichnis erstellen wenn nicht vorhanden
            Files.createDirectories(directoryPath);

            // Dateinamen von Sonderzeichen säubern
            String cleanedFileName = getCleanedFileName(fileName);

            // QR Code generieren
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height);

            // Dateipfad definieren + .png
            Path filePath = directoryPath.resolve(cleanedFileName + ".png");

            // QR Code in Datei speichern
            try (OutputStream outputStream = Files.newOutputStream(filePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
                MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            }

            return filePath;
        } catch (WriterException | IOException e) {
            // Exception Handling
            e.printStackTrace();
            throw new RuntimeException("Error generating QR code: " + e.getMessage());
        }
    }
    public String getCleanedFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9]", "_");
    }

    public String readQrCodeFromUrl(String url) throws IOException, NotFoundException {
        BufferedImage image = ImageIO.read(new URL(url));
        return readQrCode(image);
    }



}