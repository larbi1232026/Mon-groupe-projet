import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/**
 * Gère la configuration persistante de l'application via un fichier
 * {@code .properties}.
 *
 * <p>Exemple de fichier {@code config.properties} :</p>
 * <pre>
 * last_world=A
 * window_width=1100
 * window_height=920
 * animation_speed=170
 * last_save=partie1.save
 * </pre>
 *
 * <p>Usage :</p>
 * <pre>
 * ConfigManager.set("last_world", "B");
 * String world = ConfigManager.get("last_world", "A");
 * int speed = ConfigManager.getInt("animation_speed", 170);
 * </pre>
 */
public class ConfigManager {

    private static final String CONFIG_FILE = "config.properties";

    // Clés de configuration prédéfinies
    public static final String KEY_LAST_WORLD      = "last_world";
    public static final String KEY_WINDOW_WIDTH     = "window_width";
    public static final String KEY_WINDOW_HEIGHT    = "window_height";
    public static final String KEY_ANIMATION_SPEED  = "animation_speed";
    public static final String KEY_LAST_SAVE        = "last_save";

    // Valeurs par défaut
    public static final String  DEFAULT_LAST_WORLD     = "A";
    public static final int     DEFAULT_WINDOW_WIDTH    = 1100;
    public static final int     DEFAULT_WINDOW_HEIGHT   = 920;
    public static final int     DEFAULT_ANIMATION_SPEED = 170;
    public static final String  DEFAULT_LAST_SAVE       = "";

    private static Properties props = null;

    // ---------------------------------------------------------------
    //  LECTURE
    // ---------------------------------------------------------------

    /**
     * Retourne la valeur d'une clé de configuration.
     *
     * @param key          la clé
     * @param defaultValue valeur retournée si la clé est absente
     * @return la valeur associée à la clé
     */
    public static String get(String key, String defaultValue) {
        return getProperties().getProperty(key, defaultValue);
    }

    /**
     * Retourne la valeur entière d'une clé de configuration.
     *
     * @param key          la clé
     * @param defaultValue valeur retournée si la clé est absente ou invalide
     * @return la valeur entière
     */
    public static int getInt(String key, int defaultValue) {
        String value = getProperties().getProperty(key);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // ---------------------------------------------------------------
    //  ÉCRITURE
    // ---------------------------------------------------------------

    /**
     * Définit la valeur d'une clé et sauvegarde la configuration.
     *
     * @param key   la clé
     * @param value la valeur à associer
     * @throws PersistenceException si l'écriture échoue
     */
    public static void set(String key, String value) {
        getProperties().setProperty(key, value);
        saveProperties();
    }

    /**
     * Définit une valeur entière et sauvegarde la configuration.
     *
     * @param key   la clé
     * @param value la valeur entière
     */
    public static void set(String key, int value) {
        set(key, String.valueOf(value));
    }

    // ---------------------------------------------------------------
    //  RÉINITIALISATION
    // ---------------------------------------------------------------

    /**
     * Réinitialise la configuration aux valeurs par défaut et sauvegarde.
     */
    public static void reset() {
        props = new Properties();
        props.setProperty(KEY_LAST_WORLD,     DEFAULT_LAST_WORLD);
        props.setProperty(KEY_WINDOW_WIDTH,   String.valueOf(DEFAULT_WINDOW_WIDTH));
        props.setProperty(KEY_WINDOW_HEIGHT,  String.valueOf(DEFAULT_WINDOW_HEIGHT));
        props.setProperty(KEY_ANIMATION_SPEED, String.valueOf(DEFAULT_ANIMATION_SPEED));
        props.setProperty(KEY_LAST_SAVE,      DEFAULT_LAST_SAVE);
        saveProperties();
    }

    // ---------------------------------------------------------------
    //  CHARGEMENT / SAUVEGARDE INTERNES
    // ---------------------------------------------------------------

    /**
     * Retourne l'instance des propriétés, en chargeant le fichier si nécessaire.
     */
    private static Properties getProperties() {
        if (props == null) {
            props = loadProperties();
        }
        return props;
    }

    /**
     * Charge le fichier de configuration depuis le disque.
     * Si le fichier n'existe pas, crée une configuration par défaut.
     */
    private static Properties loadProperties() {
        Properties p = new Properties();

        if (!Files.exists(Paths.get(CONFIG_FILE))) {
            // Première utilisation : créer les valeurs par défaut
            p.setProperty(KEY_LAST_WORLD,     DEFAULT_LAST_WORLD);
            p.setProperty(KEY_WINDOW_WIDTH,   String.valueOf(DEFAULT_WINDOW_WIDTH));
            p.setProperty(KEY_WINDOW_HEIGHT,  String.valueOf(DEFAULT_WINDOW_HEIGHT));
            p.setProperty(KEY_ANIMATION_SPEED, String.valueOf(DEFAULT_ANIMATION_SPEED));
            p.setProperty(KEY_LAST_SAVE,      DEFAULT_LAST_SAVE);
            savePropertiesTo(p);
            return p;
        }

        try (InputStream in = Files.newInputStream(Paths.get(CONFIG_FILE))) {
            p.load(in);
        } catch (IOException e) {
            throw new PersistenceException("Impossible de lire la configuration : " + CONFIG_FILE, e);
        }

        return p;
    }

    /**
     * Sauvegarde les propriétés courantes dans le fichier.
     */
    private static void saveProperties() {
        savePropertiesTo(props);
    }

    /**
     * Écrit un objet Properties dans le fichier de configuration.
     */
    private static void savePropertiesTo(Properties p) {
        try (OutputStream out = Files.newOutputStream(Paths.get(CONFIG_FILE))) {
            p.store(out, "Configuration Sokoban Classique");
        } catch (IOException e) {
            throw new PersistenceException("Impossible d'écrire la configuration : " + CONFIG_FILE, e);
        }
    }
}
