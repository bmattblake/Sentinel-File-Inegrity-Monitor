package dirwatch;

import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.util.Calendar;
import static java.nio.file.StandardWatchEventKinds.*;

public class DirWatcher {
    public String[] watchDirs(ArrayList<Path> pathArrayList) {
        String[] result = null;
 
        try {
            // Create watcher
            WatchService watcher = FileSystems.getDefault().newWatchService();

            // Create Hash Map to store paths and path keys
            Map<WatchKey, Path> keyMap = new HashMap<>();

            // Add paths and their respective keys to keyMap for every path in pathArrayList
            for (Path dir : pathArrayList) {
            WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            keyMap.put(key, dir);
            }

            // Pull events out of watcher queue and print relavent information
            WatchKey watchKey;
            // Get all events in queue
            watchKey = watcher.take();
            // Get the directory associated with the event(s)
            Path eventDir = keyMap.get(watchKey);
            Map<String, String> outputMap = new HashMap<>();
            outputMap.put("ENTRY_CREATE", "[FILE CREATED]");
            outputMap.put("ENTRY_DELETE", "[FILE DELETED]");
            outputMap.put("ENTRY_MODIFY", "[FILE MODIFIED]");
            
            for (WatchEvent<?> event : watchKey.pollEvents()) {
                // Detirmine event type (Create, Modify, Delete)
                WatchEvent.Kind<?> kind = event.kind();
                // Get specific file associated wiht event
                Path fileName = (Path) event.context();
                // Get Current time
                Date currentTime = Calendar.getInstance().getTime();
                // Print results
                String[] results = {currentTime.toString(), outputMap.get(kind.toString()),
                eventDir.toString() + "\\" + fileName.toString()};
                return results;
            }
            // Reset queue once all events have been outputed.
            watchKey.reset();     
        }       
        catch (IOException e) {}
        catch (InterruptedException e) {}
        
        return result;
    }
}