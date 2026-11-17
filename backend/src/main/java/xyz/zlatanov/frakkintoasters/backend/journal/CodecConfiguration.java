package xyz.zlatanov.frakkintoasters.backend.journal;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class CodecConfiguration {

    @Bean
    JournalCodec journalCodec() {
        return new JournalCodec();
    }

    /**
     * Writes what clients get: the projections, with the events and facts they hold.
     */
    @Bean
    WireCodec wireCodec() {
        return new WireCodec();
    }
}
