package net.silentchaos512.hpbar.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.IllegalFormatException;

public class Config {

  public static ForgeConfigSpec.ConfigValue<String> healthStringFormat;
  public static ForgeConfigSpec.ConfigValue<String> damageStringFormat;
  public static ForgeConfigSpec.BooleanValue showLastDamageTaken;
  public static ForgeConfigSpec.DoubleValue textScale;
  public static ForgeConfigSpec.DoubleValue xOffset;
  public static ForgeConfigSpec.DoubleValue yOffset;
  public static ForgeConfigSpec.IntValue barWidth;
  public static ForgeConfigSpec.IntValue barHeight;
  public static ForgeConfigSpec.DoubleValue barScale;
  public static ForgeConfigSpec.BooleanValue barShowAlways;
  public static ForgeConfigSpec.BooleanValue replaceVanillaHealth;
  public static ForgeConfigSpec.DoubleValue barOpacity;
  public static ForgeConfigSpec.DoubleValue barQuiverFraction;
  public static ForgeConfigSpec.DoubleValue barQuiverIntensity;
  public static ForgeConfigSpec.EnumValue<Justification> barJustification;
  public static ForgeConfigSpec.IntValue checkinFrequency;
  public static Color colorHealthBar = new Color(1f, 0f, 0f);

  private static ForgeConfigSpec c;

  private static final String sep = ".";
  public static final String CAT_BAR = "health_bar";
  public static final String CAT_BAR_POSITION = CAT_BAR + sep + "position";
  public static final String CAT_BAR_RENDER = CAT_BAR + sep + "render";
  public static final String CAT_BAR_SIZE = CAT_BAR + sep + "size";
  public static final String CAT_BAR_TEXT = CAT_BAR + sep + "text";
  public static final String CAT_NETWORK = "network";

  public static void init() {

    c = new ForgeConfigSpec.Builder().configure(b -> {load(b); return null;}).getRight();
  }

  public static void load(ForgeConfigSpec.Builder builder) {

    //@formatter:off
    builder.push(CAT_BAR_TEXT);

    healthStringFormat = loadFormatString(builder);
    damageStringFormat = builder.comment("Format string for last damage taken.").define(
        "DamageStringFormat",
        "(%.3f)", o -> {
                try {
                    if (!(o instanceof String)) {
                        return false;
                    }

                    String.format((String) o, 1.0f);
                    return true;
                } catch (IllegalFormatException e) {
                    return false;
                }
            });
    showLastDamageTaken = builder.comment("Shows the last amount of damage the player took.").define(
        "ShowLastDamageTaken",
        false);

    builder.pop(2).push(CAT_BAR_RENDER);

    textScale = builder.comment("The scale of the text displaying the player's health above the bar.", "Set to 0 to disable text.").defineInRange(
        "TextScale",
        0.8f, 0f, Float.MAX_VALUE
        );

    builder.pop(2).push(CAT_BAR_POSITION);

    xOffset = builder.comment("How far across the screen the health bar renders.").defineInRange(
        "XOffset",
        0.5f, 0f, 1f);
    yOffset = builder.comment("How far down the screen the health bar renders.").defineInRange(
        "YOffset",
        0.75f, 0f, 1f);

    builder.pop(2).push(CAT_BAR_SIZE);

    barWidth = builder.comment("The width of the health bar.").defineInRange(
        "Width",
        64, 0, Integer.MAX_VALUE);
    barHeight = builder.comment("The height of the health bar.").defineInRange(
        "Height",
        8, 0, Integer.MAX_VALUE);

    builder.pop(2).push(CAT_BAR_RENDER);

    barScale = builder.comment("The scale of the health bar.", "Set to 0 to disable the health bar.").defineInRange(
        "BarScale",
        1.0f, 0f, Float.MAX_VALUE);

    barShowAlways = builder.comment("Always show the health bar, even at full health.").define(
        "ShowAlways",
        false);
    barOpacity = builder.comment("The opacity of the health bar.").defineInRange(
        "Opacity",
        0.6f, 0f, 1f);
    replaceVanillaHealth = builder.comment("Hides vanilla hearts and places the bar in their place. Ignores some configs if true.").define(
        "ReplaceVanillaHealth",
        false);
    barJustification = builder.comment("Where the health bar is rendered in the frame. CENTER means there will be equal amounts of "
            + "empty space to the left and right. LEFT and RIGHT mean all empty space is to the right "
            + "or left, respectively.").defineEnum(
        "Justification",
        Justification.CENTER);

    barQuiverFraction = builder.comment("The fraction of health remaining when the bar begins to quiver/shake. Set to 0 to disable.").defineInRange(
        "QuiverFraction",
        0.25f, 0f, 1f);
    barQuiverIntensity = builder.comment("How much the bar shakes when low on health. Intensity also increases with lower health.").defineInRange(
        "QuiverIntensity",
        1.0f, 0f, Float.MAX_VALUE);

    builder.pop(2).push(CAT_NETWORK);

    checkinFrequency = builder.comment("Even if the player's health has not changed, an update packet will be sent"
            + " after this many ticks. Set to 0 to disable (not recommended, unless you're very"
            + " bandwidth conscious).").defineInRange(
        "CheckInFrequency",
        300, 0, Integer.MAX_VALUE);

  //@formatter:off
  }

  private static ForgeConfigSpec.ConfigValue<String> loadFormatString(ForgeConfigSpec.Builder builder) {

    return builder.comment("The format string the player's current and maximum health are passed through.",
            "To show only the integer part of your health, try '%.0f / %.0f'.",
            "You need not use two format codes. Using one will show your current health only.",
            "https://docs.oracle.com/javase/tutorial/java/data/numberformat.html").define("HealthStringFormat", "%.1f / %.1f", o -> {
        try {
            if (!(o instanceof String)) {
                return false;
            }

            String.format((String) o, 1.0f, 1.0f);
            return true;
        } catch (IllegalFormatException e) {
            return false;
        }
    });
  }

  public static void save() {

    c.save();
  }

  public static ForgeConfigSpec getConfiguration() {

    return c;
  }

  public enum Justification
  {
    CENTER, LEFT, RIGHT
  }
}
