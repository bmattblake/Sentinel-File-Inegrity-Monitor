package dirwatch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

public class PathIO {

    private ArrayList<Path> pathList;
    private File file;
    private FileWriter fr;
    private Scanner sc;

    public PathIO(File f) {
        setPathFile(f);
    }

    public void addPath(String path) {
        try {
            fr = new FileWriter(file, true);
            fr.write(path + "\n");
            fr.close();

        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean deletePath(String path) {
        boolean flag = false;
        try {
            File temp = new File("temp");
            sc = new Scanner(file);
            fr = new FileWriter(temp);

            while(sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.equals(path))
                    continue;
                fr.write(line + "\n");
            }
            sc.close();
            fr.close();
            file.delete();
            flag = temp.renameTo(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return flag;
    }

    public ArrayList<Path> getPaths() {
        try {
            sc = new Scanner(file);
            pathList = new ArrayList<>();
            while (sc.hasNextLine()) {
                Path line = Paths.get(sc.nextLine());
                pathList.add(line);
            }
            sc.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
            e.printStackTrace();
        }
        return pathList;
    }

    public void setPathFile(File f) {
        file = f;
    }
}
