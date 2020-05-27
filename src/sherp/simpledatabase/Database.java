package sherp.simpledatabase;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.ArrayList;
import java.util.Collection;

public class Database extends EntryListener implements Iterable<Entry> {
    String name;
    String location;
    List<Entry> entries;
    FileManager fileManager;

    private Database(boolean read, String databasePath, String databaseName) {
        name = databaseName.trim();
        location = databasePath.trim();
        if (name.equals(""))
            throw new IllegalArgumentException("Empty name");

        entries = new CopyOnWriteArrayList<>();
        if (read) {
            fileManager = FileManager.read(location, name);
            Entry[] readEntries = fileManager.readEntries();
            entries.addAll(Arrays.asList(readEntries));
            for (Entry entry : readEntries)
                entry.addListener(this);
        } else {
            fileManager = FileManager.create(location, name);
        }
    }

    /**
     * Creates a new database in the specified folder. Returns {@code Database} instance.
     *
     * @param name database name
     * @param path absolute path of the parent folder
     * @return Database instance
     * @throws IllegalArgumentException if database name is empty
     */
    public static Database create(String name, String path) {
        return new Database(false, path, name);
    }

    /**
     * Creates a new database in local directory. Returns {@code Database} instance.
     *
     * @param name database name
     * @return Database instance
     * @throws IllegalArgumentException if database name is empty
     */
    public static Database create(String name) {
        return new Database(false, System.getProperty("user.dir"), name);
    }

    /**
     * Connects to the database in the specified location. Returns {@code Database} instance.
     *
     * @param name database name
     * @param path absolute path of the parent folder
     * @return Database instance
     * @throws IllegalArgumentException if database name is empty
     */
    public static Database connect(String path, String name) {
        return new Database(true, path, name);
    }

    /**
     * Connects to the database in local directory. Returns {@code Database} instance.
     *
     * @param name database name
     * @return Database instance
     * @throws IllegalArgumentException if database name is empty
     */
    public static Database connect(String name) {
        return new Database(true, System.getProperty("user.dir"), name);
    }

    /**
     * Scans the given folder for databases.
     *
     * @param path path to the folder
     * @return Array of found database names
     */
    public static String[] scan(String path) {
        return FileManager.scan(path.trim());
    }

    /**
     * Returns the database name.
     *
     * @return Database name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the absolute path to the database.
     *
     * @return Path
     */
    public String getLocation() {
        return location;
    }

    /**
     * Returns the array of all the entry names.
     *
     * @return Array of the entry names.
     */
    public synchronized String[] getEntryNames() {
        List<String> names = new ArrayList<>();
        for (Entry entry : entries)
            names.add(entry.name);
        return names.toArray(new String[]{});
    }

    /**
     * Returns the amount of entries in the database.
     *
     * @return Amount of entries.
     */
    public int size() {
        return entries.size();
    }

    /**
     * Returns {@code true} ot {@code false} whether the entry is in the database
     *
     * @param entryName entry name
     * @return {@code true} if entry is found, {@code false} if entry is not found
     */
    public synchronized boolean contains(String entryName) {
        for (Entry entry : entries) {
            if (entry.name.equals(entryName.trim()))
                return true;
        }
        return false;
    }

    /**
     * Returns entry, found by name. Returns {@code null} if entry is not found.
     * @param entryName entry name to be found
     * @return Entry if found, {@code null} if entry is not found
     */
    public synchronized Entry get(String entryName) {
        entryName = entryName.trim();
        for (Entry entry : entries) {
            if (entry.name.equals(entryName))
                return entry;
        }
        System.err.println("Entry name \"" + entryName + "\" not found");
        return null;
    }

    /**
     * Adds the entry to the database. Returns {@code true} if added successfully, {@code false} if entry with the same name already exists.
     * @param entry Entry to be added
     * @return {@code true} if added successfully, {@code false} if entry with the same name already exists
     */
    public synchronized boolean add(Entry entry) {
        if (contains(entry.name)) {
            System.err.println("Entry \"" + entry.name + "\" already exists");
            return false;
        }
        entries.add(entry);
        fileManager.make(entry.name);
        fileManager.writeData(entry.name, entry.getData());
        for (String filepath : entry.getFiles())
            fileManager.copyFile(entry, filepath);
        entry.addListener(this);
        return true;
    }

    /**
     * Creates a new entry with the set name and adds it to the database.
     *
     * @param entryName Entry name to be created and added
     * @return {@code true} if added successfully, {@code false} if entry with the same name already exists
     * @throws IllegalArgumentException if name is empty
     */
    public boolean add(String entryName) {
        return add(new Entry(entryName.trim()));
    }

    /**
     * Adds the array of entries to the database. If the entry with the same name already exists, entry is skipped.
     *
     * @param entries Array of entries
     */
    public void add(Entry... entries) {
        for (Entry entry : entries)
            add(entry);
    }

    /**
     * Adds the collection of entries to the database. If the entry with the same name already exists, entry is skipped.
     *
     * @param entries Collection of entries
     */
    public void add(Collection<Entry> entries) {
        for (Entry entry : entries)
            add(entry);
    }

    /**
     * Deletes the entry from the database. Returns {@code true} if deleted successfully, {@code false} if entry is not found.
     *
     * @param entryName Entry name to be found
     * @return {@code true} if deleted successfully, {@code false} if entry is not found
     */
    public synchronized boolean delete(String entryName) {
        entryName = entryName.trim();
        for (Entry entry : entries) {
            if (entry.name.equals(entryName)) {
                entries.remove(entry);
                fileManager.delete(entryName);
                return true;
            }
        }
        System.err.println("Entry \"" + entryName + "\" not found");
        return false;
    }

    /**
     * Renames the specified by name entry to a new name.
     *
     * @param entryName name of the entry in the database
     * @param newName new name to set the entry to
     * @return {@code true} if renamed successfully, {@code false} if could not find the entry or entry with {@code newName} name already exists
     * @throws IllegalArgumentException if the new name is empty
     */
    public synchronized boolean rename(String entryName, String newName) {
        entryName = entryName.trim();
        newName = newName.trim();
        Entry entry = get(entryName);
        if (entry == null || contains(newName)) {
            System.err.println();
            return false;
        }
        entry.setName(newName);
        fileManager.rename(entryName, newName);
        return true;
    }

    @Override
    public DatabaseIterator iterator() {
        return new DatabaseIterator(this);
    }

//    @Override
//    void entryNameChanged(Entry entry) {
//
//    }

    @Override
    synchronized void entryDataChanged(Entry entry) {
        fileManager.writeData(entry.name, entry.getData());
    }

    @Override
    synchronized void entryFilesChanged(Entry entry) {
        List<String> removeArray = new ArrayList<>();
        for (String remoteFile : entry.remoteFiles) {
            if (fileManager.copyFile(entry, remoteFile))
                removeArray.add(remoteFile);
        }
        for (String file : removeArray)
            entry.remoteFiles.remove(file);
        String[] entryFiles = fileManager.readFiles(entry);
        for (String file : entryFiles) {
            if (!entry.localFiles.contains(file))
                fileManager.deleteFile(entry.name, file);
        }
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (Entry entry : entries)
            str.append(System.lineSeparator()).append(entry);
        return str.toString();
    }
}
