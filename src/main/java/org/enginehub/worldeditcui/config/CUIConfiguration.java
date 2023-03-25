package org.enginehub.worldeditcui.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.enginehub.worldeditcui.InitialisationFactory;
import org.enginehub.worldeditcui.render.ConfiguredColour;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Stores and reads WorldEditCUI settings
 *
 * @author yetanotherx
 * @author Adam Mummery-Smith
 * @author Jesús Sanz - Modified to work with the config GUI implementation
 */
public final class CUIConfiguration implements InitialisationFactory {
	private static final String CONFIG_FILE_NAME = "worldeditcui.config.json";

	private static final Gson GSON = new GsonBuilder()
	    .setPrettyPrinting()
	    .registerTypeAdapter(Colour.class, new TypeAdapter<Colour>() {
            @Override
            public Colour read(JsonReader arg0) throws IOException {
                if (arg0.peek() == JsonToken.BEGIN_OBJECT) {
                    arg0.beginObject();
                    String colour = null;
                    
                    while (arg0.peek() == JsonToken.NAME) {
                        if (!arg0.nextName().equals("hex")) {
                            arg0.skipValue();
                            continue;
                        }
                        colour = arg0.nextString();
                    }
                    arg0.endObject();
                    return colour == null ? null : Colour.parseRgba(colour);
                } else if (arg0.peek() == JsonToken.NUMBER) {
                    return new Colour(arg0.nextInt());
                } else {
                    return Colour.parseRgba(arg0.nextString());
                }
            }

            @Override
            public void write(
                JsonWriter arg0,
                Colour arg1
            ) throws IOException {
                arg0.value(arg1.hexString());
            }
	        
	    }.nullSafe())
	    .create();

	private boolean debugMode = false;
	private boolean promiscuous = false;
	private boolean clearAllOnKey = false;

    private Colour cuboidGridColor = ConfiguredColour.CUBOIDGRID.getDefault();
    private Colour cuboidEdgeColor = ConfiguredColour.CUBOIDBOX.getDefault();
    private Colour cuboidFirstPointColor = ConfiguredColour.CUBOIDPOINT1.getDefault();
    private Colour cuboidSecondPointColor = ConfiguredColour.CUBOIDPOINT2.getDefault();
    private Colour polyGridColor = ConfiguredColour.POLYGRID.getDefault();
    private Colour polyEdgeColor = ConfiguredColour.POLYBOX.getDefault();
    private Colour polyPointColor = ConfiguredColour.POLYPOINT.getDefault();
    private Colour ellipsoidGridColor = ConfiguredColour.ELLIPSOIDGRID.getDefault();
    private Colour ellipsoidPointColor = ConfiguredColour.ELLIPSOIDCENTRE.getDefault();
    private Colour cylinderGridColor = ConfiguredColour.CYLINDERGRID.getDefault();
    private Colour cylinderEdgeColor = ConfiguredColour.CYLINDERBOX.getDefault();
    private Colour cylinderPointColor = ConfiguredColour.CYLINDERCENTRE.getDefault();
    private Colour chunkBoundaryColour = ConfiguredColour.CHUNKBOUNDARY.getDefault();
    private Colour chunkGridColour = ConfiguredColour.CHUNKGRID.getDefault();

	private static transient Map<String, Object> configArray = new LinkedHashMap<>();

	/**
	 * Copies the default config file to the proper directory if it does not
	 * exist. It then reads the file and sets each variable to the proper value.
	 */
	@Override
	public void initialise() {
		int index = 0;
		try {
			for (Field field : this.getClass().getDeclaredFields()) {
				if (field.getType() == Colour.class) {
					ConfiguredColour configuredColour = ConfiguredColour.values()[index++];
					Colour colour = Colour.firstOrDefault((Colour)field.get(this), configuredColour.getColour().hexString());
					field.set(this, colour);
					configuredColour.setColour(colour);
				}
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

		this.save();
	}

	public boolean isDebugMode() {
		return this.debugMode;
	}

	public boolean isPromiscuous() {
		return this.promiscuous;
	}

	public void setPromiscuous(boolean promiscuous) {
		this.promiscuous = promiscuous;
	}

	public boolean isClearAllOnKey() {
		return this.clearAllOnKey;
	}

	public void setClearAllOnKey(boolean clearAllOnKey) {
		this.clearAllOnKey = clearAllOnKey;
	}

	private static Path getConfigFile() {
		return FabricLoader.getInstance().getConfigDir().resolve(CUIConfiguration.CONFIG_FILE_NAME);
	}

	public static CUIConfiguration create() {
		Path jsonFile = getConfigFile();

		CUIConfiguration config = null;
		if (Files.exists(jsonFile)) {
			try (Reader fileReader = Files.newBufferedReader(jsonFile, StandardCharsets.UTF_8)) {
				config = CUIConfiguration.GSON.fromJson(fileReader, CUIConfiguration.class);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		if (config == null) { // load failed or file didn't exist
			config = new CUIConfiguration();
		}


		configArray.put("debugMode", config.debugMode);
		configArray.put("promiscuous", config.promiscuous);
		configArray.put("clearAllOnKey", config.clearAllOnKey);

		configArray.put("cuboidGridColor", config.cuboidGridColor);
		configArray.put("cuboidEdgeColor", config.cuboidEdgeColor);
		configArray.put("cuboidFirstPointColor", config.cuboidFirstPointColor);
		configArray.put("cuboidSecondPointColor", config.cuboidSecondPointColor);
		configArray.put("polyGridColor", config.polyGridColor);
		configArray.put("polyEdgeColor", config.polyEdgeColor);
		configArray.put("polyPointColor", config.polyPointColor);
		configArray.put("ellipsoidGridColor", config.ellipsoidGridColor);
		configArray.put("ellipsoidPointColor", config.ellipsoidPointColor);
		configArray.put("cylinderGridColor", config.cylinderGridColor);
		configArray.put("cylinderEdgeColor", config.cylinderEdgeColor);
		configArray.put("cylinderPointColor", config.cylinderPointColor);
		configArray.put("chunkBoundaryColour", config.chunkBoundaryColour);
		configArray.put("chunkGridColour", config.chunkGridColour);

		return config;
	}

	public void changeValue(String text, Object value) {
	    if (value == null) {
	        configArray.replace(text, getDefaultValue(text));
	    } else {
            configArray.replace(text, value);
	    }
	}

	public Map<String, Object> getConfigArray() {
		return configArray;
	}

	public void configChanged() {
		debugMode 				= (Boolean) configArray.get("debugMode");
		promiscuous 			= (Boolean) configArray.get("promiscuous");
		clearAllOnKey 			= (Boolean) configArray.get("clearAllOnKey");

		cuboidGridColor 		= (Colour) 	configArray.get("cuboidGridColor");
		cuboidEdgeColor 		= (Colour) 	configArray.get("cuboidEdgeColor");
		cuboidFirstPointColor 	= (Colour) 	configArray.get("cuboidFirstPointColor");
		cuboidSecondPointColor 	= (Colour) 	configArray.get("cuboidSecondPointColor");
		polyGridColor 			= (Colour) 	configArray.get("polyGridColor");
		polyEdgeColor 			= (Colour) 	configArray.get("polyEdgeColor");
		polyPointColor 			= (Colour) 	configArray.get("polyPointColor");
		ellipsoidGridColor 		= (Colour) 	configArray.get("ellipsoidGridColor");
		ellipsoidPointColor 	= (Colour) 	configArray.get("ellipsoidPointColor");
		cylinderGridColor 		= (Colour) 	configArray.get("cylinderGridColor");
		cylinderEdgeColor 		= (Colour) 	configArray.get("cylinderEdgeColor");
		cylinderPointColor 		= (Colour) 	configArray.get("cylinderPointColor");
		chunkBoundaryColour 	= (Colour) 	configArray.get("chunkBoundaryColour");
		chunkGridColour 		= (Colour) 	configArray.get("chunkGridColour");
		this.initialise();
	}

	public Object getDefaultValue(String text) {
		return switch (text) {
			case "debugMode", "promiscuous", "clearAllOnKey" -> false;
			case "cuboidGridColor" -> ConfiguredColour.CUBOIDGRID.getDefault();
			case "cuboidEdgeColor" -> ConfiguredColour.CUBOIDBOX.getDefault();
			case "cuboidFirstPointColor" -> ConfiguredColour.CUBOIDPOINT1.getDefault();
			case "cuboidSecondPointColor" -> ConfiguredColour.CUBOIDPOINT2.getDefault();
			case "polyGridColor" -> ConfiguredColour.POLYGRID.getDefault();
			case "polyEdgeColor" -> ConfiguredColour.POLYBOX.getDefault();
			case "polyPointColor" -> ConfiguredColour.POLYPOINT.getDefault();
			case "ellipsoidGridColor" -> ConfiguredColour.ELLIPSOIDGRID.getDefault();
			case "ellipsoidPointColor" -> ConfiguredColour.ELLIPSOIDCENTRE.getDefault();
			case "cylinderGridColor" -> ConfiguredColour.CYLINDERGRID.getDefault();
			case "cylinderEdgeColor" -> ConfiguredColour.CYLINDERBOX.getDefault();
			case "cylinderPointColor" -> ConfiguredColour.CYLINDERCENTRE.getDefault();
			case "chunkBoundaryColour" -> ConfiguredColour.CHUNKBOUNDARY.getDefault();
			case "chunkGridColour" -> ConfiguredColour.CHUNKGRID.getDefault();
			default -> null;
		};

	}

	public @Nullable Component getTooltip(String text) {
		String key = getKey(text);
		if (key == null) return null;
		if (text.equals("clearAllOnKey")) {
			return Component.translatable(key + ".tooltip",
					Component.translatable("key.worldeditcui.clear"),
					Component.keybind("key.worldeditcui.clear").withStyle(Style.EMPTY.withItalic(true))
			);
		}
		return Component.translatable(key + ".tooltip");
	}
	
	public @Nullable Component getDescription(String text) {
		String key = getKey(text);
		if (key == null) return null;
		return Component.translatable(key);
	}

	private @Nullable String getKey(String text) {
		return switch (text) {
			case "debugMode" -> "worldeditcui.options.debugMode";
			case "promiscuous" -> "worldeditcui.options.compat.spammy";
			case "clearAllOnKey" -> "worldeditcui.options.extra.clearall";
			case "cuboidGridColor" -> "worldeditcui.color.cuboidgrid";
			case "cuboidEdgeColor" -> "worldeditcui.color.cuboidedge";
			case "cuboidFirstPointColor" -> "worldeditcui.color.cuboidpoint1";
			case "cuboidSecondPointColor" -> "worldeditcui.color.cuboidpoint2";
			case "polyGridColor" -> "worldeditcui.color.polygrid";
			case "polyEdgeColor" -> "worldeditcui.color.polyedge";
			case "polyPointColor" -> "worldeditcui.color.polypoint";
			case "ellipsoidGridColor" -> "worldeditcui.color.ellipsoidgrid";
			case "ellipsoidPointColor" -> "worldeditcui.color.ellipsoidpoint";
			case "cylinderGridColor" -> "worldeditcui.color.cylindergrid";
			case "cylinderEdgeColor" -> "worldeditcui.color.cylinderedge";
			case "cylinderPointColor" -> "worldeditcui.color.cylinderpoint";
			case "chunkBoundaryColour" -> "worldeditcui.color.chunkboundary";
			case "chunkGridColour" -> "worldeditcui.color.chunkgrid";
			default -> null;
		};
	}

	public void save() {
		try (Writer fileWriter = Files.newBufferedWriter(getConfigFile(), StandardCharsets.UTF_8)) {
			CUIConfiguration.GSON.toJson(this, fileWriter);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
