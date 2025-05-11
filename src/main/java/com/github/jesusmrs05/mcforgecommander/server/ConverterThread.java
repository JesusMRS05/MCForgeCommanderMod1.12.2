package com.github.jesusmrs05.mcforgecommander.server;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

public class ConverterThread extends Thread {
    private static final int TARGET_WIDTH = 426;
    private static final int TARGET_HEIGTH = 240;
    private static final float JPEG_QUALITY = 0.2F; //0.2F
    @Override
    public void run() {
        Server server = Server.getInstance();
        try {
            while (!isInterrupted()) {
                FrameData frameData = server.takeFrameData();
                BufferedImage image = convertToImage(frameData.buffer, frameData.width, frameData.height);
                BufferedImage scaledImage = scaleImage(image);

                byte[] imageBytes = encodeJpegWithQuality(scaledImage);


                server.enqueueImage(imageBytes);
            }
        } catch (InterruptedException e) {
            Logger.getLogger("MCForgeCommander").severe("Error capturing frame: " + e.getMessage());
        } catch (IOException ioe) {
            Logger.getLogger("MCForgeCommander").severe("Error capturing frame: " + ioe.getMessage());
        }
    }

    private BufferedImage convertToImage(ByteBuffer buffer, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int i = (x + (width * y)) * 4;
                int r = buffer.get(i) & 0xFF;
                int g = buffer.get(i + 1) & 0xFF;
                int b = buffer.get(i + 2) & 0xFF;
                image.setRGB(x, height - (y + 1), (r << 16) | (g << 8) | b);
            }
        }
        return image;
    }

    private BufferedImage scaleImage(BufferedImage original) {
        BufferedImage resized = new BufferedImage(TARGET_WIDTH, TARGET_HEIGTH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.drawImage(original, 0, 0, TARGET_WIDTH, TARGET_HEIGTH, null);
        g.dispose();
        return resized;
    }

    private byte[] encodeJpegWithQuality(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();

        jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        jpgWriteParam.setCompressionQuality(JPEG_QUALITY);

        ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
        jpgWriter.setOutput(ios);
        IIOImage outputImage = new IIOImage(image, null, null);
        jpgWriter.write(null, outputImage, jpgWriteParam);

        ios.close();
        jpgWriter.dispose();
        return baos.toByteArray();
    }

}
