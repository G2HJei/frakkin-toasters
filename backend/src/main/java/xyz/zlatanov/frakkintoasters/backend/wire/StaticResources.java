package xyz.zlatanov.frakkintoasters.backend.wire;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.util.DigestUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Arrays;

/**
 * Serves the built frontend from the static locations in place of Spring Boot's own mapping
 * ({@code spring.web.resources.add-mappings: false}), with the caching each kind of file allows.
 */
@Configuration
@RequiredArgsConstructor
public class StaticResources implements WebMvcConfigurer {

    private final WebProperties web;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        val locations = web.getResources().getStaticLocations();
        // Vite names every file under assets/ by a hash of its content: a changed file is a new URL.
        registry.addResourceHandler("/assets/**")
                .addResourceLocations(Arrays.stream(locations).map(location -> location + "assets/").toArray(String[]::new))
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable());
        // index.html names the assets of the build that serves it, so the browser asks every time, by the content: a
        // file in the jar has the jar's modification time, which a reproducible image (Buildpacks) fixes to 1980.
        registry.addResourceHandler("/**")
                .addResourceLocations(locations)
                .setCacheControl(CacheControl.noCache())
                .setUseLastModified(false)
                .setEtagGenerator(StaticResources::contentHash);
    }

    private static String contentHash(Resource resource) {
        try (val content = resource.getInputStream()) {
            return DigestUtils.md5DigestAsHex(content);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
