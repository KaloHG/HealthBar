package net.silentchaos512.hpbar.config;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.Map;
import java.util.function.Predicate;

public class Config {

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final Logger LOGGER = LogManager.getLogger("HealthBar");

  private static File configFile;
  private static JsonObject baseConfig;
  public static ConfigSection rootSection;

  public static ConfigValue<String> healthStringFormat;
  public static ConfigValue<String> damageStringFormat;
  public static BooleanValue showLastDamageTaken;
  public static RangeDoubleValue textScale;
  public static RangeDoubleValue xOffset;
  public static RangeDoubleValue yOffset;
  public static RangeIntValue barWidth;
  public static RangeIntValue barHeight;
  public static RangeDoubleValue barScale;
  public static BooleanValue barShowAlways;
  public static BooleanValue replaceVanillaHealth;
  public static RangeDoubleValue barOpacity;
  public static RangeDoubleValue barQuiverFraction;
  public static RangeDoubleValue barQuiverIntensity;
  public static EnumValue<Justification> barJustification;
  public static Color colorHealthBar = new Color(1f, 0f, 0f);

  public static final String CAT_BAR_POSITION = "position";
  public static final String CAT_BAR_RENDER = "render";
  public static final String CAT_BAR_SIZE = "size";
  public static final String CAT_BAR_TEXT = "text";

  public static void init(File file) {

      configFile = file;

      if (file.getParentFile() != null)
      {
          file.getParentFile().mkdirs();
      }

      if (!file.exists())
      {
          try
          {
              if (!file.createNewFile())
              {
                  LOGGER.error("Unable to create config file");
              }
          }
          catch (IOException e)
          {
              LOGGER.error("Error creating config file", e);
          }
      }

      try (BufferedReader reader = Files.newBufferedReader(file.toPath()))
      {
          baseConfig = GSON.fromJson(reader, JsonObject.class);
      }
      catch (IOException e)
      {
          LOGGER.error("Error reading config", e);
      }

      if (baseConfig == null)
      {
          baseConfig = new JsonObject();
      }

      rootSection = new ConfigSection(baseConfig, null, null);

      try {
          load();
      } catch (Exception e) {
          LOGGER.error("Error reading config", e);
          baseConfig = new JsonObject();
          load();
      }

  }

  public static void load() {

    //@formatter:off
    ConfigSection text = getSection(rootSection, CAT_BAR_TEXT);

    healthStringFormat = loadFormatString(text);
    damageStringFormat = getString(text, "DamageStringFormat", "(%.3f)", s -> {
        try {
            String.format(s, 1.0f);
            return true;
        } catch (IllegalFormatException e) {
            return false;
        }
    }, "Format string for last damage taken.");
    showLastDamageTaken = getBoolean(text, "ShowLastDamageTaken", false, "Shows the last amount of damage the player took.");

    ConfigSection render = getSection(rootSection, CAT_BAR_RENDER);

    textScale = getRangeDouble(render, "TextScale", 0.8, 0.0, Double.MAX_VALUE, "The scale of the text displaying the player's health above the bar. " + "Set to 0 to disable text.");

    ConfigSection position = getSection(rootSection, CAT_BAR_POSITION);

    xOffset = getRangeDouble(position, "XOffset", 0.5f, 0f, 1f, "How far across the screen the health bar renders.");
    yOffset = getRangeDouble(position, "YOffset",
            0.75f, 0.0, 1.0, "How far down the screen the health bar renders.");

    ConfigSection size = getSection(rootSection, CAT_BAR_SIZE);

    barWidth = getRangeInt(size, "Width", 64, 0, Integer.MAX_VALUE, "The width of the health bar.");
    barHeight = getRangeInt(size, "Height", 8, 0, Integer.MAX_VALUE, "The height of the health bar.");

    barScale = getRangeDouble(render, "BarScale", 1.0, 0.0, Double.MAX_VALUE, "The scale of the health bar. " + "Set to 0 to disable the health bar.");

    barShowAlways = getBoolean(render, "ShowAlways", false, "Always show the health bar, even at full health.");
    barOpacity = getRangeDouble(render, "Opacity", 0.6, 0.0, 1.0, "The opacity of the health bar.");
    replaceVanillaHealth = getBoolean(render, "ReplaceVanillaHealth", false, "Hides vanilla hearts and places the bar in their place. Ignores some configs if true.");
    barJustification = getEnum(render, "Justification", Justification.CENTER, Justification.class, "Where the health bar is rendered in the frame. CENTER means there will be equal amounts of "
            + "empty space to the left and right. LEFT and RIGHT mean all empty space is to the right "
            + "or left, respectively.");

    barQuiverFraction = getRangeDouble(render, "QuiverFraction", 0.25, 0.0, 1.0, "The fraction of health remaining when the bar begins to quiver/shake. Set to 0 to disable.");
    barQuiverIntensity = getRangeDouble(render, "QuiverIntensity", 1.0, 0.0, Double.MAX_VALUE, "How much the bar shakes when low on health. Intensity also increases with lower health.");
  //@formatter:off

    save();
  }

  private static ConfigValue<String> loadFormatString(ConfigSection section) {

    return getString(section, "HealthStringFormat", "%.1f / %.1f", s -> {
                try {
                    String.format(s, 1.0f, 1.0f);
                    return true;
                } catch (IllegalFormatException e) {
                    return false;
                }
            },
            "To show only the integer part of your health, try '%.0f / %.0f'. " +
            "You need not use two format codes. Using one will show your current health only. " +
            "https://docs.oracle.com/javase/tutorial/java/data/numberformat.html");
  }

  public static void save() {

      try (BufferedWriter writer = Files.newBufferedWriter(configFile.toPath()))
      {
          GSON.toJson(baseConfig, writer);
      }
      catch (IOException e)
      {
          LOGGER.error("Error saving config", e);
      }
  }

    public interface ConfigSpecObject {}

    public static class ConfigSection implements ConfigSpecObject
    {
        private JsonObject section;
        private ConfigSection parent;
        private String name;
        private Map<String, ConfigSpecObject> spec = new HashMap<>();

        public ConfigSection(JsonObject section, ConfigSection parent, String name)
        {
            this.section = section;
            this.parent = parent;
            this.name = name;
        }

        public JsonObject getSection()
        {
            return this.section;
        }

        public ConfigSection getParent()
        {
            return this.parent;
        }

        public String getName()
        {
            return this.name;
        }

        public Map<String, ConfigSpecObject> getSpec()
        {
            return this.spec;
        }
    }

    public static abstract class ConfigValue<T> implements ConfigSpecObject
    {
        protected ConfigSection section;
        protected String name;
        protected T defaultValue;
        protected String description;

        public ConfigValue(ConfigSection section, String name, T defaultValue, String description)
        {
            this.section = section;
            this.name = name;
            this.defaultValue = defaultValue;
            this.description = description;

            section.getSpec().put(name, this);
        }

        public ConfigSection getSection()
        {
            return this.section;
        }

        public String getName()
        {
            return this.name;
        }

        public T getDefaultValue()
        {
            return this.defaultValue;
        }

        public String getDescription()
        {
            return this.description;
        }

        public abstract T get();

        public abstract void set(T object);
    }

    public static class BooleanValue extends ConfigValue<Boolean>
    {
        public BooleanValue(ConfigSection section, String name, Boolean defaultValue, String description)
        {
            super(section, name, defaultValue, description);

            get();
        }

        @Override
        public Boolean get()
        {
            JsonElement obj = this.section.getSection().get(this.name);

            if (obj instanceof JsonPrimitive && ((JsonPrimitive) obj).isBoolean())
            {
                return obj.getAsBoolean();
            }
            else
            {
                set(this.defaultValue);
                return this.defaultValue;
            }
        }

        @Override
        public void set(Boolean object)
        {
            this.section.getSection().addProperty(this.name, object);
        }
    }

    public static class RangeIntValue extends ConfigValue<Integer>
    {
        private int min;
        private int max;

        public RangeIntValue(ConfigSection section, String name, Integer defaultValue, int min, int max, String description)
        {
            super(section, name, defaultValue, description);

            Preconditions.checkArgument(min <= max, "min must be less than or equal to max");

            this.min = min;
            this.max = max;

            get();
        }

        public int getMin()
        {
            return this.min;
        }

        public int getMax()
        {
            return this.max;
        }

        @Override
        public Integer get()
        {
            JsonElement obj = this.section.getSection().get(this.name);

            if (obj instanceof JsonPrimitive && ((JsonPrimitive) obj).isNumber())
            {
                int value = obj.getAsInt();

                if (value <= this.max && value >= this.min)
                {
                    return value;
                }

                set(this.defaultValue);
                return this.defaultValue;
            }
            else
            {
                set(this.defaultValue);
                return this.defaultValue;
            }
        }

        @Override
        public void set(Integer object)
        {
            Preconditions.checkArgument(object >= this.min && object <= this.max, "Cannot set higher than max or lower than min");

            this.section.getSection().addProperty(this.name, object);
        }

        public boolean test(int value)
        {
            return value <= this.max && value >= this.min;
        }
    }

    public static class RangeDoubleValue extends ConfigValue<Double>
    {
        private double min;
        private double max;

        public RangeDoubleValue(ConfigSection section, String name, Double defaultValue, double min, double max, String description)
        {
            super(section, name, defaultValue, description);

            Preconditions.checkArgument(min <= max, "min must be less than or equal to max");

            this.min = min;
            this.max = max;

            get();
        }

        public double getMin()
        {
            return this.min;
        }

        public double getMax()
        {
            return this.max;
        }

        @Override
        public Double get()
        {
            JsonElement obj = this.section.getSection().get(this.name);

            if (obj instanceof JsonPrimitive && ((JsonPrimitive) obj).isNumber())
            {
                double value = obj.getAsDouble();

                if (value <= this.max && value >= this.min)
                {
                    return value;
                }

                set(this.defaultValue);
                return this.defaultValue;
            }
            else
            {
                set(this.defaultValue);
                return this.defaultValue;
            }
        }

        @Override
        public void set(Double object)
        {
            Preconditions.checkArgument(object >= this.min && object <= this.max, "Cannot set higher than max or lower than min");

            this.section.getSection().addProperty(this.name, object);
        }

        public boolean test(double value)
        {
            return value <= this.max && value >= this.min;
        }
    }

    public static class StringValue extends ConfigValue<String>
    {
        private Predicate<String> validator;

        public StringValue(ConfigSection section, String name, String defaultValue, Predicate<String> validator, String description)
        {
            super(section, name, defaultValue, description);

            Preconditions.checkArgument(validator.test(defaultValue), "Default value %s is not valid", defaultValue);

            this.validator = validator;

            get();
        }

        public Predicate<String> getValidator()
        {
            return this.validator;
        }

        @Override
        public String get()
        {
            JsonElement obj = this.section.getSection().get(this.name);

            if (obj instanceof JsonPrimitive)
            {
                String s = obj.getAsString();

                if (this.validator.test(s))
                {
                    return s;
                }

                set(this.defaultValue);
                return this.defaultValue;
            }
            else
            {
                set(this.defaultValue);
                return this.defaultValue;
            }
        }

        @Override
        public void set(String object)
        {
            Preconditions.checkArgument(this.validator.test(object), "Value %s is not valid", object);

            this.section.getSection().addProperty(this.name, object);
        }

        public boolean test(String value)
        {
            return this.validator.test(value);
        }
    }

    public static class EnumValue<E extends Enum<E>> extends ConfigValue<E>
    {
        private Class<E> clazz;

        public EnumValue(ConfigSection section, String name, E defaultValue, Class<E> clazz, String description)
        {
            super(section, name, defaultValue, description);

            this.clazz = clazz;

            get();
        }

        public Class<E> getClazz()
        {
            return this.clazz;
        }

        @Override
        public E get()
        {
            JsonElement obj = this.section.getSection().get(this.name);

            if (obj instanceof JsonPrimitive)
            {
                String s = obj.getAsString();

                try
                {
                    return Enum.valueOf(this.clazz, s);
                }
                catch (Exception e)
                {
                    set(this.defaultValue);
                    return this.defaultValue;
                }
            }
            else
            {
                set(this.defaultValue);
                return this.defaultValue;
            }
        }

        @Override
        public void set(E object)
        {
            this.section.getSection().addProperty(this.name, object.name());
        }
    }

    private static ConfigSection getSection(ConfigSection currentSection, String name)
    {
        JsonElement obj = currentSection.getSection().get(name);
        ConfigSection section;

        if (currentSection.getSpec().get(name) instanceof ConfigSection)
        {
            return (ConfigSection) currentSection.getSpec().get(name);
        }

        if (obj instanceof JsonObject)
        {
            section = new ConfigSection((JsonObject) obj, currentSection, name);
        }
        else
        {
            JsonObject newObj = new JsonObject();
            currentSection.getSection().add(name, newObj);
            section = new ConfigSection(newObj, currentSection, name);
        }

        currentSection.getSpec().put(name, section);
        return section;
    }

    public static BooleanValue getBoolean(ConfigSection currentSection, String name, boolean defaultValue, String description)
    {
        return new BooleanValue(currentSection, name, defaultValue, description);
    }

    public static RangeIntValue getRangeInt(ConfigSection currentSection, String name, int defaultValue, int min, int max, String description)
    {
        return new RangeIntValue(currentSection, name, defaultValue, min, max, description);
    }

    public static RangeDoubleValue getRangeDouble(ConfigSection currentSection, String name, double defaultValue, double min, double max, String description)
    {
        return new RangeDoubleValue(currentSection, name, defaultValue, min, max, description);
    }

    public static StringValue getString(ConfigSection currentSection, String name, String defaultValue, Predicate<String> validator, String description)
    {
        return new StringValue(currentSection, name, defaultValue, validator, description);
    }

    public static StringValue getString(ConfigSection currentSection, String name, String defaultValue, String description)
    {
        return new StringValue(currentSection, name, defaultValue, s -> true, description);
    }

    public static <E extends Enum<E>> EnumValue<E> getEnum(ConfigSection currentSection, String name, E defaultValue, Class<E> clazz, String description)
    {
        return new EnumValue<>(currentSection, name, defaultValue, clazz, description);
    }

  public enum Justification
  {
    CENTER, LEFT, RIGHT
  }
}
