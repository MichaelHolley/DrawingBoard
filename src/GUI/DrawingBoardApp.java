package GUI;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

public class DrawingBoardApp extends Application {

    private final int mouseOffset = 60;
    private Point2D lastDrawPoint;

    private Stage window;
    private Canvas canvas;
    private GraphicsContext gc;

    @Override
    public void start(Stage primaryStage) {
        window = primaryStage;
        window.setTitle("Drawing Board ~ by Michael Holley");
        window.setX(100);
        window.setY(100);
        window.setResizable(false);
        window.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        BorderPane layout = new BorderPane();

        /* OPTION PANE */
        HBox optionPane = new HBox();
        optionPane.setPrefHeight(60);
        optionPane.setSpacing(10);
        optionPane.setPadding(new Insets(10, 10, 10, 10));
        optionPane.setAlignment(Pos.CENTER);
        ColorPicker colorSelection = new ColorPicker();
        colorSelection.setValue(Color.CORNFLOWERBLUE);
        colorSelection.setOnAction(actionEvent -> {
            gc.setFill(colorSelection.getValue());
            gc.setStroke(colorSelection.getValue());
        });
        optionPane.getChildren().add(colorSelection);

        Slider sizeSlider = new Slider();
        sizeSlider.setMin(1);
        sizeSlider.setMax(50);
        sizeSlider.setValue(18);
        optionPane.getChildren().add(sizeSlider);

        Label sizeSliderLabel = new Label();
        sizeSliderLabel.setText(String.format("%.0f", sizeSlider.getValue()));
        sizeSliderLabel.setPrefWidth(30);
        sizeSlider.valueProperty().addListener((observableValue, oldNumber, newNumber) -> {
            sizeSlider.setValue(newNumber.intValue());
            sizeSliderLabel.setText(String.format("%.0f", sizeSlider.getValue()));
            gc.setLineWidth(sizeSlider.getValue());
        });
        optionPane.getChildren().add(sizeSliderLabel);

        Separator optionPaneSeparator_1 = new Separator();
        optionPaneSeparator_1.setOrientation(Orientation.VERTICAL);
        optionPane.getChildren().add(optionPaneSeparator_1);

        final ToggleGroup group = new ToggleGroup();
        RadioButton drawButton = new RadioButton("Draw");
        drawButton.setToggleGroup(group);
        drawButton.setSelected(true);
        RadioButton eraseButton = new RadioButton("Erase");
        eraseButton.setToggleGroup(group);
        optionPane.getChildren().addAll(drawButton, eraseButton);

        Separator optionPaneSeparator_2 = new Separator();
        optionPaneSeparator_2.setOrientation(Orientation.VERTICAL);
        optionPane.getChildren().add(optionPaneSeparator_2);

        Button clearButton = new Button("Clear");
        clearButton.setOnAction(actionEvent -> gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight()));
        optionPane.getChildren().add(clearButton);

        Button fillButton = new Button("Fill");
        fillButton.setOnAction(actionEvent -> gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight()));
        optionPane.getChildren().add(fillButton);
        layout.setTop(optionPane);

        Separator optionPaneSeparator_3 = new Separator();
        optionPaneSeparator_3.setOrientation(Orientation.VERTICAL);
        optionPane.getChildren().add(optionPaneSeparator_3);

        Button saveButton = new Button("Save");
        saveButton.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter =
                    new FileChooser.ExtensionFilter("png files (*.png)", "*.png");
            fileChooser.getExtensionFilters().add(extFilter);
            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                try {
                    WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
                    canvas.snapshot(null, writableImage);
                    RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                    ImageIO.write(renderedImage, "png", file);
                } catch (IOException ex) {
                }
            }
        });
        optionPane.getChildren().add(saveButton);

        /* CANVAS */
        canvas = new Canvas(800, 740);
        gc = canvas.getGraphicsContext2D();
        gc.setStroke(colorSelection.getValue());
        gc.setFill(colorSelection.getValue());
        gc.setLineWidth(sizeSlider.getValue());

        canvas.setOnMousePressed(mouseEvent ->
            lastDrawPoint = new Point2D(mouseEvent.getSceneX(), mouseEvent.getSceneY() - mouseOffset)
        );

        canvas.setOnMouseDragged(mouseEvent -> {
            Point2D location = new Point2D(
                    mouseEvent.getSceneX(), mouseEvent.getSceneY() - mouseOffset);
            double width = gc.getLineWidth();
            if (drawButton.isSelected() && !eraseButton.isSelected()) {
                Point2D diff = location.subtract(lastDrawPoint);
                double angle = Math.toDegrees(
                        Math.atan2(diff.getY(), diff.getX()));
                gc.save();
                gc.setTransform(new Affine(new Rotate(
                        angle, lastDrawPoint.getX(), lastDrawPoint.getY())));
                gc.fillOval(
                        lastDrawPoint.getX() - width / 2,
                        lastDrawPoint.getY() - width / 2,
                        lastDrawPoint.distance(location) + width, width);
            } else if (!drawButton.isSelected() && eraseButton.isSelected()) {
                gc.clearRect(
                        lastDrawPoint.getX() - width / 2,
                        lastDrawPoint.getY() - width / 2,
                        lastDrawPoint.distance(location) + width, width);
            }
            gc.restore();
            lastDrawPoint = location;
        });
        layout.setCenter(canvas);

        Scene scene = new Scene(layout, 800, 800);
        scene.getStylesheets().add("GUI/OptionsStyling.css");
        window.setScene(scene);
        window.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
