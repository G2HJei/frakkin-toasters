package xyz.zlatanov.frakkintoasters.backend.wire;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import xyz.zlatanov.frakkintoasters.backend.BackendTest;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Serves the build in src/test/resources/static.
 */
class StaticResourcesTest extends BackendTest {

    @Autowired
    MockMvc mvc;

    @Test
    void anAssetIsCachedForAYear() throws Exception {
        mvc.perform(get("/assets/index-Ab3dE6fG.js"))
                .andExpect(status().isOk())
                .andExpect(header().string(CACHE_CONTROL, "max-age=31536000, public, immutable"));
    }

    @Test
    void aMissingAssetIsNotCached() throws Exception {
        mvc.perform(get("/assets/index-00000000.js"))
                .andExpect(status().isNotFound())
                .andExpect(header().string(CACHE_CONTROL, not(containsString("immutable"))));
    }

    @Test
    void theBrowserAsksForIndexHtmlEveryTimeByItsContent() throws Exception {
        val etag = mvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(header().string(CACHE_CONTROL, "no-cache"))
                .andExpect(header().doesNotExist(LAST_MODIFIED))
                .andReturn().getResponse().getHeader(ETAG);

        mvc.perform(get("/index.html").header(IF_NONE_MATCH, etag))
                .andExpect(status().isNotModified())
                .andExpect(header().string(CACHE_CONTROL, "no-cache"));
        mvc.perform(get("/index.html").header(IF_NONE_MATCH, "\"an-earlier-build\""))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/assets/index-Ab3dE6fG.js")));
    }
}
