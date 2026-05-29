package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ModernButton extends JButton {
    private Color normalBg;
    private Color hoverBg;
    private Color activeBg;

    public ModernButton(String text, Color bg, Color fg) {
        super(text);
        this.normalBg = bg;
        // Derive hover/active colors automatically
        this.hoverBg = getHoverColor(bg);
        this.activeBg = bg.darker();
        
        setForeground(fg);
        setFont(new Font("Segoe UI", Font.BOLD, 12));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    setBackground(hoverBg);
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (isEnabled()) {
                    setBackground(normalBg);
                    repaint();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()) {
                    setBackground(activeBg);
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isEnabled()) {
                    if (getBounds().contains(e.getPoint())) {
                        setBackground(hoverBg);
                    } else {
                        setBackground(normalBg);
                    }
                    repaint();
                }
            }
        });
        setBackground(normalBg);
    }

    private Color getHoverColor(Color bg) {
        int r = Math.min(255, (int)(bg.getRed() * 1.15));
        int g = Math.min(255, (int)(bg.getGreen() * 1.15));
        int b = Math.min(255, (int)(bg.getBlue() * 1.15));
        return new Color(r, g, b);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (isEnabled()) {
            g2.setColor(getBackground());
        } else {
            g2.setColor(new Color(0x33, 0x41, 0x55)); // Slate Muted background on disabled
        }
        
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
        g2.dispose();
        
        super.paintComponent(g);
    }
}
