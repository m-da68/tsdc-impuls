package ru.tsconnect.tsdcimpuls;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

import java.io.IOException;


public class PreLoaderController {
    public AnchorPane root;
    public Text status;
    protected Stage stage;
    private static SerialBus serial;

    @FXML
    public void initialize() throws IOException {
        root.sceneProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null) {
                newValue.windowProperty().addListener(((observable1, oldValue1, newValue1) -> {
                    if (newValue1 != null) {
                        this.stage = (Stage) root.getScene().getWindow();
                        this.stage.show();
                        status.setText("Initialize");
                        new Timeline(new KeyFrame(
                                Duration.millis(1000),
                                ae -> {
                                    try {
                                        task1();
                                    } catch (IOException | SerialPortException e) {
                                        throw new RuntimeException(e);
                                    }
                                }))
                                .play();
                    }
                }));
            }
        }));
    }

    public void task1() throws IOException, SerialPortException {
        status.setText("Check product device...");
        serial = new SerialBus("1A86", "7523#", "REV_0254#");

        if (serial.isSet()) {
            if (serial.open(serial.getCom())) {
                serial.settings(115200, 8, 1, 0);
                serial.setHardwareMode(true);
                serial.addEventListener(new PortReader(), SerialPort.MASK_RXCHAR);
                status.setText(serial.getFriendlyName() + " is detected");
                serial.sendCMD("STATUS");
                status.setText("Validate device");
            } else {
                status.setText("Device is not connected. Please select an available connection option in the settings");
                new Timeline(new KeyFrame(
                        Duration.millis(2000),
                        ae -> {
                            try {
                                showMain(true);
                            } catch (IOException | SerialPortException e) {
                                throw new RuntimeException(e);
                            }
                        }))
                        .play();
            }
        } else {
            status.setText("Check custom device...");
            serial.set("1A86", "7523#");

            if (serial.isSet()) {
                for (String device : serial.getDevices()) {
                    serial.setDevice(device);
                    serial.rebuildRegistryKey();
                    if (serial.isSet()) {
                        if (serial.open(serial.getCom())) {
                            serial.settings(115200, 8, 1, 0);
                            serial.setHardwareMode(true);
                            serial.addEventListener(new PortReader(), SerialPort.MASK_RXCHAR);
                            status.setText(serial.getFriendlyName() + " is detected");
                            serial.sendCMD("STATUS");
                            status.setText("Validate device");
                        } else {
                            status.setText("Other device detected");
                            new Timeline(new KeyFrame(
                                    Duration.millis(800),
                                    ae -> {
                                        try {
                                            showMain(true);
                                        } catch (IOException | SerialPortException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }))
                                    .play();
                        }
                    } else System.out.println("Stop point");
                }
            } else {
                status.setText("Device is not connected. Please select an available connection option in the settings");
                new Timeline(new KeyFrame(
                        Duration.millis(2000),
                        ae -> {
                            try {
                                showMain(true);
                            } catch (IOException | SerialPortException e) {
                                throw new RuntimeException(e);
                            }
                        }))
                        .play();
            }
        }
    }

    public void showMain(boolean doOpenSettings) throws IOException, SerialPortException {
        if (doOpenSettings) status.setText("Wing selected");
        MainController.doOpenSettings = doOpenSettings;
        MainController.serial = serial;
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("main.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 750,273);

        Rectangle rect = new Rectangle(750,273);
        rect.setArcHeight(16);
        rect.setArcWidth(16);
        scene.setFill(Color.TRANSPARENT);
        scene.getRoot().setClip(rect);
        Stage stage = (Stage) root.getScene().getWindow();
        stage.setAlwaysOnTop(false);
        stage.setTitle("TSStation");
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    private class PortReader implements SerialPortEventListener {
        public void serialEvent(SerialPortEvent event) {
            if(event.isRXCHAR() && event.getEventValue() > 0){
                try {
                    String data = PreLoaderController.serial.readString(event.getEventValue());
//                    System.out.println(data);
//                    System.out.println("str");
                    if (!data.isEmpty() & !data.equals(" ") & !data.equals("\n")) {
                        serial.removeEventListener();
                        if (data.contains("OK:TSTelemetry TSDC by Dmitriy Gusev")) {
                            if (!serial.getFriendlyName().toString().contains(String.format("TSTelemetry TSDC (COM%d)", serial.getComNumber()))) {
                                serial.setFriendlyName(String.format("TSTelemetry TSDC (COM%d)", serial.getComNumber()));
                            }
                            new Timeline(new KeyFrame(
                                    Duration.millis(100),
                                    ae -> {
                                        try {
                                            showMain(false);
                                        } catch (IOException | SerialPortException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }))
                                    .play();
                        } else {
                            status.setText("Device gave an incorrect response. Please restart app or replug device");
                            new Timeline(new KeyFrame(
                                    Duration.millis(10),
                                    ae -> {
                                        try {
                                            showMain(true);
                                        } catch (IOException | SerialPortException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }))
                                    .play();

                        }
                    }
                }
                catch (SerialPortException ex) {
                    System.out.println(ex);
                }
            }
        }
    }
}
