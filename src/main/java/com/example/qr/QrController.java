package com.example.qr;

import com.google.zxing.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

@Controller
@RequestMapping("/")
public class QrController {

    private final QrCodeService qrCodeService;

    @Autowired
    public QrController(QrCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("qrCodePath", "");
        model.addAttribute("scannedQrCodeContent", "");
        return "index";
    }

    @PostMapping("/generate")
    public String generateQRCode(@RequestParam String content, @RequestParam String fileName, Model model) {
        try {
            String cleanedFileName = qrCodeService.getCleanedFileName(fileName);
            Path filePath = qrCodeService.generateQrCode(content, fileName);

            // Pfad im model festlegen
            model.addAttribute("qrCodePath", cleanedFileName + ".png");

            // QR Code behalten anstatt löschen
            model.addAttribute("scannedQrCodeContent", "");

            return "index";
        } catch (Exception e) {
            // Exception Handling
            e.printStackTrace();
            model.addAttribute("error", "Error generating QR code: " + e.getMessage());
            return "index";
        }
    }

    @PostMapping("/scan")
    public String scanQrCode(@RequestParam("file") MultipartFile file, Model model) {
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            String qrCodeText = qrCodeService.readQrCode(image);

            // scannedQrCodeContent model hinzufügen
            model.addAttribute("scannedQrCodeContent", qrCodeText);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);

            return "index";
        } catch (IOException | NotFoundException e) {
            e.printStackTrace();
            model.addAttribute("error", "Error scanning QR code: " + e.getMessage());
            return "index";
        }
    }
}