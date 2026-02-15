package com.cutesmouse.bdgame.particle;

import org.bukkit.Particle;

public class ParticleSettingProfile {
    private Particle type;
    private double radius;
    private int number;

    public ParticleSettingProfile(Particle type, double radius, int number) {
        this.type = type;
        this.radius = radius;
        this.number = number;
    }

    public Particle getType() {
        return type;
    }

    public void setType(Particle type) {
        this.type = type;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
