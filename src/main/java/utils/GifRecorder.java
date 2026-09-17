package utils;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Collects browser frames and writes them out as one looping animated GIF.
 *
 * <p>Frames are held as the original PNG bytes and decoded one at a time at write time. Keeping
 * them decoded would cost ~1.8 MB each ({@value #MAX_WIDTH}px wide, 32-bit) — gigabytes across a
 * suite — where the compressed bytes are roughly a tenth of that.</p>
 */
public class GifRecorder {

    private static final int MAX_WIDTH = 900;
    /** Enough for a full run of the eight flows at {@link #FRAME_DELAY_MS}. */
    private static final int MAX_FRAMES = 1000;
    private static final int FRAME_DELAY_MS = 500;

    private final List<byte[]> frames = new ArrayList<>();

    /** Adds a frame, ignoring anything beyond {@link #MAX_FRAMES}. */
    public synchronized void addFrame(byte[] pngBytes) {
        if (frames.size() < MAX_FRAMES) {
            frames.add(pngBytes);
        }
    }

    public synchronized int frameCount() {
        return frames.size();
    }

    /**
     * Writes the collected frames to {@code target}.
     *
     * <p>Every frame is normalised to the first frame's dimensions: a GIF has one logical screen
     * size, and the browser can be resized mid-run.</p>
     *
     * @return true when a file was written.
     */
    public synchronized boolean write(Path target) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("gif");
        if (frames.isEmpty() || !writers.hasNext()) {
            return false;
        }
        Files.createDirectories(target.getParent());

        ImageWriter writer = writers.next();
        int width = 0;
        int height = 0;
        boolean wroteAny = false;
        try (ImageOutputStream output = new FileImageOutputStream(target.toFile())) {
            writer.setOutput(output);
            writer.prepareWriteSequence(null);
            for (byte[] pngBytes : frames) {
                BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(pngBytes));
                if (decoded == null) {
                    continue;
                }
                if (!wroteAny) {
                    width = Math.min(decoded.getWidth(), MAX_WIDTH);
                    height = Math.max(1, decoded.getHeight() * width / decoded.getWidth());
                }
                BufferedImage frame = copyTo(decoded, width, height);
                writer.writeToSequence(new IIOImage(frame, null, metadataFor(writer, frame, !wroteAny)), null);
                wroteAny = true;
            }
            writer.endWriteSequence();
        } finally {
            writer.dispose();
        }
        return wroteAny;
    }

    /** Redraws an image at the given size as {@code TYPE_INT_RGB}, which is what GIF writing wants. */
    private BufferedImage copyTo(BufferedImage source, int width, int height) {
        BufferedImage copy = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = copy.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(source, 0, 0, width, height, null);
        graphics.dispose();
        return copy;
    }

    private IIOMetadata metadataFor(ImageWriter writer, BufferedImage frame, boolean firstFrame) throws IOException {
        ImageWriteParam params = writer.getDefaultWriteParam();
        IIOMetadata metadata = writer.getDefaultImageMetadata(new ImageTypeSpecifier(frame), params);
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
            IIOMetadataNode netscapeExtension = new IIOMetadataNode("ApplicationExtension");
            netscapeExtension.setAttribute("applicationID", "NETSCAPE");
            netscapeExtension.setAttribute("authenticationCode", "2.0");
            netscapeExtension.setUserObject(new byte[]{0x1, 0x0, 0x0});
            child(root, "ApplicationExtensions").appendChild(netscapeExtension);
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
