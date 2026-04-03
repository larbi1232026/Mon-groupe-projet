import org.junit.jupiter.api.*;
import java.awt.Color;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour toutes les classes de persistance.
 *
 * Lancer avec : javac -cp junit-platform-console-standalone.jar *.java
 *               java  -cp .:junit-platform-console-standalone.jar
 *                     org.junit.platform.console.ConsoleLauncher --select-class=PersistenceTest
 */
public class PersistenceTest {

    private static final String TEST_WORLD_FILE    = "saves/test_world.txt";
    private static final String TEST_SAVE_FILE     = "saves/test_save.save";
    private static final String TEST_SOLUTION_FILE = "saves/test_solution.sol";

    // ---------------------------------------------------------------
    //  Nettoyage avant/après chaque test
    // ---------------------------------------------------------------

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(Paths.get("saves"));
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get(TEST_WORLD_FILE));
        Files.deleteIfExists(Paths.get(TEST_SAVE_FILE));
        Files.deleteIfExists(Paths.get(TEST_SOLUTION_FILE));
        Files.deleteIfExists(Paths.get("config.properties"));
    }

    // ===================================================================
    //  Tests de WorldSerializer
    // ===================================================================

    @Test
    @DisplayName("WorldSerializer - sauvegarde et rechargement d'un monde simple")
    void testWorldSaveAndLoad() {
        // Construction du monde original
        World original = buildTestWorld();

        // Sauvegarde
        WorldSerializer.save(original, TEST_WORLD_FILE);

        // Rechargement
        World loaded = WorldSerializer.load(TEST_WORLD_FILE);

        // Vérifications
        assertEquals(original.getName(), loaded.getName(), "Nom du monde");
        assertEquals(original.getSize(), loaded.getSize(), "Taille du monde");
        assertEquals(original.getPlayerPosition(), loaded.getPlayerPosition(), "Position joueur");
        assertEquals(original.getBoxes().size(), loaded.getBoxes().size(), "Nombre de boîtes");

        assertTrue(loaded.isWall(new Position(0, 0)), "Mur en (0,0)");
        assertTrue(loaded.isTarget(new Position(3, 7)), "Cible en (3,7)");
        assertTrue(loaded.hasBoxAt(new Position(3, 5)), "Boîte en (3,5)");
    }

    @Test
    @DisplayName("WorldSerializer - monde sans boîtes ni cibles")
    void testWorldSaveAndLoadEmpty() {
        World world = new World('Z', 5, Color.GRAY);
        world.setCell(new Position(0, 0), CellType.WALL);

        WorldSerializer.save(world, TEST_WORLD_FILE);
        World loaded = WorldSerializer.load(TEST_WORLD_FILE);

        assertEquals('Z', loaded.getName());
        assertEquals(5, loaded.getSize());
        assertTrue(loaded.getBoxes().isEmpty(), "Aucune boîte attendue");
        assertTrue(loaded.isWall(new Position(0, 0)));
    }

    @Test
    @DisplayName("WorldSerializer - fichier inexistant lève PersistenceException")
    void testWorldLoadFileNotFound() {
        assertThrows(PersistenceException.class, () ->
                WorldSerializer.load("saves/fichier_inexistant.txt")
        );
    }

    // ===================================================================
    //  Tests de GameSaveManager
    // ===================================================================

    @Test
    @DisplayName("GameSaveManager - saveExists retourne false si pas de sauvegarde")
    void testSaveNotExists() {
        assertFalse(GameSaveManager.saveExists("inexistant.save"));
    }

    @Test
    @DisplayName("GameSaveManager - sauvegarde crée bien le fichier")
    void testSaveCreatesFile() {
        World world = buildTestWorld();
        GameState state = new GameState(world);
        GameModel model = new GameModel(state);

        GameSaveManager.save(model, "test_save.save");

        assertTrue(GameSaveManager.saveExists("test_save.save"),
                "Le fichier de sauvegarde doit exister après save()");
    }

    @Test
    @DisplayName("GameSaveManager - suppression de sauvegarde")
    void testDeleteSave() {
        World world = buildTestWorld();
        GameState state = new GameState(world);
        GameModel model = new GameModel(state);

        GameSaveManager.save(model, "test_save.save");
        assertTrue(GameSaveManager.saveExists("test_save.save"));

        GameSaveManager.deleteSave("test_save.save");
        assertFalse(GameSaveManager.saveExists("test_save.save"));
    }

    // ===================================================================
    //  Tests de SolutionIO
    // ===================================================================

    @Test
    @DisplayName("SolutionIO - sauvegarde et rechargement d'une solution")
    void testSolutionSaveAndLoad() {
        List<Direction> solution = List.of(
                Direction.UP, Direction.RIGHT, Direction.RIGHT,
                Direction.DOWN, Direction.LEFT
        );

        SolutionIO.save(solution, "test_solution.sol");
        List<Direction> loaded = SolutionIO.load("test_solution.sol");

        assertEquals(solution.size(), loaded.size(), "Taille de la solution");
        for (int i = 0; i < solution.size(); i++) {
            assertEquals(solution.get(i), loaded.get(i), "Direction à l'index " + i);
        }
    }

    @Test
    @DisplayName("SolutionIO - solution vide lève IllegalArgumentException")
    void testSolutionSaveEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                SolutionIO.save(List.of(), "test_solution.sol")
        );
    }

    @Test
    @DisplayName("SolutionIO - fichier inexistant lève PersistenceException")
    void testSolutionLoadNotFound() {
        assertThrows(PersistenceException.class, () ->
                SolutionIO.load("inexistant.sol")
        );
    }

    @Test
    @DisplayName("SolutionIO - solutionExists fonctionne correctement")
    void testSolutionExists() {
        assertFalse(SolutionIO.solutionExists("inexistant.sol"));

        List<Direction> solution = List.of(Direction.UP, Direction.DOWN);
        SolutionIO.save(solution, "test_solution.sol");

        assertTrue(SolutionIO.solutionExists("test_solution.sol"));
    }

    // ===================================================================
    //  Tests de ConfigManager
    // ===================================================================

    @Test
    @DisplayName("ConfigManager - valeur par défaut si clé absente")
    void testConfigDefaultValue() {
        String val = ConfigManager.get("cle_inexistante", "valeur_defaut");
        assertEquals("valeur_defaut", val);
    }

    @Test
    @DisplayName("ConfigManager - set et get d'une valeur String")
    void testConfigSetAndGet() {
        ConfigManager.set(ConfigManager.KEY_LAST_WORLD, "B");
        assertEquals("B", ConfigManager.get(ConfigManager.KEY_LAST_WORLD, "A"));
    }

    @Test
    @DisplayName("ConfigManager - set et get d'une valeur entière")
    void testConfigSetAndGetInt() {
        ConfigManager.set(ConfigManager.KEY_ANIMATION_SPEED, 250);
        assertEquals(250, ConfigManager.getInt(ConfigManager.KEY_ANIMATION_SPEED, 170));
    }

    @Test
    @DisplayName("ConfigManager - reset remet les valeurs par défaut")
    void testConfigReset() {
        ConfigManager.set(ConfigManager.KEY_ANIMATION_SPEED, 999);
        ConfigManager.reset();
        assertEquals(ConfigManager.DEFAULT_ANIMATION_SPEED,
                ConfigManager.getInt(ConfigManager.KEY_ANIMATION_SPEED, -1));
    }

    // ===================================================================
    //  Méthode utilitaire : construction d'un monde de test
    // ===================================================================

    private World buildTestWorld() {
        World world = new World('A', 10, new Color(80, 140, 255));

        // Murs de bordure
        for (int i = 0; i < 10; i++) {
            world.setCell(new Position(0, i), CellType.WALL);
            world.setCell(new Position(9, i), CellType.WALL);
            world.setCell(new Position(i, 0), CellType.WALL);
            world.setCell(new Position(i, 9), CellType.WALL);
        }

        world.setPlayerPosition(new Position(1, 2));
        world.setCell(new Position(3, 7), CellType.TARGET);
        world.setCell(new Position(6, 7), CellType.TARGET);
        world.addBox(new Box(new Position(3, 5)));
        world.addBox(new Box(new Position(6, 5)));

        return world;
    }
}
