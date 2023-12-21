package com.example.qr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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


    public String decodeQrCode(BufferedImage image) throws NotFoundException {
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

        Result result = new MultiFormatReader().decode(binaryBitmap, hints);
        return result.getText();
    }


    public QrCodeData readQrCode(BufferedImage image) throws IOException, NotFoundException {
        String qrCodeText = decodeQrCode(image); // Corrected to call the right method

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(qrCodeText, QrCodeData.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new IOException("Error parsing QR code content", e);
        }
    }
    public Path generateQrCode(QrCodeData qrCodeData, String fileName) throws WriterException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonContent = objectMapper.writeValueAsString(qrCodeData);
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
            BitMatrix bitMatrix = qrCodeWriter.encode(jsonContent, BarcodeFormat.QR_CODE, width, height);

            // Dateipfad definieren + .png
            Path filePath = directoryPath.resolve(cleanedFileName + ".png");

            // QR Code in Datei speichern
            try (OutputStream outputStream = Files.newOutputStream(filePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
                MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            }

            return filePath;
        } catch (WriterException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating QR code: " + e.getMessage());
        }
    }
    public String getCleanedFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9]", "_");
    }





}