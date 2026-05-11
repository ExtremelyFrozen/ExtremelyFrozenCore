package com.extfro.extfrocore.api.material;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(fluent = true, chain = false)
public class EFElement {

    @Getter
    @Setter
    private long protons;
    @Getter
    @Setter
    private long neutrons;
    @Getter
    @Setter
    private long halfLifeSeconds;
    @Getter
    @Setter
    private String decayTo;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private String symbol;
    @Getter
    @Setter
    private boolean isIsotope;

    public EFElement(long protons, long neutrons, long halfLifeSeconds, String decayTo, String name, String symbol,
                     boolean isIsotope) {
        this.protons = protons;
        this.neutrons = neutrons;
        this.halfLifeSeconds = halfLifeSeconds;
        this.decayTo = decayTo;
        this.name = name;
        this.symbol = symbol;
        this.isIsotope = isIsotope;
    }

    public long mass() {
        return protons + neutrons;
    }
}
