import java.awt.Color;
import java.io.*;
import java.nio.file.*;

/**
 * Responsable de la sérialisation et désérialisation d'un {@link World}
 * vers/depuis un fichier texte.
 *
 * <p>Format du fichier :</p>
 * <pre>
 * NAME=A
 * SIZE=10
 * COLOR=80,140,255
 * PLAYER=1,2
 * CELLS:
 * 0,0,WALL
 * 0,1,WALL
 * ...
 * BOXES:
 * 3,5
 * 6,5
 * </pre>
 *
 * <p>Seules les cellules non vides (WALL, TARGET) sont écrites.
 * Les cases EMPTY sont implicites.</p>
 */
public class WorldSerializer {

    // Marqueurs de section dans le fichier
    private static final String CELLS_MARKER = "CELLS:";
    private static final String BOXES_MARKER = "BOXES:";

    // ---------------------------------------------------------------
    //  SAUVEGARDE
    // ---------------------------------------------------------------

    /**
     * Sauvegarde un monde dans le fichier spécifié.
     *
     * @param world  le monde à sauvegarder
     * @param path   chemin du fichier de destination
     * @throws PersistenceException si l'écriture échoue
     */
    public static void save(World world, String path) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(path))) {

            // En-tête
            writer.write("NAME=" + world.getName());
            writer.newLine();
            writer.write("SIZE=" + world.getSize());
            writer.newLine();

            Color c = world.getColor();
            writer.write("COLOR=" + c.getRed() + "," + c.getGreen() + "," + c.getBlue());
            writer.newLine();

            writer.write("PLAYER=" + world.getPlayerPosition().row()
                    + "," + world.getPlayerPosition().col());
            writer.newLine();

            // Cellules non vides
            writer.write(CELLS_MARKER);
            writer.newLine();
            for (int r = 0; r < world.getSize(); r++) {
                for (int c2 = 0; c2 < world.getSize(); c2++) {
                    Position pos = new Position(r, c2);
                    CellType type = world.getCell(pos);
                    if (type != CellType.EMPTY) {
                        writer.write(r + "," + c2 + "," + type.name());
                        writer.newLine();
                    }
                }
            }

            // Boîtes
            writer.write(BOXES_MARKER);
            writer.newLine();
            for (Box box : world.getBoxes()) {
                writer.write(box.getPosition().row() + "," + box.getPosition().col());
                writer.newLine();
            }

        } catch (IOException e) {
            throw new PersistenceException("Erreur lors de la sauvegarde du monde : " + path, e);
        }
    }

    // ---------------------------------------------------------------
    //  CHARGEMENT
    // ---------------------------------------------------------------

    /**
     * Charge un monde depuis le fichier spécifié.
     *
     * @param path  chemin du fichier source
     * @return le monde reconstruit
     * @throws PersistenceException si le fichier est introuvable ou malformé
     */
    public static World load(String path) {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(path))) {

            char name    = readChar(reader, "NAME");
            int  size    = readInt(reader, "SIZE");
            Color color  = readColor(reader, "COLOR");
            Position player = readPosition(reader, "PLAYER");

            World world = new World(name, size, color);
            world.setPlayerPosition(player);

            // Lecture des cellules
            expectMarker(reader, CELLS_MARKER);
            String line;
            while ((line = reader.readLine()) != null && !line.equals(BOXES_MARKER)) {
                if (line.isBlank()) continue;
                String[] parts = line.split(",");
                if (parts.length != 3) throw new PersistenceException("Ligne CELL invalide : " + line);
                int r  = Integer.parseInt(parts[0].trim());
                int c  = Integer.parseInt(parts[1].trim());
                CellType type = CellType.valueOf(parts[2].trim());
                world.setCell(new Position(r, c), type);
            }

            // Lecture des boîtes
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] parts = line.split(",");
                if (parts.length != 2) throw new PersistenceException("Ligne BOX invalide : " + line);
                int r = Integer.parseInt(parts[0].trim());
                int c = Integer.parseInt(parts[1].trim());
                world.addBox(new Box(new Position(r, c)));
            }

            return world;

        } catch (IOException e) {
            throw new PersistenceException("Fichier introuvable ou illisible : " + path, e);
        } catch (NumberFormatException e) {
            throw new PersistenceException("Valeur numérique invalide dans : " + path, e);
        }
    }

    // ---------------------------------------------------------------
    //  Méthodes utilitaires de lecture
    // ---------------------------------------------------------------

    private static char readChar(BufferedReader reader, String key) throws IOException {
        String line = reader.readLine();
        checkKey(line, key);
        return line.substring(key.length() + 1).trim().charAt(0);
    }

    private static int readInt(BufferedReader reader, String key) throws IOException {
        String line = reader.readLine();
        checkKey(line, key);
        return Integer.parseInt(line.substring(key.length() + 1).trim());
    }

    private static Color readColor(BufferedReader reader, String key) throws IOException {
        String line = reader.readLine();
        checkKey(line, key);
        String[] parts = line.substring(key.length() + 1).split(",");
        return new Color(
                Integer.parseInt(parts[0].trim()),
                Integer.parseInt(parts[1].trim()),
                Integer.parseInt(parts[2].trim())
        );
    }

    private static Position readPosition(BufferedReader reader, String key) throws IOException {
        String line = reader.readLine();
        checkKey(line, key);
        String[] parts = line.substring(key.length() + 1).split(",");
        return new Position(
                Integer.parseInt(parts[0].trim()),
                Integer.parseInt(parts[1].trim())
        );
    }

    private static void expectMarker(BufferedReader reader, String marker) throws IOException {
        String line = reader.readLine();
        if (!marker.equals(line)) {
            throw new PersistenceException("Marqueur attendu '" + marker + "', trouvé : " + line);
        }
    }

    private static void checkKey(String line, String key) {
        if (line == null || !line.startsWith(key + "=")) {
            throw new PersistenceException("Clé attendue '" + key + "', ligne : " + line);
        }
    }
}
