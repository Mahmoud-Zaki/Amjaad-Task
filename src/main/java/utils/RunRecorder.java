package utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.events.WebDriverListener;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;

/**
 * Records a run as one animated GIF, grabbing a throttled frame after each browser command.
 *
 * <p>A {@link WebDriverListener} rather than a background capture thread: a WebDriver session is
 * not thread-safe, so screenshotting off-thread interleaves badly with the commands the test is
 * issuing. Hooking the listener keeps every capture on the test's own thread.</p>
 *
 * <p>Enabled with {@code record.run=true}; off by default, since capturing a frame per interaction
 * roughly doubles a run's wall time.</p>
 */
public class RunRecorder implements WebDriverListener {

    /** Matches the GIF's own frame delay, so playback is real time and no frame is captured twice. */
    private static final long MIN_FRAME_GAP_MS = 500;

    /**
     * One recording for the whole suite: TestNG gives every test method its own driver and
     * therefore its own listener, while the deliverable is a single continuous video.
     */
    private static final GifRecorder SUITE_RECORDING = new GifRecorder();

    private final WebDriver rawDriver;
    private long lastFrameAt;

    public RunRecorder(WebDriver rawDriver) {
        this.rawDriver = rawDriver;
    }

    public static boolean isEnabled() {
        return ConfigReader.getBoolean("record.run");
    }

    /** Writes the suite's recording. Returns the file, or null when nothing was captured. */
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

    /**
     * One hook for every command rather than a per-command list. The throttle is what keeps this
     * affordable: the polling that explicit waits do would otherwise produce hundreds of
     * near-identical frames per second.
     */
    @Override
    public void afterAnyCall(Object target, Method method, Object[] args, Object result) {
        long now = System.currentTimeMillis();
        if (now - lastFrameAt < MIN_FRAME_GAP_MS) {
            return;
        }
        lastFrameAt = now;
        try {
            SUITE_RECORDING.addFrame(((TakesScreenshot) rawDriver).getScreenshotAs(OutputType.BYTES));
        } catch (RuntimeException captureFailed) {
            // A browser mid-navigation cannot be screenshotted; skip the frame.
        }
    }
}
