package com.ekspedisi.util;

// --- IMPORT YANG BENAR DARI LIBRARY METADATA-EXTRACTOR ---
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;

// --- IMPORT DARI JAVA AWT UNTUK MANIPULASI GAMBAR ---
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * Kelas utilitas untuk membaca metadata EXIF dan merotasi gambar sesuai orientasinya.
 * Menggunakan library 'metadata-extractor'.
 */
public class ImageRotator {

    /**
     * Membaca orientasi dari metadata EXIF sebuah gambar.
     * @param inputStream Stream dari file gambar.
     * @return Nilai orientasi dari tag EXIF (1-8), atau 1 (normal) jika tidak ditemukan atau terjadi error.
     */
    public static int getExifOrientation(InputStream inputStream) throws MetadataException {
        try {
            // Membaca semua metadata dari gambar
            Metadata metadata = ImageMetadataReader.readMetadata(inputStream);
            // Mencari direktori khusus untuk orientasi (EXIF IFD0)
            ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            
            // Jika direktori ditemukan dan memiliki tag orientasi...
            if (directory != null && directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                // ...kembalikan nilai orientasinya
                return directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
            }
        } catch (ImageProcessingException | IOException e) {
            // Abaikan error, karena orientasi mungkin tidak ada. Cukup kembalikan nilai default.
            // e.printStackTrace(); 
        }
        return 1; // 1 = Normal (tidak ada rotasi)
    }

    /**
     * Merotasi sebuah BufferedImage berdasarkan flag orientasi EXIF.
     * @param image Gambar yang akan dirotasi.
     * @param orientation Flag orientasi (1-8) yang didapat dari getExifOrientation().
     * @return Gambar baru yang sudah dirotasi, atau gambar asli jika tidak perlu rotasi.
     */
    public static BufferedImage rotateImage(BufferedImage image, int orientation) {
        if (orientation <= 1 || image == null) {
            return image; // Tidak perlu rotasi
        }

        int width = image.getWidth();
        int height = image.getHeight();
        AffineTransform transform = new AffineTransform();
        
        // Logika rotasi berdasarkan nilai tag orientasi EXIF
        switch (orientation) {
            case 2: transform.scale(-1.0, 1.0); transform.translate(-width, 0); break; // Flip horizontal
            case 3: transform.translate(width, height); transform.rotate(Math.PI); break; // Putar 180°
            case 4: transform.scale(1.0, -1.0); transform.translate(0, -height); break; // Flip vertikal
            case 5: transform.rotate(-Math.PI / 2); transform.scale(-1.0, 1.0); break; // Putar -90° & flip vertikal
            case 6: transform.translate(height, 0); transform.rotate(Math.PI / 2); break; // Putar 90°
            case 7: transform.rotate(Math.PI / 2); transform.scale(-1.0, 1.0); transform.translate(-height, 0); break; // Putar 90° & flip vertikal
            case 8: transform.translate(0, width); transform.rotate(-Math.PI / 2); break; // Putar -90°
        }
        
        // Jika gambar dirotasi 90/-90 derajat, lebar dan tinggi akan tertukar
        int newWidth = (orientation >= 5 && orientation <= 8) ? height : width;
        int newHeight = (orientation >= 5 && orientation <= 8) ? width : height;

        BufferedImage newImage = new BufferedImage(newWidth, newHeight, image.getType());
        Graphics2D g = newImage.createGraphics();
        g.drawImage(image, transform, null);
        g.dispose();

        return newImage;
    }
}