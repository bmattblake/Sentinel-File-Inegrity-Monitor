package DirWatch;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class ConfigIO {

    private String file;
    private FileReader reader;
    private Properties props;
    
    public ConfigIO(String f) {
        setFile(f);
        try {
            reader = new FileReader(file);
            props = new Properties();
            props.load(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setFile(String f) {
        file = f;
    }

    public String getProperty(String key) {
        String value = props.getProperty(key);
        return value;
    }

    public void setProperty(String key, String value) throws IOException {
        props.setProperty(key, value);
        props.put(key, value);
        props.store(new FileOutputStream(file), null);
    }

    public void removeProperty(String key) throws IOException, FileNotFoundException {
        props.remove(key);
        props.store(new FileOutputStream(file), null);
    }
}