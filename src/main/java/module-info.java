module ru.tsconnect.tsdcimpuls {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.install4j.runtime;
    requires jssc;


    opens ru.tsconnect.tsdcimpuls to javafx.fxml;
    exports ru.tsconnect.tsdcimpuls;
}