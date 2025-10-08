package com.aipyq.friendapp.ai.providers;

import com.aipyq.friendapp.ai.ImageRenderProvider;
import com.aipyq.friendapp.api.dto.RenderRequest;
import com.aipyq.friendapp.api.dto.RenderResult;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Collections;

public class MockImageRenderProvider implements ImageRenderProvider {
    @Override
    public RenderResult render(RenderRequest req) {
        int[] wh = parseResolution(req.getResolution());
        int width = wh[0];
        int height = wh[1];

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);
            g.setColor(Color.BLACK);
            int fontSize = req.getFontSize() != null ? req.getFontSize().intValue() : 36;
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));
            int margin = req.getMargin() != null ? req.getMargin().intValue() : 24;
            drawMultiline(g, req.getCopyText() == null ? "" : req.getCopyText(), margin, margin + fontSize, width - margin * 2);
        } finally {
            g.dispose();
        }
        String dataUrl = toDataUrl(image);
        RenderResult r = new RenderResult();
        r.setImages(Collections.singletonList(dataUrl));
        return r;
    }

    private static void drawMultiline(Graphics2D g, String text, int x, int y, int maxWidth) {
        if (text == null) return;
        FontMetrics fm = g.getFontMetrics();
        StringBuilder line = new StringBuilder();
        int yy = y;
        for (String token : text.split("")) {
            String candidate = line.toString() + token;
            if (fm.stringWidth(candidate) > maxWidth) {
                g.drawString(line.toString(), x, yy);
                yy += fm.getHeight();
                line = new StringBuilder(token);
            } else {
                line.append(token);
            }
        }
        if (line.length() > 0) {
            g.drawString(line.toString(), x, yy);
        }
    }

    private static int[] parseResolution(String res) {
        try {
            if (res != null && res.contains("x")) {
                String[] parts = res.split("x");
                return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
            }
        } catch (Exception ignored) {}
        return new int[]{1080, 1080};
    }

    private static String toDataUrl(BufferedImage img) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            String b64 = Base64.getEncoder().encodeToString(baos.toByteArray());
            return "data:image/png;base64," + b64;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

