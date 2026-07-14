package io.github.bananapuncher714.cartographer.module.worldguard;

import java.util.regex.Pattern;

public class ColorRule {
    
    final boolean usePattern;
    
    final String regionName;
    final Pattern pattern;
    
    final RegionColors color;
    
    public ColorRule( String regionName, RegionColors color ) {
        this.usePattern = false;
        this.regionName = regionName;
        this.pattern = null;
        this.color = color;
    }
    
    public ColorRule( Pattern pattern, RegionColors color ) {
        this.usePattern = true;
        this.regionName = null;
        this.pattern = pattern;
        this.color = color;
    }
    
    public RegionColors getColor() {
        return color;
    }

    public boolean isMatch( String targetRegionName ) {
        if ( usePattern ) {
            if ( pattern.matcher( targetRegionName ).matches() ) {
                return true;
            }
        } else {
            if ( regionName.equalsIgnoreCase( targetRegionName ) ) {
                return true;
            }
        }
        
        return false;
    }
}
