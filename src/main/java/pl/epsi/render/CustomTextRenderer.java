package pl.epsi.render;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class CustomTextRenderer {

    final char CH_WHITE_SPACE = ' ';
    final char CH_CARRIAGE_RETURN = '\n';
    final char CH_LINE_FEED = '\r';
    final char CH_VERTICAL_BREAK = '\b';

    public static class CharProps {
        public final int x;
        public final int y;
        public final int width;
        public final int height;

        CharProps(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    private static class FontProps {
        String name;
        int bitmapHeight;
        int bitmapWidth;
        int baseHeight;
        int baseLine;
        int spaceWidth;
        final Map<Character, CharProps> characters = new TreeMap<>();
        public CharProps getCharProps(char ch) {
            return characters.getOrDefault(ch, null);
        }
        public FontProps setCharProps(char ch, CharProps cp) {
            characters.put(ch, cp);
            return this;
        }
    }

    public static class Cursor {
        public final int x;
        public final int y;
        public final boolean inside;
        public Cursor(int x, int y, boolean inside) {
            this.x = x;
            this.y = y;
            this.inside = inside;
        }
    }

    private final static Map<Identifier, CustomTextRenderer> renderers = new TreeMap<>();

    final private Identifier font;
    final private FontProps props;

    private int horizontalSpacing = 1;
    private int verticalSpacing = 2;
    private int paragraphBreak = 4;

    private CustomTextRenderer(Identifier font, FontProps props) {
        this.font = font;
        this.props = props;
    }

    public static CustomTextRenderer of(String modId, String name) {
        Identifier font = new Identifier(modId, name + ".png");
        if (renderers.containsKey(font)) return renderers.get(font);

        Yaml yaml = new Yaml(new Constructor(FontProps.class, new LoaderOptions()));
        Optional<ModContainer> maybeContainer = FabricLoader.getInstance().getModContainer(modId);
        if (maybeContainer.isEmpty()) return null;

        Optional<Path> maybePath = maybeContainer.get().findPath("assets/" + modId + "/" + name + ".yml");
        if (maybePath.isEmpty()) return null;

        try {
            FontProps props = yaml.load(new FileInputStream(maybePath.get().toFile()));
            return renderers.put(font, new CustomTextRenderer(font, props));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public CustomTextRenderer setHorizontalSpacing(int horizontalSpacing) {
        this.horizontalSpacing = horizontalSpacing;
        return this;
    }

    public CustomTextRenderer setVerticalSpacing(int verticalSpacing) {
        this.verticalSpacing = verticalSpacing;
        return this;
    }

    public CustomTextRenderer setParagraphBreak(int paragraphBreak) {
        this.paragraphBreak = paragraphBreak;
        return this;
    }

    public static Cursor nextCursor(Cursor current, int charWidth, int charHeight, int horizontalSpacing, int verticalSpacing, int x, int y, int regionWidth, int regionHeight) {
        int nx = current.x;
        int ny = current.y;
        if (charWidth > 0 && current.x + charWidth + horizontalSpacing <= x + regionWidth) {
            nx += charWidth + horizontalSpacing;
        } else {
            nx = x;
            ny += charHeight + verticalSpacing;
        }
        return new Cursor(nx, ny, nx <= x + regionWidth && ny <= y + regionHeight);
    }

    public Cursor submit(DrawContext context, char ch, Cursor current, int x, int y, int width, int height) {
        if (ch == CH_WHITE_SPACE)
            return nextCursor(current, props.spaceWidth, props.baseHeight, 0, verticalSpacing, x, y, width, height);
        if (ch == CH_CARRIAGE_RETURN)
            return nextCursor(current, width, props.baseHeight, 0, verticalSpacing, x, y, width, height);
        if (ch == CH_LINE_FEED)
            return nextCursor(current, 0, props.baseHeight, 0, verticalSpacing, x, y, width, height);
        if (ch == CH_VERTICAL_BREAK)
            return nextCursor(current, 0, props.baseHeight + paragraphBreak, 0, 0, x, y, width, height);

        CharProps cp = props.getCharProps(ch);
        if (cp == null) return current;

        Cursor next = nextCursor(current, cp.width, cp.height, horizontalSpacing, verticalSpacing, x, y, width, height);
        if (next.inside) context.drawTexture(font, current.x, current.y, cp.width, cp.height, cp.x, cp.y,
                cp.width, cp.height, props.bitmapWidth, props.bitmapHeight);

        return next;
    }

    public Cursor render(DrawContext context, String text, int x, int y) {
        return render(context, text, x, y, 1000, 1000);
    }

    public Cursor render(DrawContext context, String text, int x, int y, int width, int height) {
        Cursor cursor = new Cursor(x, y, true);
        for (int i = 0; i < text.length(); ++i) {
            cursor = submit(context, text.charAt(i), cursor, x, y, width, height);
            if (!cursor.inside) break;
        }
        return cursor;
    }

    public Cursor render(DrawContext context, List<String> texts, int x, int y, int width, int height) {
        return render(context, String.join(String.valueOf(CH_VERTICAL_BREAK), texts), x, y, width, height);
    }
}