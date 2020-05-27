package sherp.simpledatabase;

abstract class EntryListener {
//    void entryNameChanged(Entry entry);
    abstract void entryDataChanged(Entry entry);
    abstract void entryFilesChanged(Entry entry);
}
