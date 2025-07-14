import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PriceHistoryPanel extends JPanel {
    private List<Double> history;

    public PriceHistoryPanel(List<Double> history) {
        this.history = history;
    }

    public void setHistory(List<Double> history) {
        this.history = history;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (history == null || history.size() < 2) return;
        Graphics2D g2 = (Graphics2D) g;
        int w = getWidth();
        int h = getHeight();
        int pad = 40;
        double max = history.stream().max(Double::compare).orElse(0.0);
        double min = history.stream().min(Double::compare).orElse(0.0);
        double range = max - min;
        if (range == 0) range = 1;
        int n = history.size();
        // Draw axes
        g2.drawLine(pad, h - pad, pad, pad);
        g2.drawLine(pad, h - pad, w - pad, h - pad);
        // Draw line series
        int prevX = pad;
        int prevY = h - pad;
        for (int i = 0; i < n; i++) {
            double v = history.get(i);
            int x = pad + (int) ((double) (w - 2 * pad) * i / (n - 1));
            int y = pad + (int) ((double) (h - 2 * pad) * (max - v) / range);
            if (i > 0) {
                g2.drawLine(prevX, prevY, x, y);
            }
            prevX = x;
            prevY = y;
        }
    }
}
