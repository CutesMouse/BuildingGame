package com.cutesmouse.bdgame.particle;

import org.bukkit.Particle;

public class ModifiableParticleSettingProfile extends ParticleSettingProfile {
    private ParticleEntity particle;
    public ModifiableParticleSettingProfile(ParticleEntity particle) {
        super(particle.getParticle(), particle.getRadius(), particle.getAmount());
        this.particle = particle;
    }

    public void remove() {
        particle.getEntity().remove();
    }

    @Override
    public void setType(Particle type) {
        super.setType(type);
        particle.setParticle(type);
    }

    @Override
    public void setRadius(double radius) {
        super.setRadius(radius);
        particle.setRadius(radius);
    }

    @Override
    public void setNumber(int number) {
        super.setNumber(number);
        particle.setAmount(number);
    }
}
