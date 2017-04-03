package sample;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.stream.Stream;

public class Controller {

    private enum Color {
        WRONG,
        RIGHT,
        UNSET,
        RUNNING
    }

    @FXML
    private JFXButton btn_ckPRG;

    @FXML
    private JFXButton btn_ckUSB;

    @FXML
    private JFXButton btn_backup;

    @FXML
    private JFXButton btn_restore;

    @FXML
    private JFXProgressBar progressBar;

    private double progress;

    @FXML
    private ToggleGroup toggleGroup;

    @FXML
    private JFXTextField txt_f_prgm;

    @FXML
    private JFXTextField txt_f_dest;

    @FXML
    private TextArea txt_area;

    @FXML
    private JFXComboBox<Label> cm_box_driveLetter = new JFXComboBox<>();

    private String PRGM_PATH;
    private String DEST_PATH;
    private boolean flagP, flagD;
    private int nFiles;
    Thread thread;
    private boolean coping;

    private void copyFiles(int cp) {
        coping = true;
        try {
            File folder = new File(PRGM_PATH);
            int size = folder.listFiles().length;
            initBar(size);
            System.out.println(size);
            File destFolder = new File(DEST_PATH);
            if (!destFolder.exists()) {
                destFolder.createNewFile();
            }

            setBtnColor(cp, Color.RUNNING);
            pushMessage("Copy files from: " + PRGM_PATH + " to " + DEST_PATH);
            for (File file : folder.listFiles()) {
                Platform.runLater(() -> pushMessage("   coping file: " + file.getAbsolutePath()));
                if (file.isDirectory()) {
                    FileUtils.copyDirectory(file, new File(destFolder + "\\" + file.getName()));
                } else {
                    FileUtils.copyFile(file, new File(destFolder + "\\" + file.getName()));
                }
                incBar();
            }

            setBtnColor(cp, Color.RIGHT);
            Platform.runLater(() -> pushMessage("Done."));
        } catch (IOException e) {
            Platform.runLater(() -> pushMessage(e.getMessage()));
            setBtnColor(cp, Color.WRONG);
        }
        coping = false;
        if (!PRGM_PATH.contains(txt_f_prgm.getText()))
            Platform.runLater(() -> {
                String temp = PRGM_PATH;
                PRGM_PATH = DEST_PATH;
                DEST_PATH = temp;
            });
    }

    @FXML
    private void actionPerf_setBtn_ckPRG(ActionEvent event) {
        PRGM_PATH = txt_f_prgm.getText();
        boolean f = checkDir(PRGM_PATH);
        flagP = f;
        if (f) {
            setBtnColor(1, Color.RIGHT);
        } else setBtnColor(1, Color.WRONG);
        check();
    }

    @FXML
    private void actionPerf_setBtn_ckUSB(ActionEvent event) {
        DEST_PATH = cm_box_driveLetter.getSelectionModel().getSelectedItem().getText() + txt_f_dest.getText();
        boolean f = checkDir(DEST_PATH);
        flagD = f;
        if (f) setBtnColor(2, Color.RIGHT);
        else setBtnColor(2, Color.WRONG);
        check();
    }

    @FXML
    private void actionPerf_sellectedGE(ActionEvent event) {
        PRGM_PATH = "C:\\GE";
        DEST_PATH = "GE BACKUP";
        reset();
    }

    @FXML
    private void actionPerf_sellectedSX(ActionEvent event) {
        PRGM_PATH = "C:\\1992Ver7";
        DEST_PATH = "1992 BACKUP";
        reset();
    }

    @FXML
    private void actionPerf_backup(ActionEvent event) {
        if (!coping) {
            thread = new Thread() {
                @Override
                public void run() {
                    copyFiles(3);
                }
            };
            thread.start();
        }
    }

    @FXML
    private void actionPerf_restore(ActionEvent event) {
        if (!coping) {
            String temp = PRGM_PATH;
            PRGM_PATH = DEST_PATH;
            DEST_PATH = temp;
            thread = new Thread() {
                @Override
                public void run() {
                    copyFiles(4);
                }
            };
            thread.start();
        }
    }

    private void reset() {
        for (int i = 1; i < 5; i++) {
            setBtnColor(i, Color.UNSET);
        }
        txt_f_dest.textProperty().removeListener(listenerDest);
        txt_f_prgm.textProperty().removeListener(listenerPrgm);
        cm_box_driveLetter.valueProperty().removeListener(listenerDrivekey);
        txt_f_dest.setText(DEST_PATH);
        txt_f_prgm.setText(PRGM_PATH);
        txt_f_prgm.textProperty().addListener(listenerPrgm);
        txt_f_dest.textProperty().addListener(listenerDest);
        btn_ckUSB.setDisable(false);
        btn_ckPRG.setDisable(false);
        btn_backup.setDisable(true);
        btn_restore.setDisable(true);
        flagD = flagP = false;
        cm_box_driveLetter.setDisable(false);
        cm_box_driveLetter.getItems().clear();
        for (File f : File.listRoots()) {
            if (!f.toString().contains("C")) {
                cm_box_driveLetter.getItems().add(new Label(f.toString()));
            }
        }
        cm_box_driveLetter.getSelectionModel().select(0);
        cm_box_driveLetter.valueProperty().addListener(listenerDrivekey);
    }

    private boolean checkDir(String dir) {
        File f = new File(dir);
        if (f.exists()) {
            Platform.runLater(() -> pushMessage("Directory exists."));
            return true;
        } else {
            Platform.runLater(() -> pushMessage("Directory DOESN'T exists."));
        }
        return false;
    }

    private void check() {
        if (flagD && flagP) {
            btn_backup.setDisable(false);
            btn_restore.setDisable(false);
        } else {
            btn_backup.setDisable(true);
            btn_restore.setDisable(true);
        }
    }

    private void pushMessage(String m) {
        txt_area.appendText(m + "\n");
    }

    private void setBtnColor(int b, Color c) {
        String color = "-fx-background-color : ";
        switch (c) {
            case RIGHT:
                color += "#00C853";
                break;
            case UNSET:
                color += "#2196F3";
                break;
            case WRONG:
                color += "#E53935";
                break;
            case RUNNING:
                color += "#F57C00";
                break;
        }

        switch (b) {
            case 1:
                btn_ckPRG.setStyle(color);
                break;
            case 2:
                btn_ckUSB.setStyle(color);
                break;
            case 3:
                btn_backup.setStyle(color);
                break;
            case 4:
                btn_restore.setStyle(color);
                break;
        }
    }

    private void initBar(int c) {
        nFiles = c;
        progress = 0;
        progressBar.setProgress(0);
    }

    private void incBar() {
        progress += 1 / ((double) nFiles);
        progress = (progress > 1) ? 1 : progress;
        progressBar.setProgress(progress);
    }

    private void textChanged(int i) {
        if (i == 1) flagP = false;
        else flagD = false;
        setBtnColor(i, Color.UNSET);
        check();
        String s = ((i == 1) ? "Program" : "USB") + " directory path might have changed, try to check again...";
        pushMessage(s);
    }

    //listeners for text feilds text prop
    InvalidationListener listenerPrgm;
    InvalidationListener listenerDest;
    InvalidationListener listenerDrivekey;

    @FXML
    public void initialize() {
        listenerPrgm = observable -> textChanged(1);
        listenerDest = observable -> textChanged(2);
        listenerDrivekey = observable -> textChanged(2);
    }

    public void killThread() {
        if (!thread.isInterrupted()) {
            thread.interrupt();
        }
    }
}
