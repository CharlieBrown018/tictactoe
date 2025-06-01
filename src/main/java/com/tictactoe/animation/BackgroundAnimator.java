package com.tictactoe.animation;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class BackgroundAnimator {
    private final Circle circle1;
    private final Circle circle2;
    private final Polygon triangle1;
    private final Rectangle rectangle1;

    public BackgroundAnimator(Circle circle1, Circle circle2, Polygon triangle1, Rectangle rectangle1) {
        this.circle1 = circle1;
        this.circle2 = circle2;
        this.triangle1 = triangle1;
        this.rectangle1 = rectangle1;
        
        // Initialize triangle points
        triangle1.getPoints().addAll(
            0.0, 0.0,
            20.0, 0.0,
            10.0, 20.0
        );

        // Set initial positions
        setRandomPosition(circle1);
        setRandomPosition(circle2);
        setRandomPosition(triangle1);
        setRandomPosition(rectangle1);

        // Start animations
        startAnimation(circle1, 5000);
        startAnimation(circle2, 7000);
        startAnimation(triangle1, 6000);
        startAnimation(rectangle1, 8000);
    }

    private void setRandomPosition(Node node) {
        node.setLayoutX(Math.random() * 800);
        node.setLayoutY(Math.random() * 600);
    }

    private void startAnimation(Node node, int duration) {
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(node.layoutXProperty(), node.getLayoutX()),
                new KeyValue(node.layoutYProperty(), node.getLayoutY()),
                new KeyValue(node.rotateProperty(), 0)
            ),
            new KeyFrame(Duration.millis(duration),
                new KeyValue(node.layoutXProperty(), Math.random() * 800),
                new KeyValue(node.layoutYProperty(), Math.random() * 600),
                new KeyValue(node.rotateProperty(), 360)
            )
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.setAutoReverse(true);
        timeline.play();
    }
} 