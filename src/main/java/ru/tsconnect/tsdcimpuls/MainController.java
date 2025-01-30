package ru.tsconnect.tsdcimpuls;

import javafx.application.Platform;
import javafx.animation.*;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainController {
    public Circle btnFullScreen;
    public Circle btnClose;
    public BorderPane root;
    public Text mkrh;
    public Text mksvh;
    public ChoiceBox settingsFieldCom;
    public Button btnSettingsSave;
    public ButtonBar navBtns;
    private double xOffset;
    private double yOffset;
    public BorderPane logo_bp;
    public static SerialBus serial;
    public static boolean doOpenSettings = false;
    public boolean doPortConfirm = false;

    protected ObservableList<String> portsAvailable = FXCollections.observableArrayList(serial.getPortList());

//    private final InvalidationListener listener = newValue -> {
//        if (newValue != null) {
//            enterSettingsBtn();
//            showPane(btnSettings);
//            btnSettings.heightProperty().removeListener(this.listener);
//        }
//    };

    @FXML
    public void initialize() {
        btnSettingsSave.setUserData(0);

        refreshPorts();
        for (String port : portsAvailable) {
            settingsFieldCom.setValue(port);
            serial.set(port);
        }

        serial.open(serial.getCom((String) settingsFieldCom.getValue()));

        logo_bp.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Node node = (Node) event.getSource();
                Stage stage = (Stage) node.getScene().getWindow();
                if (!stage.isFullScreen()) {
                    xOffset = stage.getX() - event.getScreenX();
                    yOffset = stage.getY() - event.getScreenY();
                }
            }
        });
        logo_bp.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Node node = (Node) event.getSource();
                Stage stage = (Stage) node.getScene().getWindow();
                if (!stage.isFullScreen()) {
                    stage.setX(event.getScreenX() + xOffset);
                    stage.setY(event.getScreenY() + yOffset);
                }
            }
        });
        settingsFieldCom.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                refreshPorts();
            }
        });
    }

    @FXML
    protected void close() throws SerialPortException {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        if (serial.isOpened()) {
            serial.write("TSTr2RX:bye");
            serial.sendCMD("BLANK");
            serial.close();
        }
        stage.close();
    }

    @FXML
    protected void fullScreen() throws SerialPortException {
        Stage stage = (Stage) btnFullScreen.getScene().getWindow();
        double old_w = stage.getWidth();
        double old_h = stage.getHeight();

//        serial.sendSND("FullScreen");

        if (stage.isFullScreen()) {
            stage.setFullScreen(false);
            logo_bp.setCursor(Cursor.MOVE);
            Rectangle rect = new Rectangle(900,600);
            rect.setArcHeight(16);
            rect.setArcWidth(16);
            stage.getScene().setFill(Color.TRANSPARENT);
            stage.getScene().getRoot().setClip(rect);

        } else {
            stage.setFullScreen(true);
            logo_bp.setCursor(Cursor.DEFAULT);
            stage.getScene().getRoot().setClip(null);

        }
    }

/*
    !=== Pain НАСТРОЙКИ ===!
 */

    protected void refreshPorts() {
        portsAvailable = FXCollections.observableArrayList(serial.getPortList());
        for (String port : portsAvailable) {
            if (port.contains(serial.getCom())) {
                portsAvailable.set(portsAvailable.indexOf(serial.getCom()), serial.getCom() + " (Wing)");
            }
        }
        settingsFieldCom.setItems(portsAvailable);
    }

    @FXML
    public void savePortSettings(ActionEvent actionEvent) throws SerialPortException {
        if (serial.isOpened()) serial.close();
        if (serial.open(serial.getCom((String) settingsFieldCom.getValue()))) {

            serial.settings(
                    115200, 8,
                    1,0
            );
            serial.setHardwareMode(true);
            serial.addEventListener(new PortReader(), SerialPort.MASK_RXCHAR);
            serial.write("TSTr2RX:hello");
        }

    }

/*
    !=== ANIMATIONS BEGIN ===!
 */



/*
    !=== ANIMATIONS END ===!
 */


/*
    !=== MAIN LISTENER ===!
 */


    private class PortReader implements SerialPortEventListener {
        public void serialEvent(SerialPortEvent event) {
            if(event.isRXCHAR() && event.getEventValue() > 0) {
                try {
                    String data = serial.readString(event.getEventValue()).replaceAll("\\r\\n", "");
                    if (!data.isEmpty() & !data.equals(" ") & !data.equals("\n")) {
                        System.out.println(data);
                        String[] params = data.split(":");
                        Integer total = Integer.valueOf(params[1]);
                        Integer impuls = Integer.valueOf(params[2]);
                        Integer checksum = Integer.valueOf(params[3]);
                        Integer time = 40;
                        Integer mkrh_value = 3600 * impuls / time / 240;
                        mkrh.setText(String.valueOf(mkrh_value));
                        mksvh.setText(Float.toString ( ((float) mkrh_value) / 100));
                    }
                }
                catch (SerialPortException ex) {
                    System.out.println(ex);
                }
            }
        }
    }



}