package xyz.zlatanov.frakkintoasters.backend;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PackagedJarTest {

    @Test
    void theRegistryFindsTheProcessorsInThePackagedApplication() throws Exception {
        // In the Spring Boot jar core is a nested jar, with a URL scheme of its own.
        val jar = Path.of(System.getProperty("bootJar"));
        val java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        val process = new ProcessBuilder(java,
                "-cp", jar.toString(),
                "-Dloader.main=" + ProcessorRegistryCheck.class.getName(),
                "org.springframework.boot.loader.launch.PropertiesLauncher")
                .redirectErrorStream(true)
                .start();
        val finished = process.waitFor(60, TimeUnit.SECONDS);
        val output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        assertTrue(finished, output);
        assertEquals(0, process.exitValue(), output);
        assertTrue(output.contains("status Waiting"), output);
    }
}
