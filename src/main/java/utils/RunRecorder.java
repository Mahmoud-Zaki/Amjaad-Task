package utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.events.WebDriverListener;
import java.lang.reflect.Method;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Records a test run as an animated GIF by grabbing a frame after each browser interaction.
 *
 * <p>Implemented as a {@link WebDriverListener} rather than a background capture thread, because a
 * WebDriver session is not thread-safe: screenshotting from another thread while the test issues
 * commands interleaves badly on the same session. Hooking the listener keeps every capture on the
 * test's own thread.</p>
 *
 * <p>Enabled with {@code record.run=true}; off by default, since capturing a frame per interaction
 * roughly doubles a run's wall time.</p>
 */
public class RunRecorder implements WebDriverListener {

    private static final long MIN_FRAME_GAP_MS = 250;

    /**
     * One recording for the whole suite.
     *
     * <p>Shared statically because TestNG gives every test method its own driver, and therefore
     * its own listener, while the deliverable is a single continuous video of all eight flows.</p>
     */
    private static final GifRecorder SUITE_RECORDING = new GifRecorder();

    private final WebDriver rawDriver;
    private long lastFrameAt;
    private boolean capturing;

    public RunRecorder(WebDriver rawDriver) {
        this.rawDriver = rawDriver;
    }

    public static boolean isEnabled() {
        return ConfigReader.getBoolean("record.run");
    }

    /** Writes the whole suite's recording. Returns the file, or null when nothing was captured. */
    public static Path saveSuiteRecording(Path target) {
        try {
            return SUITE_RECORDING.write(target) ? target : null;
        } catch (IOException couldNotWrite) {
            System.out.println("Could not write recording: " + couldNotWrite.getMessage());
            return null;
        }
    }

    public static int suiteFrameCount() {
        return SUITE_RECORDING.frameCount();
    }

    /** Captures a frame, throttled, and never lets a capture failure break the test. */
    public void capture() {
        long now = System.currentTimeMillis();
        if (capturing || now - lastFrameAt < MIN_FRAME_GAP_MS) {
            return;
        }
        capturing = true;
        try {
            SUITE_RECORDING.addFrame(((TakesScreenshot) rawDriver).getScreenshotAs(OutputType.BYTES));
            lastFrameAt = now;
        } catch (RuntimeException captureFailed) {
            // A browser mid-navigation cannot be screenshotted; skip the frame.
        } finally {
            capturing = false;
        }
    }

    /**
     * One hook for every command, rather than a per-command list.
     *
     * <p>Throttling in {@link #capture()} is what keeps this affordable: the polling that explicit
     * waits do would otherwise produce hundreds of near-identical frames per second.</p>
     */
    @Override
    public void afterAnyCall(Object target, Method method, Object[] args, Object result) {
        capture();
    }
}
