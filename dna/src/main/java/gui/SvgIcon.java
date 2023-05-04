package gui;

import java.awt.image.BaseMultiResolutionImage;
import java.awt.image.BufferedImage;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import dna.Dna;
import logger.LogEvent;
import logger.Logger;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;

import javax.swing.*;

public class SvgIcon {
    private BaseMultiResolutionImage b;

    public SvgIcon(String svgPath, int baselineSize) {
        this.b = this.createMultiImage(svgPath, baselineSize);
    }

    public ImageIcon getImageIcon() {
        return new ImageIcon(this.b);
    }

    private BaseMultiResolutionImage createMultiImage(String svgPath, int baselineSize) {
        double[] zoomFactors = {1.0, 1.25, 1.5, 1.75, 2.0, 2.25, 2.5, 2.75, 3.0};
        List<BufferedImage> images = new ArrayList<>();
        for (double zoomFactor : zoomFactors) {
            int size = (int) (baselineSize * zoomFactor);
            BufferedImage image = transcodeSvgToImage(svgPath, size, size);
            images.add(image);
        }
        return new BaseMultiResolutionImage(images.stream().toArray(BufferedImage[]::new));
    }

    private BufferedImage transcodeSvgToImage(String svgPath, int width, int height) {
        String inputString = null;
        try {
            inputString = getClass().getResource(svgPath).toURI().toString();
        } catch (URISyntaxException e) {
            LogEvent l = new LogEvent(Logger.ERROR,
                    "Failed to convert SVG resource to URI.",
                    "Tried to retrieve the SVG file " + svgPath + " from the JAR container, but failed to convert it into a URI string.",
                    e);
            Dna.logger.log(l);
        }
        BufferedImageTranscoder transcoder = new BufferedImageTranscoder();
        transcoder.addTranscodingHint(ImageTranscoder.KEY_WIDTH, (float) width);
        transcoder.addTranscodingHint(ImageTranscoder.KEY_HEIGHT, (float) height);
        TranscoderInput input = new TranscoderInput(inputString);
        try {
            transcoder.transcode(input, null);
        } catch (TranscoderException e) {
            LogEvent l = new LogEvent(Logger.ERROR,
                    "Failed to transcode SVG image.",
                    "Tried to transcode SVG file " + svgPath + " into a rasterized image for use as an icon.",
                    e);
            Dna.logger.log(l);
        }
        return transcoder.getImage();
    }

    private static class BufferedImageTranscoder extends ImageTranscoder {
        private BufferedImage image = null;

        public BufferedImage getImage() {
            return image;
        }

        @Override
        public BufferedImage createImage(int w, int h) {
            BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            return bi;
        }

        @Override
        public void writeImage(BufferedImage img, TranscoderOutput output) {
            image = img;
        }
    }
}