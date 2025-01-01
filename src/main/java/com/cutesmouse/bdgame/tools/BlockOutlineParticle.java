package com.cutesmouse.bdgame.tools;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;


public class BlockOutlineParticle {
    private static final double space = 0.2;

    public static void playOutlineParticle(Player player, Location from, Location to, java.awt.Color color) {
        Color c = Color.fromBGR(color.getBlue(), color.getGreen(), color.getRed());
        for (double x = from.getX(); x < to.getX(); x += space) {
            playParticle(player, new Location(from.getWorld(), x, from.getY(), from.getZ()), c);
            playParticle(player, new Location(from.getWorld(), x, to.getY(), from.getZ()), c);
            playParticle(player, new Location(from.getWorld(), x, from.getY(), to.getZ()), c);
            playParticle(player, new Location(from.getWorld(), x, to.getY(), to.getZ()), c);
        }
        for (double y = from.getY(); y < to.getY(); y += space) {
            playParticle(player, new Location(from.getWorld(), from.getX(), y, from.getZ()), c);
            playParticle(player, new Location(from.getWorld(), to.getX(), y, from.getZ()), c);
            playParticle(player, new Location(from.getWorld(), from.getX(), y, to.getZ()), c);
            playParticle(player, new Location(from.getWorld(), to.getX(), y, to.getZ()), c);
        }
        for (double z = from.getZ(); z < to.getZ(); z += space) {
            playParticle(player, new Location(from.getWorld(), from.getX(), from.getY(), z), c);
            playParticle(player, new Location(from.getWorld(), from.getX(), to.getY(), z), c);
            playParticle(player, new Location(from.getWorld(), to.getX(), from.getY(), z), c);
            playParticle(player, new Location(from.getWorld(), to.getX(), to.getY(), z), c);
        }
    }

    private static void playParticle(Player p, Location loc, Color c) {
        p.spawnParticle(Particle.DUST, loc, 0, 0, 0, 0,
                new Particle.DustOptions(c, 1F));
    }
}
