package com.netcommerce.kernel.core.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

/** Slugs match the .NET SlugGenerator's, captured by running it on the same input. */
class SlugGeneratorTest {

    @Test
    void accentedLettersAndSymbolsAreDroppedAsInDotNet() {
        assertThat(SlugGenerator.generate("Café Crème 50% OFF!")).isEqualTo("caf-crme-50-off");
    }

    @Test
    void ampersandsQuotesAndUnderscoresAreHandledAsInDotNet() {
        assertThat(SlugGenerator.generate("  Rock & Roll's \"Best\"__Hits  ")).isEqualTo("rock-and-rolls-best-hits");
    }

    @Test
    void aTakenSlugGetsTheNextFreeNumber() {
        assertThat(SlugGenerator.generateUnique("Blue Shirt", List.of("blue-shirt", "blue-shirt-1"))).isEqualTo("blue-shirt-2");
    }
}
