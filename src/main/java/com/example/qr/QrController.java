package com.example.qr;

import com.google.zxing.NotFoundException;
import jakarta.servlet.http.HttpSession;
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
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/")
public class QrController {

    private final QrCodeService qrCodeService;

    @Autowired
    public QrController(QrCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }


    @GetMapping
    public String index(HttpSession session, Model model) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        model.addAttribute("qrCodePath", "");
        model.addAttribute("scannedQrCodeContent", "");
        return "index";
    }

    @Autowired
    private GeocodingService geocodingService;

    @PostMapping("/generate")
    public String generateQRCode(HttpSession session,
                                 @RequestParam String notes,
                                 @RequestParam String city,
                                 @RequestParam String fileName,
                                 @RequestParam("file") MultipartFile file,
                                 Model model) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        try {
            String imageUrl = null;
            if (!file.isEmpty()) {
                imageUrl = fileStorageService.saveUploadedFile(file);
            }

            String location = geocodingService.getCoordinates(city);

            QrCodeData qrCodeData = new QrCodeData();
            qrCodeData.setImageUrl(imageUrl);
            qrCodeData.setNotes(notes);
            qrCodeData.setLocation(location);

            Path filePath = qrCodeService.generateQrCode(qrCodeData, fileName);
            model.addAttribute("qrCodePath", filePath.getFileName().toString());
            return "index";
        } catch (Exception e) {
            model.addAttribute("error", "Error QR Code Erstellung: " + e.getMessage());
            return "index";
        }
    }




    @PostMapping("/scan")
    public String scanQrCode(HttpSession session, @RequestParam("file") MultipartFile file, Model model) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            QrCodeData qrCodeData = qrCodeService.readQrCode(image);

            model.addAttribute("scannedImageUrl", qrCodeData.getImageUrl());
            model.addAttribute("scannedNotes", qrCodeData.getNotes());

            if (qrCodeData.getLocation() != null && !qrCodeData.getLocation().isEmpty()) {
                String[] parts = qrCodeData.getLocation().split(",");
                List<Double> location = Arrays.asList(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
                model.addAttribute("scannedLocation", location);
            } else {
                model.addAttribute("scannedLocation", null);
            }

            return "scannedResult";
        } catch (IOException | NotFoundException e) {
            e.printStackTrace();
            model.addAttribute("error", "Error scanning QR code: " + e.getMessage());
            return "index";
        }
    }


    @Autowired
    private FileStorageService fileStorageService;


}