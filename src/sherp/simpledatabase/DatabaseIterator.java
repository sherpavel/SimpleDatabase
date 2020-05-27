package sherp.simpledatabase;

import java.util.Iterator;
import java.util.List;

class DatabaseIterator implements Iterator<Entry> {
    private final List<Entry> entries;
    private int iterator;

    DatabaseIterator(Database database) {
        entries = database.entries;
        iterator = 0;
    }

    @Override
    public boolean hasNext() {
        return iterator < entries.size();
    }

    @Override
    public Entry next() {
        return entries.get(iterator++);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
