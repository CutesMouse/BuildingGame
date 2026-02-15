package com.cutesmouse.bdgame.particle;

import com.cutesmouse.bdgame.Main;
import com.saicone.rtag.RtagEntity;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.awt.*;

public class ParticleEntity {
    public static final double PARTICLE_DISPLAY_DISTANCE = 20;

    public static boolean isParticleEntity(Entity entity) {
        if (!entity.getType().equals(EntityType.ARMOR_STAND)) return false;
        return getNBTString(((ArmorStand) entity), "bd_particle") != null;
    }

    public static ParticleEntity modifyParticle(ArmorStand entity) {
        for (ParticleEntity particle : ParticleDisplayHandler.getParticleEntities()) {
            if (particle.getEntity().equals(entity)) return particle;
        }
        return null;
    }

    public static ParticleEntity loadAsParticle(ArmorStand entity) {
        return loadAsParticle(entity, null);
    }

    public static ParticleEntity loadAsParticle(ArmorStand entity, ParticleSettingProfile profile) {
        ParticleEntity inst = new ParticleEntity(entity);
        if (profile != null) {
            inst.setParticle(profile.getType());
            inst.setAmount(profile.getNumber());
            inst.setRadius(profile.getRadius());
        }
        return inst;
    }

    private ArmorStand entity;
    private Particle particle;
    private double radius;
    private int amount;

    private ParticleEntity(ArmorStand entity) {
        this.entity = entity;
        this.amount = 1;
        this.radius = 1;
        loadFieldsFromNBT();
        entitySetup();
    }

    private void loadFieldsFromNBT() {
        String particle_nbt = getNBTString(entity, "bd_particle");
        if (particle_nbt != null) particle = Particle.valueOf(particle_nbt);

        String radius_nbt = getNBTString(entity, "bd_radius");
        if (radius_nbt != null) radius = Double.parseDouble(radius_nbt);

        String amount_nbt = getNBTString(entity, "bd_amount");
        if (amount_nbt != null) amount = Integer.parseInt(amount_nbt);
    }

    private void entitySetup() {
        entity.setInvisible(true);
        entity.setGravity(false);
    }

    public Particle getParticle() {
        return this.particle;
    }

    public void setParticle(Particle particle) {
        this.particle = particle;
        setNBTString(entity, "bd_particle", particle.name());
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
        setNBTString(entity, "bd_radius", Double.toString(radius));
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
        setNBTString(entity, "bd_amount", Integer.toString(amount));
    }

    public ArmorStand getEntity() {
        return entity;
    }

    public void displayFor(Player player) {
        if (particle == null) return;
        if (entity.getWorld() != player.getWorld()) return;
        if (entity.getLocation().distance(player.getLocation()) > PARTICLE_DISPLAY_DISTANCE) return;
        Location loc = entity.getLocation();
        switch (particle) {
            case EFFECT:
                player.spawnParticle(particle, loc.getX(), loc.getY(), loc.getZ(),
                        amount, radius, radius, radius, new Particle.Spell(Color.RED, 1.0F));
                break;
            case CLOUD:
            case CAMPFIRE_SIGNAL_SMOKE:
            case DAMAGE_INDICATOR:
                player.spawnParticle(particle, loc.getX(), loc.getY(), loc.getZ(),
                        amount, radius, radius, radius, 0);
                break;
            case CRIT:
                player.spawnParticle(particle, loc.getX(), loc.getY(), loc.getZ(),
                        amount, radius, radius, radius, 0.1);
                break;
            case FLAME:
            case SNOWFLAKE:
            case SQUID_INK:
            case TOTEM_OF_UNDYING:
                player.spawnParticle(particle, loc.getX(), loc.getY(), loc.getZ(),
                        amount, radius, radius, radius, 0.01);
                break;
            default:
                player.spawnParticle(particle, loc.getX(), loc.getY(), loc.getZ(),
                        amount, radius, radius, radius);
                break;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ParticleEntity particle) {
            return particle.getEntity().equals(this.getEntity());
        }
        return false;
    }

    /* utils */
    private static String getNBTString(ArmorStand entity, String path) {
        NamespacedKey key = new NamespacedKey(Main.getPlugin(Main.class), path);
        return entity.getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }

    private static void setNBTString(ArmorStand entity, String path, String value) {
        NamespacedKey key = new NamespacedKey(Main.getPlugin(Main.class), path);
        entity.getPersistentDataContainer().set(key, PersistentDataType.STRING, value);
    }
}
