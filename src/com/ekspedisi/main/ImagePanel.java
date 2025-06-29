package com.ekspedisi.main;

import java.awt.Graphics;
import java.awt.Image;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 * Sebuah JPanel kustom yang bisa menampilkan gambar sebagai latar belakangnya.
 * Gambar akan diskalakan agar pas dengan ukuran panel.
 */
public class ImagePanel extends JPanel {

    private Image backgroundImage;

    /**
     * Konstruktor untuk ImagePanel.
     * @param imagePath Path ke gambar di dalam resource (misal: "/images/background.jpg").
     */
    public ImagePanel(String imagePath) {
        try {
            // Memuat gambar dari resource aplikasi
            URL imageUrl = getClass().getResource(imagePath);
            if (imageUrl != null) {
                this.backgroundImage = new ImageIcon(imageUrl).getImage();
            } else {
                System.err.println("File background tidak ditemukan di: " + imagePath);
                this.backgroundImage = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.backgroundImage = null;
        }
    }

    /**
     * Override metode paintComponent untuk menggambar gambar latar belakang.
     * @param g Objek Graphics yang digunakan untuk menggambar.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Jika gambar berhasil dimuat, gambar di seluruh area panel
        if (backgroundImage != null) {
            // Menggambar gambar dengan ukuran yang sama dengan panel (scaling)
            g.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
        }
    }
}