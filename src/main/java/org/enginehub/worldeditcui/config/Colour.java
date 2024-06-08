/*
 * Copyright (c) 2011-2024 WorldEditCUI team and contributors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.enginehub.worldeditcui.config;

import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public record Colour(int argb) {
    private static final Pattern COLOUR_PATTERN = Pattern.compile("^#[0-9a-f]{6,8}$", Pattern.CASE_INSENSITIVE);
    private static final int DEFAULT_ALPHA = 0xCC;

    public static Colour parseRgba(final String hex) {
        int r = Integer.parseInt(hex.substring(1, 3), 16) & 0xff;
        int g = Integer.parseInt(hex.substring(3, 5), 16) & 0xff;
        int b = Integer.parseInt(hex.substring(5, 7), 16) & 0xff;
        int a = hex.length() < 9 ? DEFAULT_ALPHA : Integer.parseInt(hex.substring(7, 9), 16) & 0xff;
        return new Colour(a, r, g, b);
    }

    public static Colour parseRgbaOr(String colour, final Colour fallback) {
        return ((colour = Colour.sanitiseColour(colour, null)) == null) ? fallback : parseRgba(colour);
    }
    
    public static @Nullable Colour parseRgbaOrNull(final String colour) {
        return parseRgbaOr(colour, null);
    }

    public Colour {
        argb &= 0xff_ff_ff_ff;
    }

    public Colour(int r, int g, int b) {
        this(DEFAULT_ALPHA, r, g, b);
    }

    public Colour(int a, int r, int g, int b) {
        this(((a & 0xff) << 24) | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff));
    }

    public static Colour firstOrDefault(Colour colour, String defaultColour) {
        if (colour == null)
        {
            return parseRgba(defaultColour);
        }

        return colour;
    }

    /**
     * Validates a user-entered RGBA colour code. Ensures that style is not null, it
     * starts with #, that it has all 6 digits, and that each hex code is valid.
     *
     * @param colour
     * @param def
     * @return
     */
    private static String sanitiseColour(String colour, String def) {
        if (colour == null)
        {
            return def;
        }
        else if (!colour.startsWith("#"))
        {
            return def;
        }
        else if (colour.length() != 7 && colour.length() != 9)
        {
            return def;
        }

        return COLOUR_PATTERN.matcher(colour).matches() ? colour : def;
    }

    /**
     * Hex string in #RRGGBBAA format.
     *
     * @return the hex string
     */
    public String hexString() {
        return "#%08X".formatted((this.argb & 0xff_ff_ff) << 8 | this.alpha());
    }

    public int red() {
        return (this.argb >> 16) & 0xff;
    }

    public Colour red(final int red) {
        return new Colour((this.argb & ~(0xff << 16)) | ((red & 0xff) << 16));
    }

    public int green() {
        return (this.argb >> 8) & 0xff;
    }

    public Colour green(final int green) {
        return new Colour((this.argb & ~(0xff << 8)) | ((green & 0xff) << 8));
    }

    public int blue() {
        return this.argb & 0xff;
    }

    public Colour blue(final int blue) {
        return new Colour((this.argb & ~0xff) | (blue & 0xff));
    }

    public int alpha() {
        return (this.argb >> 24) & 0xff;
    }

    public Colour alpha(final int alpha) {
        return new Colour((this.argb & ~(0xff << 24)) | ((alpha & 0xff) << 24));
    }
}
