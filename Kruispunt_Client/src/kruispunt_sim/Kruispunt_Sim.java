/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kruispunt_sim;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class Kruispunt_Sim extends Application {

    TextField[][] field;
    TextField[] ipFields;
    Label[] labels;
    ClientSocket connection;
    Intersection intersection;
    long timer = 0;

    @Override
    public void start(final Stage primaryStage) {
        intersection = new Intersection(connection);
        initialize(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void addTextLimiter(final TextField tf, final int maxLength) {
        TextFormatter<String> formatter = new TextFormatter<>(change -> {
            String text = change.getText();
            text = text.replaceAll("[^0-9]", "");
            if (tf.getText().length() > maxLength - 1) {
                text = "";
            }
            change.setText(text);
            return change;
        });

        tf.setTextFormatter(formatter);
    }

    public String getIpString() {
        return ipFields[0].getText() + labels[0].getText() + ipFields[1].getText() + labels[1].getText()
                + ipFields[2].getText() + labels[2].getText() + ipFields[3].getText();
    }

    public void initialize(final Stage primaryStage) {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setMaxSize(100, 100);
        grid.setVgap(1);
        grid.setHgap(1);
        field = new TextField[23][26];

        for (int x = 0; x < field.length; x++) {
            for (int y = 0; y < field[0].length; y++) {
                field[x][y] = new TextField();
                field[x][y].setMaxHeight(75);
                field[x][y].setMaxWidth(75);
                field[x][y].setMinHeight(20);
                field[x][y].setMinWidth(20);
                //field[x][y].setPrefSize(50, 50);
                field[x][y].setPrefColumnCount(2);
                field[x][y].setFont(Font.font("Tahoma", FontWeight.BOLD, field[x][y].getFont().getSize() - 2));
                field[x][y].setDisable(true);
                grid.add(field[x][y], x, y);
            }
        }

        ipFields = new TextField[5];
        labels = new Label[ipFields.length - 1];
        for (int i = 0; i < ipFields.length - 1; i++) {
            ipFields[i] = new TextField();
            ipFields[i].setMaxWidth(50);
            labels[i] = new Label();
            //addTextLimiter(ipFields[i], 3);
            labels[i].setText(".");
        }
        ipFields[ipFields.length - 1] = new TextField();
        //addTextLimiter(ipFields[ipFields.length - 1], 5);
        labels[labels.length - 1] = new Label();
        labels[labels.length - 1].setText(":");

        ipFields[4].setText("8080");

        //intersection = new Intersection();
        updateGrid();

        TilePane tile = new TilePane();
        tile.setPadding(new Insets(10, 10, 10, 10));
        tile.setPrefColumns(2);
        tile.setStyle("-fx-background-color: #CD5C5C;");
        HBox hbox = new HBox(8);

        Button btn = new Button();
        btn.setText("Connect");
        btn.setPrefSize(400, 27);
        btn.setOnAction((ActionEvent event) -> {
            if (connection == null) {
                connection = new ClientSocket(getIpString(), ipFields[4].getText());
                intersection = new Intersection(connection);
                btn.setText("Disconnect");
            } else {
                connection = null;
                intersection = null;
                timer = 0;
                btn.setText("Connect");
            }

            System.out.println(getIpString());
        });

        hbox.getChildren().addAll(ipFields[0], labels[0], ipFields[1], labels[1], ipFields[2], labels[2], ipFields[3], labels[3], ipFields[4], btn);
        tile.getChildren().add(hbox);

        BorderPane pane = new BorderPane();

        pane.setCenter(grid);
        pane.setBottom(tile);

        Scene scene = new Scene(pane, 400, 427);

        primaryStage.setTitle("Intersection..");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();

        new AnimationTimer() {
            @Override
            public void handle(long currentNanoTime) {
                //System.out.println(currentNanoTime);              
                if (intersection != null) {
                    timer++;
                    if (timer % 15 == 0) {
                        intersection.Update();
                        updateGrid();
                    }
                    if (timer >= 60) {
                        timer = 0;
                        intersection.sendState();
                        intersection.syncState();
                    }
                }
            }
        }.start();

    }

    private void colorizeGrid() {
        for (int x = 0; x < field.length; x++) {
            for (int y = 0; y < field[0].length; y++) {
                field[x][y].setStyle(intersection.getColorCode(x, y));
            }
        }
    }

    private void updateGrid() {
        if (intersection != null) {
            colorizeGrid();
            for (int x = 0; x < field.length; x++) {
                for (int y = 0; y < field[0].length; y++) {
                    field[x][y].setText(intersection.getLetterCode(x, y));
                }
            }
        }
    }
}
