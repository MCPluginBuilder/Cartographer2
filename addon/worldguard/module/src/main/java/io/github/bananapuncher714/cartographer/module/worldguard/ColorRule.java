package io.github.bananapuncher714.cartographer.module.worldguard;

import java.util.regex.Pattern;

public class ColorRule {
    
    private final Pattern pattern;
    private final RegionColors color;
    
    public ColorRule( Pattern pattern, RegionColors color ) {
        this.pattern = pattern;
        this.color = color;
    }

    public RegionColors getColor() {
        return color;
    }

    public boolean isMatch( String targetRegionName ) {
        // Note: "^" and "$" is not necessary in the regex pattern, because of the ".matcher()" check
        return pattern.matcher( targetRegionName ).matches();
    }
}
