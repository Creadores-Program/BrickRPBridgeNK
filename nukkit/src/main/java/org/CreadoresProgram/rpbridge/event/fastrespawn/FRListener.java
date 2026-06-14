package org.CreadoresProgram.rpbridge.event.fastrespawn;
//Original Plugin FastRespawn by PetteriM1: https://github.com/PetteriM1/FastRespawn

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityLiving;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByBlockEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.PlayerDeathEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.lang.TranslationContainer;
import cn.nukkit.level.ParticleEffect;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.network.protocol.EntityEventPacket;
import cn.nukkit.network.protocol.LevelEventPacket;
import cn.nukkit.potion.Effect;

import org.CreadoresProgram.rpbridge.Main;
import org.CreadoresProgram.rpbridge.data.PlayerRP;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FRListener implements Listener {
    private Main main;
    public FRListener(){
        this.main = Main.getInstance();
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void handleDeath(EntityDamageEvent e) {
        if (e.getEntity() instanceof PlayerRP) {
            Player p = (Player) e.getEntity();

            if (p.getHealth() - e.getFinalDamage() < 1.0f) {
                if (e.getCause() != EntityDamageEvent.DamageCause.VOID && e.getCause() != EntityDamageEvent.DamageCause.SUICIDE) {
                    boolean totem = false;
                    if (p.getOffhandInventory().getItem(0).getId() == ItemID.TOTEM) {
                        p.getOffhandInventory().clear(0);
                        totem = true;
                    } else if (p.getInventory().getItemInHand().getId() == ItemID.TOTEM) {
                        p.getInventory().clear(p.getInventory().getHeldItemIndex());
                        totem = true;
                    }
                    if (totem) {
                        p.getLevel().addLevelEvent(p, LevelEventPacket.EVENT_SOUND_TOTEM);
                        p.getLevel().addParticleEffect(p, ParticleEffect.TOTEM);

                        p.extinguish();
                        p.removeAllEffects();
                        p.setHealth(1);

                        p.addEffect(Effect.getEffect(Effect.REGENERATION).setDuration(800).setAmplifier(1));
                        p.addEffect(Effect.getEffect(Effect.FIRE_RESISTANCE).setDuration(800));
                        p.addEffect(Effect.getEffect(Effect.ABSORPTION).setDuration(100).setAmplifier(1));

                        EntityEventPacket pk = new EntityEventPacket();
                        pk.eid = p.getId();
                        pk.event = EntityEventPacket.CONSUME_TOTEM;
                        p.dataPacket(pk);

                        e.setCancelled(true);
                        return;
                    }
                }

                String msg = "";
                List<String> params = new ArrayList<>();
                params.add(p.getDisplayName());

                switch (e.getCause()) {
                    case ENTITY_ATTACK:
                        if (e instanceof EntityDamageByEntityEvent) {
                            Entity ent = ((EntityDamageByEntityEvent) e).getDamager();
                            if (ent instanceof Player) {
                                msg = "death.attack.player";
                                params.add(((Player) ent).getDisplayName());
                                break;
                            } else if (ent instanceof EntityLiving) {
                                msg = "death.attack.mob";
                                params.add(!Objects.equals(ent.getNameTag(), "") ? ent.getNameTag() : ent.getName());
                                break;
                            } else {
                                params.add("Unknown");
                            }
                        }
                        break;
                    case PROJECTILE:
                        if (e instanceof EntityDamageByEntityEvent) {
                            Entity ent = ((EntityDamageByEntityEvent) e).getDamager();
                            if (ent instanceof Player) {
                                msg = "death.attack.arrow";
                                params.add(((Player) ent).getDisplayName());
                            } else if (ent instanceof EntityLiving) {
                                msg = "death.attack.arrow";
                                params.add(!Objects.equals(ent.getNameTag(), "") ? ent.getNameTag() : ent.getName());
                                break;
                            } else {
                                params.add("Unknown");
                            }
                        }
                        break;
                    case VOID:
                        msg = "death.attack.outOfWorld";
                        break;
                    case FALL:
                        if (e.getFinalDamage() > 2) {
                            msg = "death.fell.accident.generic";
                            break;
                        }
                        msg = "death.attack.fall";
                        break;
                    case SUFFOCATION:
                        msg = "death.attack.inWall";
                        break;
                    case LAVA:
                        msg = "death.attack.lava";
                        break;
                    case FIRE:
                        msg = "death.attack.onFire";
                        break;
                    case FIRE_TICK:
                        msg = "death.attack.inFire";
                        break;
                    case DROWNING:
                        msg = "death.attack.drown";
                        break;
                    case CONTACT:
                        if (e instanceof EntityDamageByBlockEvent) {
                            if (((EntityDamageByBlockEvent) e).getDamager().getId() == Block.CACTUS) {
                                msg = "death.attack.cactus";
                            }
                        }
                        break;
                    case BLOCK_EXPLOSION:
                    case ENTITY_EXPLOSION:
                        if (e instanceof EntityDamageByEntityEvent) {
                            Entity ent = ((EntityDamageByEntityEvent) e).getDamager();
                            if (ent instanceof Player) {
                                msg = "death.attack.explosion.player";
                                params.add(((Player) ent).getDisplayName());
                            } else if (ent instanceof EntityLiving) {
                                msg = "death.attack.explosion.player";
                                params.add(!Objects.equals(ent.getNameTag(), "") ? ent.getNameTag() : ent.getName());
                                break;
                            } else {
                                msg = "death.attack.explosion";
                            }
                        } else {
                            msg = "death.attack.explosion";
                        }
                        break;
                    case MAGIC:
                        msg = "death.attack.magic";
                        break;
                    case HUNGER:
                        msg = "death.attack.starve";
                        break;
                    default:
                        msg = "death.attack.generic";
                        break;
                }

                PlayerDeathEvent ev = new PlayerDeathEvent(p, p.getDrops(), new TranslationContainer(msg, params.toArray(new String[0])), p.getExperienceLevel());
                this.main.getServer().getPluginManager().callEvent(ev);

                p.setHealth(p.getMaxHealth());

                p.getFoodData().setLevel(p.getFoodData().getMaxLevel());

                p.removeAllEffects();

                p.extinguish();

                for (Item item : p.getDrops()) {
                    if (item.getEnchantment(Enchantment.ID_VANISHING_CURSE) == null) {
                        p.getLevel().dropItem(p, item, null, true, 40);
                    }
                }

                Item cursor = p.getCursorInventory().getItem(0);
                if (cursor.getEnchantment(Enchantment.ID_VANISHING_CURSE) == null) {
                    p.getLevel().dropItem(p, cursor, null, true, 40);
                }

                p.getInventory().clearAll();
                p.getCursorInventory().clearAll();

                if (p.isSurvival() || p.isAdventure()) {
                    NukkitRandom rand = new NukkitRandom();
                    int exp = p.getExperience() * 7;
                    if (exp > 100) exp = 100;
                    int add = 1;

                    for (int i = 1; i < exp; i += add) {
                        p.getLevel().dropExpOrb(p, add);
                        add = rand.nextRange(1, 3);
                    }
                }

                p.setExperience(0, 0);

                this.main.getServer().broadcast(ev.getDeathMessage(), Server.BROADCAST_CHANNEL_USERS);

                p.teleport(p.getSpawn(), null);
                e.setCancelled(true);
            }
        }
    }
}