package com.tictactoe.animation;

import javafx.animation.AnimationTimer;
import javafx.scene.effect.BlendMode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class ParticleEffect {
    private final Pane container;
    private final List<Particle> particles;
    private final Random random;
    private AnimationTimer animator;
    private final Color particleColor;
    private final double sourceX;
    private final double sourceY;

    public ParticleEffect(Pane container, double sourceX, double sourceY, Color color) {
        this.container = container;
        this.particles = new ArrayList<>();
        this.random = new Random();
        this.particleColor = color;
        this.sourceX = sourceX;
        this.sourceY = sourceY;
    }

    public void start() {
        // Create initial particles
        for (int i = 0; i < 20; i++) {
            createParticle();
        }

        animator = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateParticles();
            }
        };
        animator.start();
    }

    public void stop() {
        if (animator != null) {
            animator.stop();
        }
        container.getChildren().removeAll(particles);
        particles.clear();
    }

    private void createParticle() {
        Particle particle = new Particle();
        particle.setRadius(random.nextDouble() * 3 + 1);
        particle.setFill(particleColor);
        particle.setBlendMode(BlendMode.ADD);
        particle.setOpacity(random.nextDouble() * 0.6 + 0.4);
        
        // Set initial position at the source
        particle.setTranslateX(sourceX);
        particle.setTranslateY(sourceY);
        
        // Random velocity
        double angle = random.nextDouble() * 2 * Math.PI;
        double speed = random.nextDouble() * 100 + 50;
        particle.velocityX = Math.cos(angle) * speed;
        particle.velocityY = Math.sin(angle) * speed;
        
        particles.add(particle);
        container.getChildren().add(particle);
    }

    private void updateParticles() {
        Iterator<Particle> iter = particles.iterator();
        while (iter.hasNext()) {
            Particle particle = iter.next();
            
            // Update position
            particle.setTranslateX(particle.getTranslateX() + particle.velocityX * 0.016);
            particle.setTranslateY(particle.getTranslateY() + particle.velocityY * 0.016);
            
            // Apply gravity
            particle.velocityY += 98.1 * 0.016;
            
            // Fade out
            particle.setOpacity(particle.getOpacity() - 0.016);
            
            // Remove if faded out
            if (particle.getOpacity() <= 0) {
                container.getChildren().remove(particle);
                iter.remove();
                if (random.nextDouble() < 0.3) { // 30% chance to create a new particle
                    createParticle();
                }
            }
        }
    }

    private class Particle extends Circle {
        double velocityX;
        double velocityY;
    }
} 