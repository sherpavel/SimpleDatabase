package sherp.simpledatabase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;

public class Entry {
    String name;
    List<String> data;
    List<String> localFiles;
    List<String> remoteFiles;
    private final List<EntryListener> entryListeners;

    /**
     * Creates a new entry with the specified name.
     *
     * @param name entry name
     * @throws IllegalArgumentException if name is empty
     */
    public Entry(String name) {
        name = name.trim();
        if (name.equals(""))
            throw new IllegalArgumentException("Empty name");
        this.name = name;
        data = new CopyOnWriteArrayList<>();
        localFiles = new CopyOnWriteArrayList<>();
        remoteFiles = new CopyOnWriteArrayList<>();
        entryListeners = new CopyOnWriteArrayList();
    }

    synchronized void addListener(EntryListener l) {
        entryListeners.add(l);
    }

    synchronized void removeListener(EntryListener l) {
        entryListeners.remove(l);
    }

//    private void updateName() {
//        for (EntryListener l : entryListeners)
//            l.entryNameChanged(this);
//    }
    private void updateData() {
        for (EntryListener l : entryListeners)
            l.entryDataChanged(this);
    }
    private void updateFiles() {
        for (EntryListener l : entryListeners)
            l.entryFilesChanged(this);
    }

    /**
     * Returns the entry name.
     *
     * @return Entry name
     */
    public String getName() {
        return name;
    }

//    /**
//     * Sets the entry name.
//     *
//     * @param name new entry name
//     * @throws IllegalArgumentException if name is empty
//     */
    void setName(String name) {
        name = name.trim();
        if (name.equals(""))
            throw new IllegalArgumentException("Empty name");
        this.name = name;
//        updateName();
    }

    /**
     * Returns the array of data.
     *
     * @return Array of data
     */
    public String[] getData() {
        return data.toArray(new String[]{});
    }

    /**
     * Returns the array of file paths.
     *
     * @return Array of file paths
     */
    public String[] getFiles() {
        List<String> files = new ArrayList<>();
        files.addAll(localFiles);
        files.addAll(remoteFiles);
        return files.toArray(new String[]{});
    }

    /**
     * Returns the amount of data lines.
     *
     * @return Amount of data lines
     */
    public int dataCount() {
        return data.size();
    }

    /**
     * Returns the amount of file.
     *
     * @return Amount of files
     */
    public int filesCount() {
        return localFiles.size() + remoteFiles.size();
    }

    /**
     * Adds the new {@code String} value (line) to the data array.
     *
     * @param data single line of data
     */
    public void uploadData(String data) {
        this.data.add(data);
        updateData();
    }

    /**
     * Adds the array of values (lines) to the data array.
     *
     * @param data array of data
     */
    public void uploadData(String... data) {
        this.data.addAll(Arrays.asList(data));
        updateData();
    }

    /**
     * Adds the collection of values (lines) to the data array.
     *
     * @param data collection of data
     */
    public void uploadData(Collection<String> data) {
        this.data.addAll(data);
        updateData();
    }

    /**
     * Adds the new {@code String} file path to the files array.
     *
     * @param path path to a file
     */
    public void uploadFile(String path) {
        remoteFiles.add(path);
        updateFiles();
    }

    /**
     * Adds the array of file paths to the files array.
     *
     * @param paths array of file paths
     */
    public void uploadFiles(String... paths) {
        remoteFiles.addAll(Arrays.asList(paths));
        updateFiles();
    }

    /**
     * Adds the collection of file paths to the files array.
     *
     * @param paths collection of file paths
     */
    public void uploadFiles(Collection<String> paths) {
        remoteFiles.addAll(paths);
        updateFiles();
    }

    /**
     * Removes the value from the data array.
     *
     * @param index index of the {@code String} value in data array
     * @return {@code true} if removed successfully, {@code false} if {@code index} is out of bounds
     */
    public boolean removeData(int index) {
        if (index < 0 || index >= data.size())
            return false;
        data.remove(index);
        updateData();
        return true;
    }

    /**
     * Removes the value from the files array.
     *
     * @param index index of the {@code String} value in files array
     * @return {@code true} if removed successfully, {@code false} if {@code index} is out of bounds
     */
    public boolean removeFile(int index) {
        if (index < 0 || index >= filesCount())
            return false;
        if (index < localFiles.size())
            localFiles.remove(index);
        else
            remoteFiles.remove(index - localFiles.size());
        updateFiles();
        return true;
    }

    /**
     * Clears the data array.
     */
    public void clearData() {
        data.clear();
        updateData();
    }

    /**
     * Clears the files array.
     */
    public void clearFiles() {
        localFiles.clear();
        remoteFiles.clear();
        updateFiles();
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("Name: ").append(name);
        str.append(System.lineSeparator()).append("[").append(data.size()).append("] Data");
        for (int i = 0; i < data.size(); i++) {
            str.append(System.lineSeparator());
            if (i != data.size()-1)
                str.append("\u2502 ");
            else
                str.append("\u2514 ");
            str.append(data.get(i));
        }
        String[] files = getFiles();
        str.append(System.lineSeparator()).append("[").append(files.length).append("] Files");
        for (int i = 0; i < files.length; i++) {
            str.append(System.lineSeparator());
            if (i != files.length-1)
                str.append("\u2502 ");
            else
                str.append("\u2514 ");
            str.append(files[i]);
        }
        return str.toString();
    }
}
