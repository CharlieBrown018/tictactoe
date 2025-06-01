module com.tictactoe {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.slf4j;
    requires ch.qos.logback.core;
    requires ch.qos.logback.classic;
    requires org.xerial.sqlitejdbc;

    opens com.tictactoe to javafx.fxml;
    opens com.tictactoe.controller to javafx.fxml;
    exports com.tictactoe;
    exports com.tictactoe.controller;
    exports com.tictactoe.model.entity;
    exports com.tictactoe.model.enums;
}
