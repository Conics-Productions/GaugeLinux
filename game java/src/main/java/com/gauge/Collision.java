package com.gauge;

import javafx.geometry.Bounds;
import javafx.scene.Node;

public class Collision {

    public static boolean intersects(
            Node player,
            Node obstacle
    ) {

        Bounds playerBounds =
                player.getBoundsInParent();

        Bounds obstacleBounds =
                obstacle.getBoundsInParent();

        return playerBounds.intersects(
                obstacleBounds
        );
    }
}