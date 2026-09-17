package utils;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Collects browser frames and writes them out as a single looping animated GIF.
 *
 * <p>Used to record a test run without an external screen recorder. Frames are downscaled and
 * capped so a long run still produces a file worth opening.</p>
 */
public class GifRecorder {

    private static final int MAX_WIDTH = 900;
    /** Enough for a whole suite: a full run of the eight flows lands around 450 frames. */
    private static final int MAX_FRAMES = 1500;
    private static final int FRAME_DELAY_MS = 500;

    private final List<BufferedImage> frames = new ArrayList<>();

    /** Adds a frame, downscaled, ignoring anything beyond {@link #MAX_FRAMES}. */
    public synchronized void addFrame(byte[] pngBytes) {
        if (frames.size() >= MAX_FRAMES) {
            return;
        }
        try {
            BufferedImage source = ImageIO.read(new java.io.ByteArrayInputStream(pngBytes));
            if (source != null) {
                frames.add(downscale(source));
            }
        } catch (IOException unreadableFrame) {
            // A dropped frame is not worth failing a test over.
        }
    }

    public synchronized int frameCount() {
        return frames.size();
    }

    private BufferedImage downscale(BufferedImage source) {
        if (source.getWidth() <= MAX_WIDTH) {
            return toRgb(source);
        }
        int width = MAX_WIDTH;
        int height = Math.max(1, source.getHeight() * MAX_WIDTH / source.getWidth());
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = scaled.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(source, 0, 0, width, height, null);
        graphics.dispose();
        return scaled;
    }

    private BufferedImage toRgb(BufferedImage source) {
        if (source.getType() == BufferedImage.TYPE_INT_RGB) {
            return source;
        }
        BufferedImage converted = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = converted.createGraphics();
        graphics.drawImage(source, 0, 0, null);
        graphics.dispose();
        return converted;
    }

    /**
     * Writes the collected frames to {@code target}.
     *
     * <p>Frames after the first are forced to the first frame's dimensions, because the GIF format
     * has one logical screen size and the browser can be resized mid-run.</p>
     *
     * @return true when a file was written.
     */
    public synchronized boolean write(Path target) throws IOException {
        if (frames.isEmpty()) {
            return false;
        }
        Files.createDirectories(target.getParent());
        BufferedImage first = frames.get(0);

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("gif");
        if (!writers.hasNext()) {
            return false;
        }
        ImageWriter writer = writers.next();
        try (ImageOutputStream output = new FileImageOutputStream(target.toFile())) {
            writer.setOutput(output);
            writer.prepareWriteSequence(null);
            for (int index = 0; index < frames.size(); index++) {
                BufferedImage frame = fitTo(frames.get(index), first.getWidth(), first.getHeight());
                writer.writeToSequence(new IIOImage(frame, null, metadataFor(writer, frame, index == 0)), null);
            }
            writer.endWriteSequence();
        } finally {
            writer.dispose();
        }
        return true;
    }

    private BufferedImage fitTo(BufferedImage frame, int width, int height) {
        if (frame.getWidth() == width && frame.getHeight() == height) {
            return frame;
        }
        BufferedImage fitted = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = fitted.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(frame, 0, 0, width, height, null);
        graphics.dispose();
        return fitted;
    }

    private IIOMetadata metadataFor(ImageWriter writer, BufferedImage frame, boolean firstFrame) throws IOException {
        ImageWriteParam params = writer.getDefaultWriteParam();
        IIOMetadata metadata = writer.getDefaultImageMetadata(
                new javax.imageio.ImageTypeSpecifier(frame), params);
        String format = metadata.getNativeMetadataFormatName();
        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(format);

        IIOMetadataNode graphicsControl = child(root, "GraphicControlExtension");
        graphicsControl.setAttribute("disposalMethod", "none");
        graphicsControl.setAttribute("userInputFlag", "FALSE");
        graphicsControl.setAttribute("transparentColorFlag", "FALSE");
        // GIF delays are in hundredths of a second.
        graphicsControl.setAttribute("delayTime", String.valueOf(FRAME_DELAY_MS / 10));
        graphicsControl.setAttribute("transparentColorIndex", "0");

        if (firstFrame) {
            // The Netscape application extension is what makes the animation loop forever.
            IIOMetadataNode applicationExtensions = child(root, "ApplicationExtensions");
            IIOMetadataNode netscapeExtension = new IIOMetadataNode("ApplicationExtension");
            netscapeExtension.setAttribute("applicationID", "NETSCAPE");
            netscapeExtension.setAttribute("authenticationCode", "2.0");
            netscapeExtension.setUserObject(new byte[]{0x1, 0x0, 0x0});
            applicationExtensions.appendChild(netscapeExtension);
        }

        metadata.setFromTree(format, root);
        return metadata;
    }

    private IIOMetadataNode child(IIOMetadataNode root, String name) {
        for (int index = 0; index < root.getLength(); index++) {
            if (root.item(index).getNodeName().equalsIgnoreCase(name)) {
                return (IIOMetadataNode) root.item(index);
            }
        }
        IIOMetadataNode created = new IIOMetadataNode(name);
        root.appendChild(created);
        return created;
    }
}
