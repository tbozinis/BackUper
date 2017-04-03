package myPackege;

import javafx.scene.control.ToggleGroup;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class Controller {
    private String _GEPATH;
    private String _DESTPATH;
    private ToggleGroup toggleGroup = new ToggleGroup();

    private void copyFiles() {
        try {
            File folder = new File(_GEPATH);
            int size = folder.listFiles().length;
            initBar(size);

            File destFolder = new File(_DESTPATH);
            if (!destFolder.exists()) {
                destFolder.createNewFile();
            }

            Stream<Path> paths = Files.walk(folder.toPath());
            paths.forEach((Path filePath) -> {
                try {
                    FileUtils.copyDirectory(filePath.toFile(), new File(destFolder + filePath.toFile().getName()));
                    incBar();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initBar(int c){

    }

    private void incBar(){

    }

}
