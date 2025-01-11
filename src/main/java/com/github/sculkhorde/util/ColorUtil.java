package com.github.sculkhorde.util;

import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class ColorUtil {

    public static String sculkBaseColor1 = "034150"; // Lightest
    public static String sculkBaseColor2 = "062E37";
    public static String sculkBaseColor3 = "04252D";
    public static String sculkBaseColor4 = "002A2A";
    public static String sculkBaseColor5 = "122225";
    public static String sculkBaseColor6 = "122225";
    public static String sculkLightColor1 = "29DFEB"; // Lightest
    public static String sculkLightColor2 = "0BB4AA";
    public static String sculkLightColor3 = "009295";
    public static String sculkLightColor4 = "1B6864";
    public static String sculkLightColor5 = "037286";
    public static String sculkLightColor6 = "0A5C70"; // Darkest
    public static String sculkBoneColor1 = "D1D6B6"; // Lightest
    public static String sculkBoneColor2 = "BBC39B";
    public static String sculkBoneColor3 = "A2AF86";
    public static String sculkBoneColor4 = "819988";
    public static String sculkBoneColor5 = "6E757B";
    public static String sculkBoneColor6 = "40576C"; // Darkest
    public static String sculkAcidColor1 = "84FF35"; // Lightest
    public static String sculkAcidColor2 = "5FF21C";
    public static String sculkAcidColor3 = "10D010";
    public static String sculkAcidColor4 = "0EAD20";
    public static String sculkAcidColor5 = "0A8C2E";
    public static String sculkAcidColor6 = "055430";// Darkest
    public static String purityLightColor1 = "f6f892"; // Lightest
    public static String purityLightColor2 = "eaee57";
    public static String purityLightColor3 = "eccb45";
    public static String purityLightColor4 = "dba213"; // Darkest
    public static String purityDarkColor1 = "b26411"; // Lightest
    public static String purityDarkColor2 = "752802";
    public static String purityDarkColor3 = "541209"; // Darkest


    public static String getRandomHexAcidColor(RandomSource rng)
    {
        int index = rng.nextInt(7);
        switch (index)
        {
            case 0:
                return sculkAcidColor1;
            case 1:
                return sculkAcidColor2;
            case 2:
                return sculkAcidColor3;
            case 3:
                return sculkAcidColor4;
            case 4:
                return sculkAcidColor5;
            case 5:
                return sculkAcidColor6;
            default:
                return sculkAcidColor1;
        }
    }

    public static String getRandomPurityColor(RandomSource rng)
    {
        int index = rng.nextInt(8);
        switch (index)
        {
            case 0:
                return purityLightColor1;
            case 1:
                return purityLightColor2;
            case 2:
                return purityLightColor3;
            case 3:
                return purityLightColor4;
            case 4:
                return purityDarkColor1;
            case 5:
                return purityDarkColor2;
            case 6:
                return purityDarkColor3;
            default:
                return purityLightColor1;
        }
    }

    public static int hexToRGB(String hex) {
        // Remove the hash at the beginning if it's there
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }

        // Parse the hex string to an integer
        int color = Integer.parseInt(hex, 16);

        return color;
    }

    public static Vector3f hexToVector3F(String hex)
    {
        return Vec3.fromRGB24(hexToRGB(hex)).toVector3f();
    }
}
