package xyz.zlatanov.frakkintoasters.backend.journal;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class CodecConfiguration {

    @Bean
    JournalCodec journalCodec() {
        return new JournalCodec();
    }

    @Bean
    WireCodec wireCodec() {
        return new WireCodec();
    }
}
