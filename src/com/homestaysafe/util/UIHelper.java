package com.homestaysafe.util;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class UIHelper {

    public static final Color COLOR_NAVY = new Color(15, 23, 42);
    public static final Color COLOR_NAVY_HOVER = new Color(30, 41, 59);
    public static final Color COLOR_NAVY_PRESS = new Color(10, 16, 30);
    public static final Color COLOR_BG = new Color(245, 247, 250);
    public static final Color COLOR_GOLD = new Color(197, 168, 128);
    public static final Color COLOR_GOLD_HOVER = new Color(180, 148, 105);
    public static final Color COLOR_GOLD_PRESS = new Color(160, 130, 90);
    public static final Color COLOR_DANGER = new Color(220, 38, 38);
    public static final Color COLOR_DANGER_HOVER = new Color(185, 28, 28);
    public static final Color COLOR_SUCCESS = new Color(22, 163, 74);
    public static final Color COLOR_SUCCESS_HOVER = new Color(21, 128, 61);
    public static final Color COLOR_SOFT = new Color(241, 245, 249);
    public static final Color COLOR_SOFT_HOVER = new Color(226, 232, 240);
    public static final Color COLOR_BORDER = new Color(226, 232, 240);
    public static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);
    public static final Color COLOR_TEXT_DARK = new Color(30, 41, 59);

    private static final Map<String, Icon> IMAGE_CACHE = new HashMap<>();

    // =========================================================================
    // IMAGE & LOGO HELPERS (HD BICUBIC + HIDPI + AUTO-CROP LOGO HITAM)
    // =========================================================================

    private static File resolveImageBaseDir() {
        String[] candidates = {"gambar", "../gambar", "src/gambar", "resources/gambar"};
        for (String c : candidates) {
            File dir = new File(c);
            if (dir.exists() && dir.isDirectory()) return dir;
        }
        return new File("gambar");
    }

    /** Memuat logo HomestaySafe, memotong margin hitam/putih, dan menjadikannya transparan + HD */
    public static Icon getLogoIcon(int width, int height) {
        String key = "LOGO_HD_" + width + "x" + height;
        if (IMAGE_CACHE.containsKey(key)) return IMAGE_CACHE.get(key);

        File base = resolveImageBaseDir();
        String[] names = {
                "logo.png", "logo.jpg", "logo.jpeg",
                "Homestaysafe logo.png", "Homestaysafe logo.jpg", "Homestaysafe logo.jpeg"
        };
        File found = null;
        for (String n : names) {
            File f = new File(base, n);
            if (f.exists() && f.isFile()) { found = f; break; }
        }

        if (found != null) {
            try {
                BufferedImage src = ImageIO.read(found);
                if (src != null) {
                    BufferedImage cropped = trimSolidBorder(src);
                    BufferedImage transparent = makeBackgroundTransparent(cropped);

                    // Render di resolusi 2x agar tajam di layar HiDPI Windows
                    int hiW = width * 2;
                    int hiH = height * 2;
                    double scale = Math.min((double) hiW / transparent.getWidth(), (double) hiH / transparent.getHeight());
                    int scaledW = Math.max(1, (int) Math.round(transparent.getWidth() * scale));
                    int scaledH = Math.max(1, (int) Math.round(transparent.getHeight() * scale));

                    BufferedImage scaledImg = getHighQualityScaledInstance(transparent, scaledW, scaledH);
                    BufferedImage canvas = new BufferedImage(hiW, hiH, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2 = canvas.createGraphics();
                    applyQualityHints(g2);
                    g2.drawImage(scaledImg, (hiW - scaledW) / 2, (hiH - scaledH) / 2, null);
                    g2.dispose();

                    Icon icon = new HiDpiIcon(canvas, width, height);
                    IMAGE_CACHE.put(key, icon);
                    return icon;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Icon fallback = createFallbackLogo(width, height);
        IMAGE_CACHE.put(key, fallback);
        return fallback;
    }

    /** Memuat foto tipe kamar dari gambar/tipe kamar/ dengan kualitas HD */
    public static Icon getKamarImageIcon(String namaTipe, int width, int height, int radius) {
        String safeName = (namaTipe == null ? "standard" : namaTipe.trim().toLowerCase());
        String key = "KAMAR_HD_" + safeName + "_" + width + "x" + height + "_" + radius;
        if (IMAGE_CACHE.containsKey(key)) return IMAGE_CACHE.get(key);

        File dir = new File(resolveImageBaseDir(), "tipe kamar");
        File file = findMatchingImageFile(dir, safeName);
        Icon icon = (file != null)
                ? loadRoundedCoverIcon(file, width, height, radius)
                : createPlaceholderIcon(width, height, radius, namaTipe != null ? namaTipe : "Kamar", COLOR_NAVY);

        IMAGE_CACHE.put(key, icon);
        return icon;
    }

    /** Memuat foto menu F&B dari gambar/menu/ (minuman, signature, makanan) dengan kualitas HD */
    public static Icon getMenuImageIcon(String namaMenu, int width, int height, int radius) {
        String safeName = (namaMenu == null ? "menu" : namaMenu.trim().toLowerCase());
        String key = "MENU_HD_" + safeName + "_" + width + "x" + height + "_" + radius;
        if (IMAGE_CACHE.containsKey(key)) return IMAGE_CACHE.get(key);

        File menuRoot = new File(resolveImageBaseDir(), "menu");
        String[] subFolders = {"signature", "minuman", "makanan", ""};
        File found = null;
        for (String sub : subFolders) {
            File dir = sub.isEmpty() ? menuRoot : new File(menuRoot, sub);
            found = findMatchingImageFile(dir, safeName);
            if (found != null) break;
        }

        Icon icon = (found != null)
                ? loadRoundedCoverIcon(found, width, height, radius)
                : createPlaceholderIcon(width, height, radius, namaMenu != null ? namaMenu : "Menu F&B", new Color(51, 65, 85));

        IMAGE_CACHE.put(key, icon);
        return icon;
    }

    private static File findMatchingImageFile(File dir, String targetName) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) return null;
        File[] files = dir.listFiles();
        if (files == null) return null;

        String normTarget = normalizeKey(targetName);
        File partialMatch = null;

        for (File f : files) {
            if (!f.isFile()) continue;
            String fname = f.getName().toLowerCase();
            if (!(fname.endsWith(".jpg") || fname.endsWith(".jpeg") || fname.endsWith(".png") || fname.endsWith(".webp"))) {
                continue;
            }
            int dot = fname.lastIndexOf('.');
            String baseName = dot > 0 ? fname.substring(0, dot) : fname;
            String normFile = normalizeKey(baseName);

            if (normFile.equals(normTarget)) return f;
            if (partialMatch == null && (normFile.contains(normTarget) || normTarget.contains(normFile))) {
                partialMatch = f;
            }
        }
        return partialMatch;
    }

    private static String normalizeKey(String s) {
        if (s == null) return "";
        return s.toLowerCase()
                .replace("excutive", "executive")
                .replace("amaricano", "americano")
                .replace("machiato", "macchiato")
                .replace("coffe ", "caffe ")
                .replace("lyceed", "lychee")
                .replace("daiquri", "daiquiri")
                .replace("spesial", "special")
                .replaceAll("[^a-z0-9]", "");
    }

    /** Multi-step Bicubic Downscaling + 2x HiDPI Icon agar foto tidak burik/pecah */
    private static Icon loadRoundedCoverIcon(File file, int targetW, int targetH, int radius) {
        try {
            BufferedImage src = ImageIO.read(file);
            if (src == null) return null;

            int hiW = targetW * 2;
            int hiH = targetH * 2;
            int hiRadius = radius * 2;

            double scale = Math.max((double) hiW / src.getWidth(), (double) hiH / src.getHeight());
            int scaledW = Math.max(1, (int) Math.round(src.getWidth() * scale));
            int scaledH = Math.max(1, (int) Math.round(src.getHeight() * scale));

            BufferedImage scaled = getHighQualityScaledInstance(src, scaledW, scaledH);
            BufferedImage out = new BufferedImage(hiW, hiH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = out.createGraphics();
            applyQualityHints(g2);
            g2.setClip(new RoundRectangle2D.Float(0, 0, hiW, hiH, hiRadius, hiRadius));

            int x = (hiW - scaledW) / 2;
            int y = (hiH - scaledH) / 2;
            g2.drawImage(scaled, x, y, null);
            g2.dispose();
            return new HiDpiIcon(out, targetW, targetH);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Mengecilkan gambar bertahap (50% per step) menggunakan Bicubic agar detail tetap tajam */
    private static BufferedImage getHighQualityScaledInstance(BufferedImage img, int targetWidth, int targetHeight) {
        int type = BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = img;
        int w = img.getWidth();
        int h = img.getHeight();

        if (w <= targetWidth || h <= targetHeight) {
            BufferedImage tmp = new BufferedImage(targetWidth, targetHeight, type);
            Graphics2D g2 = tmp.createGraphics();
            applyQualityHints(g2);
            g2.drawImage(ret, 0, 0, targetWidth, targetHeight, null);
            g2.dispose();
            return tmp;
        }

        do {
            if (w > targetWidth) {
                w /= 2;
                if (w < targetWidth) w = targetWidth;
            }
            if (h > targetHeight) {
                h /= 2;
                if (h < targetHeight) h = targetHeight;
            }
            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            applyQualityHints(g2);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();
            ret = tmp;
        } while (w != targetWidth || h != targetHeight);

        return ret;
    }

    private static void applyQualityHints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
    }

    /** Memotong margin solid (hitam atau putih) di pinggir logo agar perisai tampil penuh */
    private static BufferedImage trimSolidBorder(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        int cornerRgb = img.getRGB(0, 0);
        int bgR = (cornerRgb >> 16) & 0xFF;
        int bgG = (cornerRgb >> 8) & 0xFF;
        int bgB = cornerRgb & 0xFF;

        int minX = w, minY = h, maxX = 0, maxY = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = img.getRGB(x, y);
                int alpha = (argb >> 24) & 0xFF;
                if (alpha < 20) continue;

                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                int diff = Math.abs(r - bgR) + Math.abs(g - bgG) + Math.abs(b - bgB);
                if (diff > 35) {
                    if (x < minX) minX = x;
                    if (y < minY) minY = y;
                    if (x > maxX) maxX = x;
                    if (y > maxY) maxY = y;
                }
            }
        }
        if (maxX <= minX || maxY <= minY) return img;
        int pad = Math.max(4, Math.min(w, h) / 60);
        minX = Math.max(0, minX - pad);
        minY = Math.max(0, minY - pad);
        maxX = Math.min(w - 1, maxX + pad);
        maxY = Math.min(h - 1, maxY + pad);
        return img.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    /** Menghapus latar belakang solid (hitam pekat atau putih polos) di luar objek logo */
    private static BufferedImage makeBackgroundTransparent(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        int cornerRgb = src.getRGB(0, 0);
        int cornerAlpha = (cornerRgb >> 24) & 0xFF;
        if (cornerAlpha < 20) return src; // Sudah transparan

        int bgR = (cornerRgb >> 16) & 0xFF;
        int bgG = (cornerRgb >> 8) & 0xFF;
        int bgB = cornerRgb & 0xFF;

        boolean isBlackBg = (bgR < 25 && bgG < 25 && bgB < 25);
        boolean isWhiteBg = (bgR > 235 && bgG > 235 && bgB > 235);
        if (!isBlackBg && !isWhiteBg) return src;

        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = src.getRGB(x, y);
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                int diff = Math.abs(r - bgR) + Math.abs(g - bgG) + Math.abs(b - bgB);
                if (diff < 24) {
                    out.setRGB(x, y, 0x00000000);
                } else if (diff < 52) {
                    int alpha = (int) (((diff - 24) / 28.0) * 255);
                    out.setRGB(x, y, (alpha << 24) | (r << 16) | (g << 8) | b);
                } else {
                    out.setRGB(x, y, (0xFF << 24) | (r << 16) | (g << 8) | b);
                }
            }
        }
        return out;
    }

    /** Custom Icon yang merender buffer 2x ke ukuran logis 1x agar tajam di layar HiDPI */
    private static class HiDpiIcon implements Icon {
        private final BufferedImage hiResImage;
        private final int logicalWidth;
        private final int logicalHeight;

        public HiDpiIcon(BufferedImage hiResImage, int logicalWidth, int logicalHeight) {
            this.hiResImage = hiResImage;
            this.logicalWidth = logicalWidth;
            this.logicalHeight = logicalHeight;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            applyQualityHints(g2);
            g2.drawImage(hiResImage, x, y, logicalWidth, logicalHeight, null);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return logicalWidth;
        }

        @Override
        public int getIconHeight() {
            return logicalHeight;
        }
    }

    private static Icon createFallbackLogo(int w, int h) {
        BufferedImage img = new BufferedImage(w * 2, h * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        applyQualityHints(g2);
        g2.setColor(COLOR_NAVY);
        g2.fill(new RoundRectangle2D.Float(4, 4, w * 2 - 8, h * 2 - 8, 28, 28));
        g2.setColor(COLOR_GOLD);
        g2.setStroke(new BasicStroke(4f));
        g2.draw(new RoundRectangle2D.Float(4, 4, w * 2 - 8, h * 2 - 8, 28, 28));
        g2.setFont(new Font("Serif", Font.BOLD, Math.max(24, h)));
        FontMetrics fm = g2.getFontMetrics();
        String txt = "HS";
        g2.drawString(txt, (w * 2 - fm.stringWidth(txt)) / 2, (h * 2 + fm.getAscent() - fm.getDescent()) / 2);
        g2.dispose();
        return new HiDpiIcon(img, w, h);
    }

    private static Icon createPlaceholderIcon(int w, int h, int radius, String label, Color bg) {
        BufferedImage img = new BufferedImage(w * 2, h * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        applyQualityHints(g2);
        g2.setColor(bg);
        g2.fill(new RoundRectangle2D.Float(0, 0, w * 2, h * 2, radius * 2, radius * 2));
        g2.setColor(COLOR_GOLD);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 24));
        FontMetrics fm = g2.getFontMetrics();
        String shortText = label.length() > 22 ? label.substring(0, 20) + ".." : label;
        g2.drawString(shortText, (w * 2 - fm.stringWidth(shortText)) / 2, (h * 2 + fm.getAscent() - fm.getDescent()) / 2);
        g2.dispose();
        return new HiDpiIcon(img, w, h);
    }

    // =========================================================================
    // BUTTON, CARD & FIELD STYLES
    // =========================================================================

    public static JButton primaryButton(String text) {
        return createRoundedButton(text, COLOR_NAVY, COLOR_NAVY_HOVER, COLOR_NAVY_PRESS, Color.WHITE, 12, 14);
    }

    public static JButton goldButton(String text) {
        return createRoundedButton(text, COLOR_GOLD, COLOR_GOLD_HOVER, COLOR_GOLD_PRESS, Color.WHITE, 12, 14);
    }

    public static JButton dangerButton(String text) {
        return createRoundedButton(text, COLOR_DANGER, COLOR_DANGER_HOVER, COLOR_DANGER.darker(), Color.WHITE, 12, 14);
    }

    public static JButton successButton(String text) {
        return createRoundedButton(text, COLOR_SUCCESS, COLOR_SUCCESS_HOVER, COLOR_SUCCESS.darker(), Color.WHITE, 12, 14);
    }

    public static JButton softButton(String text) {
        return createRoundedButton(text, COLOR_SOFT, COLOR_SOFT_HOVER, new Color(203, 213, 225), COLOR_TEXT_DARK, 12, 13);
    }

    public static JButton createRoundedButton(String text, Color bg, Color hoverBg, Color fg, int radius) {
        Color press = darker(bg, 0.85f);
        return createRoundedButton(text, bg, hoverBg, press, fg, radius, 13);
    }

    public static JButton createRoundedButton(String text, Color bg, Color hoverBg, Color pressBg,
                                              Color fg, int radius, int fontSize) {
        JButton btn = new JButton(text) {
            private boolean hovered = false;
            private boolean pressed = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (isEnabled()) { hovered = true; repaint(); }
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        pressed = false;
                        repaint();
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (isEnabled()) { pressed = true; repaint(); }
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        pressed = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                Color fill;
                if (!isEnabled()) {
                    fill = new Color(203, 213, 225);
                } else if (pressed) {
                    fill = pressBg;
                } else if (hovered) {
                    fill = hoverBg;
                } else {
                    fill = bg;
                }

                if (hovered && isEnabled() && !pressed) {
                    g2.setColor(new Color(15, 23, 42, 28));
                    g2.fill(new RoundRectangle2D.Float(1, 2, getWidth() - 2, getHeight() - 1, radius, radius));
                }

                g2.setColor(fill);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight() - 1, radius, radius));
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 18, 10, 18));
        btn.setMargin(new Insets(8, 16, 8, 16));
        return btn;
    }

    public static void polishButton(JButton btn, Color bg, Color hoverBg, Color fg) {
        Color press = darker(bg, 0.85f);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 18, 10, 18));

        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            private boolean hovered = false;
            private boolean pressed = false;

            @Override
            public void installUI(JComponent c) {
                super.installUI(c);
                c.addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { if (c.isEnabled()) { hovered = true; c.repaint(); } }
                    @Override public void mouseExited(MouseEvent e) { hovered = false; pressed = false; c.repaint(); }
                    @Override public void mousePressed(MouseEvent e) { if (c.isEnabled()) { pressed = true; c.repaint(); } }
                    @Override public void mouseReleased(MouseEvent e) { pressed = false; c.repaint(); }
                });
            }

            @Override
            public void paint(Graphics g, JComponent c) {
                AbstractButton b = (AbstractButton) c;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color fill;
                if (!b.isEnabled()) fill = new Color(203, 213, 225);
                else if (pressed) fill = press;
                else if (hovered) fill = hoverBg;
                else fill = bg;

                int r = 12;
                if (hovered && b.isEnabled() && !pressed) {
                    g2.setColor(new Color(15, 23, 42, 28));
                    g2.fill(new RoundRectangle2D.Float(1, 2, c.getWidth() - 2, c.getHeight() - 1, r, r));
                }
                g2.setColor(fill);
                g2.fill(new RoundRectangle2D.Float(0, 0, c.getWidth(), c.getHeight() - 1, r, r));
                g2.dispose();
                super.paint(g, c);
            }
        });
    }

    public static void polishPrimary(JButton btn) { polishButton(btn, COLOR_NAVY, COLOR_NAVY_HOVER, Color.WHITE); }
    public static void polishGold(JButton btn) { polishButton(btn, COLOR_GOLD, COLOR_GOLD_HOVER, Color.WHITE); }
    public static void polishDanger(JButton btn) { polishButton(btn, COLOR_DANGER, COLOR_DANGER_HOVER, Color.WHITE); }
    public static void polishSoft(JButton btn) { polishButton(btn, COLOR_SOFT, COLOR_SOFT_HOVER, COLOR_TEXT_DARK); }
    public static void polishSuccess(JButton btn) { polishButton(btn, COLOR_SUCCESS, COLOR_SUCCESS_HOVER, Color.WHITE); }

    public static JPanel createRoundedCard(int radius, int padV, int padH) {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground() != null ? getBackground() : Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, radius, radius));
                g2.setColor(COLOR_BORDER);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, radius, radius));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(padV, padH, padV, padH));
        return p;
    }

    public static AbstractBorder createRoundedFieldBorder(Color borderColor, int radius) {
        return new AbstractBorder() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(borderColor);
                g2.draw(new RoundRectangle2D.Float(x, y, width - 1, height - 1, radius, radius));
                g2.dispose();
            }

            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(6, 8, 6, 8);
            }
        };
    }

    public static void styleField(JComponent field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(createRoundedFieldBorder(COLOR_BORDER, 10));
        field.setBackground(Color.WHITE);
        if (field instanceof JTextField) {
            ((JTextField) field).setCaretColor(COLOR_NAVY);
        }
    }

    private static Color darker(Color c, float factor) {
        return new Color(
                Math.max(0, (int) (c.getRed() * factor)),
                Math.max(0, (int) (c.getGreen() * factor)),
                Math.max(0, (int) (c.getBlue() * factor))
        );
    }
}