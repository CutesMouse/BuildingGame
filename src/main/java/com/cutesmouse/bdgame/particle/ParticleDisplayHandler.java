package com.cutesmouse.bdgame.particle;

import com.cutesmouse.bdgame.Main;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class ParticleDisplayHandler {

    private static ArrayList<ParticleEntity> particleEntities;

    public static void enable() {
        resetParticleEntities();
        new BukkitRunnable() {
            @Override
            public void run() {
                for (ParticleEntity particle : particleEntities) {
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        particle.displayFor(onlinePlayer);
                    }
                }
                removeNotLivingEntity();
            }
        }.runTaskTimer(Main.getPlugin(Main.class), 0L, 20L); // update every second
    }

    public static void resetParticleEntities() {
        particleEntities = new ArrayList<>();
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (!ParticleEntity.isParticleEntity(entity)) continue;
                particleEntities.add(ParticleEntity.loadAsParticle(((ArmorStand) entity)));
            }
        }
    }

    private static void removeNotLivingEntity() {
        particleEntities.removeIf(p -> p.getEntity().isDead() || !p.getEntity().isValid());
    }

    public static void registerParticle(ParticleEntity particle) {
        particleEntities.add(particle);
    }

    public static ArrayList<ParticleEntity> getParticleEntities() {
        return particleEntities;
    }
}
