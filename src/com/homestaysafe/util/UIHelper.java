package com.homestaysafe.util;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

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

    public static void polishPrimary(JButton btn) {
        polishButton(btn, COLOR_NAVY, COLOR_NAVY_HOVER, Color.WHITE);
    }

    public static void polishGold(JButton btn) {
        polishButton(btn, COLOR_GOLD, COLOR_GOLD_HOVER, Color.WHITE);
    }

    public static void polishDanger(JButton btn) {
        polishButton(btn, COLOR_DANGER, COLOR_DANGER_HOVER, Color.WHITE);
    }

    public static void polishSoft(JButton btn) {
        polishButton(btn, COLOR_SOFT, COLOR_SOFT_HOVER, COLOR_TEXT_DARK);
    }

    public static void polishSuccess(JButton btn) {
        polishButton(btn, COLOR_SUCCESS, COLOR_SUCCESS_HOVER, Color.WHITE);
    }

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