import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Gère la sauvegarde et le chargement d'une solution automatique.
 *
 * <p>Format du fichier :</p>
 * <pre>
 * STEPS=5
 * UP
 * RIGHT
 * RIGHT
 * DOWN
 * LEFT
 * </pre>
 *
 * <p>Chaque ligne correspond à une direction : UP, DOWN, LEFT ou RIGHT.</p>
 */
public class SolutionIO {

    private static final String STEPS_KEY  = "STEPS";
    private static final String SAVE_DIR   = "saves/";

    // ---------------------------------------------------------------
    //  SAUVEGARDE
    // ---------------------------------------------------------------

    /**
     * Sauvegarde une liste de directions dans un fichier.
     *
     * @param solution liste des directions constituant la solution
     * @param filename nom du fichier (sans chemin)
     * @throws PersistenceException si l'écriture échoue
     * @throws IllegalArgumentException si la solution est null ou vide
     */
    public static void save(List<Direction> solution, String filename) {
        if (solution == null || solution.isEmpty()) {
            throw new IllegalArgumentException("La solution est vide ou nulle.");
        }

        ensureSaveDir();
        String fullPath = SAVE_DIR + filename;

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(fullPath))) {

            writer.write(STEPS_KEY + "=" + solution.size());
            writer.newLine();

            for (Direction dir : solution) {
                writer.write(dir.name());
                writer.newLine();
            }

        } catch (IOException e) {
            throw new PersistenceException("Erreur lors de la sauvegarde de la solution : " + fullPath, e);
        }
    }

    // ---------------------------------------------------------------
    //  CHARGEMENT
    // ---------------------------------------------------------------

    /**
     * Charge une solution depuis un fichier.
     *
     * @param filename nom du fichier (sans chemin)
     * @return la liste de directions lue
     * @throws PersistenceException si le fichier est introuvable ou malformé
     */
    public static List<Direction> load(String filename) {
        String fullPath = SAVE_DIR + filename;

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(fullPath))) {

            // Nombre de pas attendus
            String stepsLine = reader.readLine();
            if (stepsLine == null || !stepsLine.startsWith(STEPS_KEY + "=")) {
                throw new PersistenceException("Format invalide : clé STEPS manquante dans " + fullPath);
            }
            int expectedSteps = Integer.parseInt(stepsLine.substring(STEPS_KEY.length() + 1).trim());

            // Lecture des directions
            List<Direction> solution = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                try {
                    solution.add(Direction.valueOf(line.trim()));
                } catch (IllegalArgumentException e) {
                    throw new PersistenceException("Direction invalide dans le fichier : '" + line + "'");
                }
            }

            // Vérification de cohérence
            if (solution.size() != expectedSteps) {
                throw new PersistenceException(
                        "Nombre de pas incohérent : attendu " + expectedSteps
                        + ", trouvé " + solution.size()
                );
            }

            return solution;

        } catch (IOException e) {
            throw new PersistenceException("Fichier de solution introuvable : " + fullPath, e);
        } catch (NumberFormatException e) {
            throw new PersistenceException("Valeur STEPS invalide dans : " + fullPath, e);
        }
    }

    // ---------------------------------------------------------------
    //  Utilitaires
    // ---------------------------------------------------------------

    /**
     * Vérifie si un fichier de solution existe.
     *
     * @param filename nom du fichier
     * @return true si le fichier existe
     */
    public static boolean solutionExists(String filename) {
        return Files.exists(Paths.get(SAVE_DIR + filename));
    }

    /**
     * Supprime un fichier de solution.
     *
     * @param filename nom du fichier
     */
    public static void deleteSolution(String filename) {
        try {
            Files.deleteIfExists(Paths.get(SAVE_DIR + filename));
        } catch (IOException e) {
            throw new PersistenceException("Impossible de supprimer : " + filename, e);
        }
    }

    private static void ensureSaveDir() {
        try {
            Files.createDirectories(Paths.get(SAVE_DIR));
        } catch (IOException e) {
            throw new PersistenceException("Impossible de créer le dossier de sauvegarde.", e);
        }
    }
}
