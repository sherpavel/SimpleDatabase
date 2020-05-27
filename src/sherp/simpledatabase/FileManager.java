package sherp.simpledatabase;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class FileManager {
    String currentPath;
    static String dataFolderName;

    private FileManager(boolean read, String path, String databaseName) {
        currentPath = path + File.separator + databaseName + File.separator;
        dataFolderName = ".sddata";

        if (read) {
            if (!new File(currentPath).exists())
                throw new RuntimeException("Database " + databaseName + " not found");
            if (!new File(currentPath + dataFolderName + File.separator + "log.dat").exists()) {
                try {
                    new File(currentPath + dataFolderName + File.separator + "log.dat").createNewFile();
                } catch (IOException e) {
                    System.err.println("Log file error");
                }
            }
        } else {
            if (!new File(currentPath).mkdirs()) {
                throw new RuntimeException("Database with the name" + databaseName + " exists in this location");
            }
            // Log file
            try {
                new File(currentPath + dataFolderName).mkdirs();
                new File(currentPath + dataFolderName + File.separator + "log.dat").createNewFile();
            } catch (IOException e) {
                System.err.println("Log file error");
            }
        }
    }

    static String[] scan(String path) {
        File dir = new File(path);
        if (dir.list() == null)
            throw new RuntimeException(path + " not found");

        List<String> databases = new ArrayList<>();
        for (String entry : dir.list()) {
            if (new File(entry).isDirectory()) {
                if (new File(entry + File.separator + dataFolderName).exists())
                    databases.add(entry);
            }
        }

        return databases.toArray(new String[]{});
    }

    static FileManager read(String path, String databaseName) {
        return new FileManager(true, path, databaseName);
    }

    static FileManager create(String path, String databaseName) {
        return new FileManager(false, path, databaseName);
    }

    synchronized void log(String line) {
        try {
            BufferedWriter logWriter = new BufferedWriter(new FileWriter(currentPath + dataFolderName + File.separator + "log.dat", true));
            logWriter.append(line).append(System.lineSeparator());
            logWriter.close();
        } catch (IOException e) {
            System.err.println("Log write error");
        }
    }

    synchronized String[] getEntryNames() {
        List<String> entries = new ArrayList<>();

        File rootFolder = new File(currentPath);
        for (String entry : rootFolder.list()) {
            if (new File(currentPath + entry).isDirectory() &&
                    !entry.startsWith("."))
                entries.add(entry);
        }

        return entries.toArray(new String[]{});
    }

    synchronized Entry[] readEntries() {
        List<Entry> entries = new ArrayList<>();

        File rootFolder = new File(currentPath);
        for (String name : rootFolder.list()) {
            if (name.equals(dataFolderName))
                continue;
            Entry entry = new Entry(name);
            entry.uploadData(readData(name));
            entry.localFiles.addAll(Arrays.asList(readFiles(entry)));
            entries.add(entry);
        }

        return entries.toArray(new Entry[]{});
    }

    synchronized String[] readData(String entryName) {
        String filepath = currentPath + entryName + File.separator + entryName + ".dat";
        if (!new File(filepath).exists()) {
            System.err.println("Dir/File " + entryName + " not found");
            return null;
        }

        List<String> lines = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filepath));
            String line;
            while ((line = reader.readLine()) != null)
                lines.add(line);
            reader.close();
        } catch (IOException e) {
            System.err.println("Write error");
            return null;
        }
        return lines.toArray(new String[]{});
    }

    synchronized boolean writeData(String entryName, String[] data) {
        String filepath = currentPath + entryName + File.separator + entryName + ".dat";
        if (!new File(filepath).exists()) {
            System.err.println("Dir/File " + entryName + " not found");
            return false;
        }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filepath));
            for (String line : data)
                writer.write(line + System.lineSeparator());
            writer.close();
        } catch (IOException e) {
            System.err.println("Write error");
            return false;
        }
        log("edit data in '" + entryName + "'");
        return true;
    }

    synchronized String[] readFiles(Entry entry) {
        String path = currentPath + entry.name + File.separator;
        List<String> files = new ArrayList<>();
        File folder = new File(path);

        if (!folder.exists()) {
            System.err.println("Dir " + entry.name + " not found");
            return null;
        }

        for (String file : folder.list()) {
            if (new File(currentPath + entry.name + File.separator + file).isFile() &&
                    !file.endsWith(".dat"))
                files.add(file);
        }

        return files.toArray(new String[]{});
    }

    synchronized boolean copyFile(Entry entry, String path) {
        if (!new File(path).exists()) {
            System.err.println("File not found in " + path);
            return false;
        }
        if (!new File(path).isFile()) {
            System.err.println(path + " is not a file");
            return false;
        }

        Path file = Paths.get(path);
        if (new File(currentPath + entry.name + File.separator + file.getFileName()).exists()) {
            System.err.println("File " + file.getFileName() + " is in the entry " + entry.name);
            return false;
        }

        try {
            Files.copy(file, Paths.get(currentPath + entry.name + File.separator + file.getFileName()));
        } catch (IOException e) {
            System.err.println("Copy error");
            e.printStackTrace();
            return false;
        }
        entry.localFiles.add(file.getFileName().toString());
        entry.remoteFiles.remove(path);
        log("file added to '" + entry.name + "' [" + path + "]");
        return true;
    }

    synchronized boolean deleteFile(String entryName, String filename) {
        if (!new File(currentPath + entryName + File.separator + filename).delete()) {
            System.err.println("File delete error");
            return false;
        }
        log("file delete from '" + entryName + "' [" + filename + "]");
        return true;
    }

    synchronized boolean make(String entryName) {
        File folder = new File(currentPath + entryName);
        File file = new File(currentPath + entryName + File.separator + entryName + ".dat");

        if (!folder.mkdir()) {
            System.err.println("Directory " + entryName + " exists");
            return false;
        }
        try {
            if (!file.createNewFile()) {
                System.err.println("File " + entryName + " exists");
                return false;
            }
        } catch (IOException e) {
            System.err.println("Make error");
            e.printStackTrace();
            return false;
        }
        log("new entry '" + entryName + "'");
        return true;
    }

    synchronized boolean delete(String entryName) {
        File folder = new File(currentPath + entryName);

        String[] files = folder.list();
        if (files != null) {
            for (String file : files) {
                new File(folder.getPath(), file).delete();
            }
        }
        if (!folder.delete()) {
            System.err.println("Directory " + entryName + " not found");
            return false;
        }
        log("del entry '" + entryName + "'");
        return true;
    }

    synchronized boolean rename(String entryName, String newName) {
        File folder = new File(currentPath + entryName);
        File file = new File(currentPath + entryName + File.separator + entryName + ".dat");

        if (new File(currentPath + newName).exists()) {
            System.err.println("Dir " + newName + " exists");
            return false;
        }
        if (!folder.exists() || !file.exists()) {
            System.err.println("Dir/File " + entryName + " not found");
            return false;
        }

        file = new File(currentPath + newName + File.separator + entryName + ".dat");
        if (!folder.renameTo(new File(currentPath + newName)) ||
                !file.renameTo(new File(currentPath + newName + File.separator + newName + ".dat"))) {
            System.err.println("Rename error");
            return false;
        }
        log("rename entry '" + entryName + "' -> '" + newName + "'");
        return true;
    }
}
