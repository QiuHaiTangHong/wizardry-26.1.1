package top.begonia.wizardry.core.event;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import top.begonia.wizardry.core.data.json.definition.spell.part.SpellModifiers;
import top.begonia.wizardry.core.spell.AbstractSpell;

import javax.annotation.Nullable;

public abstract class SpellCastEvent extends Event {

    private final AbstractSpell spell;
    private final SpellModifiers modifiers;
    private final Source source;
    @Nullable
    private final LivingEntity caster;
    private final Level level;
    private final double x, y, z;
    private final Direction direction;

    public SpellCastEvent(Source source, AbstractSpell spell, LivingEntity caster, SpellModifiers modifiers) {
        super();
        this.spell = spell;
        this.modifiers = modifiers;
        this.source = source;
        this.caster = caster;
        this.level = caster.level();
        this.x = caster.getX();
        this.y = caster.getY();
        this.z = caster.getZ();
        this.direction = caster.getDirection();
    }

    public SpellCastEvent(Source source, AbstractSpell spell, Level level, double x, double y, double z, Direction direction, SpellModifiers modifiers) {
        super();
        this.spell = spell;
        this.modifiers = modifiers;
        this.source = source;
        this.caster = null;
        this.level = level;
        this.x = x;
        this.y = y;
        this.z = z;
        this.direction = direction;
    }

    public AbstractSpell getSpell() {
        return spell;
    }

    public SpellModifiers getModifiers() {
        return modifiers;
    }

    public Source getSource() {
        return source;
    }

    @Nullable
    public LivingEntity getCaster() {
        return caster;
    }

    public Level getLevel() {
        return level;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    @Nullable
    public Direction getDirection() {
        return direction;
    }

    public enum Source {
        WAND, SCROLL, COMMAND, NPC, DISPENSER, OTHER
    }

    public static class Pre extends SpellCastEvent implements ICancellableEvent {

        public Pre(Source source, AbstractSpell spell, LivingEntity caster, SpellModifiers modifiers) {
            super(source, spell, caster, modifiers);
        }

        public Pre(Source source, AbstractSpell spell, Level level, double x, double y, double z, Direction direction, SpellModifiers modifiers) {
            super(source, spell, level, x, y, z, direction, modifiers);
        }
    }

    public static class Post extends SpellCastEvent {

        public Post(Source source, AbstractSpell spell, LivingEntity caster, SpellModifiers modifiers) {
            super(source, spell, caster, modifiers);
        }

        public Post(Source source, AbstractSpell spell, Level level, double x, double y, double z, Direction direction, SpellModifiers modifiers) {
            super(source, spell, level, x, y, z, direction, modifiers);
        }

    }

    public static class Tick extends SpellCastEvent implements ICancellableEvent {

        private final int count;

        public Tick(Source source, AbstractSpell spell, LivingEntity caster, SpellModifiers modifiers, int count) {
            super(source, spell, caster, modifiers);
            this.count = count;
        }

        public Tick(Source source, AbstractSpell spell, Level level, double x, double y, double z, Direction direction, SpellModifiers modifiers, int count) {
            super(source, spell, level, x, y, z, direction, modifiers);
            this.count = count;
        }

        public int getCount() {
            return count;
        }

    }

    public static class Finish extends SpellCastEvent {

        private final int count;

        public Finish(Source source, AbstractSpell spell, LivingEntity caster, SpellModifiers modifiers, int count) {
            super(source, spell, caster, modifiers);
            this.count = count;
        }

        public Finish(Source source, AbstractSpell spell, Level level, double x, double y, double z, Direction direction, SpellModifiers modifiers, int count) {
            super(source, spell, level, x, y, z, direction, modifiers);
            this.count = count;
        }

        public int getCount() {
            return count;
        }
    }
}
