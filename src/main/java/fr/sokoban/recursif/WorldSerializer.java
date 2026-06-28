package fr.sokoban.recursif;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WorldSerializer {

    public static void save(World rootWorld, String path) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(path))) {
            Map<Character, World> worlds = new LinkedHashMap<>();
            collectWorlds(rootWorld, worlds);

            boolean first = true;
            for (World world : worlds.values()) {
                if (!first) {
                    writer.newLine();
                }
                first = false;

                writer.write(world.getName() + " " + world.getSize());
                writer.newLine();

                for (int r = 0; r < world.getSize(); r++) {
                    StringBuilder line = new StringBuilder();
                    for (int c = 0; c < world.getSize(); c++) {
                        line.append(toChar(world, new Position(r, c)));
                    }
                    writer.write(line.toString());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new PersistenceException("Erreur lors de la sauvegarde du niveau : " + path, e);
        }
    }

    public static World load(String path) {
        List<String> rawLines;
        try {
            rawLines = Files.readAllLines(Paths.get(path));
        } catch (IOException e) {
            throw new PersistenceException("Fichier introuvable ou illisible : " + path, e);
        }

        List<String> lines = new ArrayList<>();
        for (String line : rawLines) {
            if (!line.isEmpty()) {
                lines.add(line);
            }
        }

        if (lines.isEmpty()) {
            throw new PersistenceException("Fichier vide : " + path);
        }

        class WorldData {
            char name;
            int size;
            List<String> rows = new ArrayList<>();
        }

        Map<Character, WorldData> dataMap = new LinkedHashMap<>();
        int i = 0;

        while (i < lines.size()) {
            String header = lines.get(i).trim();
            String[] parts = header.split("\\s+");
            if (parts.length != 2 || parts[0].length() != 1) {
                throw new PersistenceException("En-tête de monde invalide : " + header);
            }

            char worldName = parts[0].charAt(0);
            int size;
            try {
                size = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                throw new PersistenceException("Taille de monde invalide : " + header, e);
            }

            if (i + size >= lines.size() + 1) {
                throw new PersistenceException("Nombre de lignes insuffisant pour le monde " + worldName);
            }

            WorldData wd = new WorldData();
            wd.name = worldName;
            wd.size = size;

            for (int r = 0; r < size; r++) {
                i++;
                if (i >= lines.size()) {
                    throw new PersistenceException("Monde incomplet : " + worldName);
                }
                String row = lines.get(i);
                if (row.length() != size) {
                    throw new PersistenceException(
                            "Ligne de taille invalide pour le monde " + worldName + " : " + row
                    );
                }
                wd.rows.add(row);
            }

            dataMap.put(worldName, wd);
            i++;
        }

        Map<Character, World> worlds = new LinkedHashMap<>();
        for (WorldData wd : dataMap.values()) {
            worlds.put(wd.name, new World(wd.name, wd.size, colorForWorld(wd.name)));
        }

        for (WorldData wd : dataMap.values()) {
            World world = worlds.get(wd.name);

            for (int r = 0; r < wd.size; r++) {
                String row = wd.rows.get(r);
                for (int c = 0; c < wd.size; c++) {
                    char ch = row.charAt(c);
                    Position pos = new Position(r, c);

                    switch (ch) {
                        case '#' -> world.setCell(pos, CellType.WALL);
                        case '.' -> world.setCell(pos, CellType.TARGET);
                        case '@' -> world.setPlayerPosition(pos);
                        case '+' -> {
                            world.setCell(pos, CellType.TARGET);
                            world.setPlayerPosition(pos);
                        }
                        case '$' -> world.addBox(new TargetBox(pos));
                        case '*' -> {
                            world.setCell(pos, CellType.TARGET);
                            world.addBox(new TargetBox(pos));
                        }
                        case ' ' -> {
                            // case vide
                        }
                        default -> {
                            if (Character.isLetter(ch)) {
                                boolean onTarget = Character.isUpperCase(ch);
                                char referencedWorldName = Character.toUpperCase(ch);

                                if (onTarget) {
                                    world.setCell(pos, CellType.TARGET);
                                }

                                World inner = worlds.get(referencedWorldName);
                                if (inner != null && referencedWorldName != wd.name) {
                                    world.addBox(new WorldBox(pos, inner));
                                } else {
                                    world.addBox(new TargetBox(pos));
                                }
                            } else {
                                throw new PersistenceException(
                                        "Caractère invalide '" + ch + "' dans le monde " + wd.name
                                );
                            }
                        }
                    }
                }
            }
        }

        World root = worlds.get('A');
        if (root != null) {
            return root;
        }

        return worlds.values().iterator().next();
    }

    private static void collectWorlds(World world, Map<Character, World> worlds) {
        if (world == null || worlds.containsKey(world.getName())) {
            return;
        }

        worlds.put(world.getName(), world);

        for (AbstractBox box : world.getBoxes()) {
            if (box instanceof WorldBox worldBox) {
                collectWorlds(worldBox.getInnerWorld(), worlds);
            }
        }
    }

    private static char toChar(World world, Position pos) {
        boolean isTarget = world.isTarget(pos);
        boolean isPlayer = world.getPlayerPosition() != null && world.getPlayerPosition().equals(pos);
        AbstractBox box = world.getBoxAt(pos);

        if (world.isWall(pos)) {
            return '#';
        }

        if (isPlayer) {
            return isTarget ? '+' : '@';
        }

        if (box instanceof WorldBox worldBox) {
            World inner = worldBox.getInnerWorld();
            char name = inner != null ? inner.getName() : 'W';
            return isTarget ? Character.toUpperCase(name) : Character.toLowerCase(name);
        }

        if (box instanceof TargetBox) {
            return isTarget ? '*' : '$';
        }

        if (isTarget) {
            return '.';
        }

        return ' ';
    }

    private static Color colorForWorld(char worldName) {
        float hue = ((worldName - 'A') * 0.17f) % 1.0f;
        return Color.getHSBColor(hue, 0.45f, 0.95f);
    }
}