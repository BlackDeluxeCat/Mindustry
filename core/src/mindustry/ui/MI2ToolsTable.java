package mindustry.ui;

import static mindustry.Vars.*;

import arc.graphics.*;
import arc.math.Mathf;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.game.Teams.*;
import mindustry.gen.*;

public class MI2ToolsTable extends Table{
    public int waveOffset = 0;
    private float fontScl = 0.6f;
    private boolean show = true;

    public MI2ToolsTable(){
        rebuild();
    }

    public void rebuild(){
        clear();
        left();
        button("<\nM\nI\n2\n \nT\no\no\nl\ns\n<", () -> {
            show = !show;
            rebuild();
        }).left().width(36f * (0.5f + fontScl * 0.5f)).fillY().get().getLabel().setFontScale(fontScl * 1.2f);

        if(show){
            table(Styles.black8, body -> {
                /* map rules 1 / game stats */
                body.table(line1 -> {
                    line1.table(t -> {
                        t.label(() -> "" + Iconc.statusBurning).get().setColor((state.rules.fire?new Color(1f,1f,1f):new Color(1f,0.3f,0.3f,0.5f)));
                        t.label(() -> "" + Iconc.itemBlastCompound).get().setColor((state.rules.damageExplosions?new Color(1f,1f,1f):new Color(1f,0.3f,0.3f,0.5f)));
                        t.label(() -> "" + Iconc.blockThoriumReactor).get().setColor((state.rules.reactorExplosions?new Color(1f,1f,1f):new Color(1f,0.3f,0.3f,0.5f)));
                        t.label(() -> "" + Iconc.itemCopper).get().setColor((state.rules.unitAmmo?new Color(1f,1f,1f):new Color(1f,0.3f,0.3f,0.5f)));
                        t.label(() -> "" + Iconc.blockMicroProcessor).get().setColor((state.rules.logicUnitBuild?new Color(1f,1f,1f):new Color(1f,0.3f,0.3f,0.5f)));
                        t.label(() -> "" + Iconc.blockCoreShard).get().setColor((state.rules.unitCapVariable?new Color(1f,1f,1f):new Color(1f,0.3f,0.3f,0.5f)));
                        t.label(() -> "bUCap:" + state.rules.unitCap).colspan(2).get().setFontScale(fontScl);
                        t.row();
            
                        t.label(() -> "" + Iconc.blockIlluminator).get().setColor((state.rules.lighting?new Color(1f,1f,1f):new Color(1f,0.3f,0.3f,0.5f)));
                        t.label(() -> "" + Iconc.blockIncinerator).get().setColor((state.rules.coreIncinerates?new Color(1f,1f,1f):new Color(1f,0.3f,0.3f,0.5f)));
                        t.label(() -> "" + Iconc.paste).get().setColor((state.rules.schematicsAllowed?new Color(1f,1f,1f):new Color(1f,0.3f,0.3f,0.5f)));
                        t.label(() -> world != null ? (world.width() + "x" + world.height()):"ohno").colspan(3).get().setFontScale(fontScl);
                        t.add("MI2").get().setFontScale(fontScl);
                        t.add("a19").get().setFontScale(fontScl);
                    }).left();
            
                    line1.table(t -> {
                        t.label(() -> (control.saves.getCurrent() != null ? ("Time: " + control.saves.getCurrent().getPlayTime() + "\n"):"") + 
                        "Kill: " + state.stats.enemyUnitsDestroyed).pad(5f).get().setFontScale(fontScl);
                        t.label(() -> "Build: " + state.stats.buildingsBuilt + 
                        "\nDescons: " + state.stats.buildingsDeconstructed + 
                        "\nDestroy: " + state.stats.buildingsDestroyed).pad(5f).get().setFontScale(fontScl);
                    }).left();
                }).left();

                body.row();

                /* map rules 2 */
                body.table(t -> {
                    t.label(() -> "BHp ").get().setFontScale(fontScl);
                    t.label(() -> "BDmg ").get().setFontScale(fontScl);
                    t.label(() -> "UDmg ").get().setFontScale(fontScl);
                    t.label(() -> "BCost ").get().setFontScale(fontScl);
                    t.label(() -> "BSpd ").get().setFontScale(fontScl);
                    t.label(() -> "BRe ").get().setFontScale(fontScl);
                    t.label(() -> "USpd ").get().setFontScale(fontScl);
                    t.row();
            
                    t.label(() -> " " + (state.rules.blockHealthMultiplier)).get().setFontScale(fontScl);
                    t.label(() -> " " + (state.rules.blockDamageMultiplier)).get().setFontScale(fontScl);
                    t.label(() -> " " + (state.rules.unitDamageMultiplier)).get().setFontScale(fontScl);
                    t.label(() -> " " + (state.rules.buildCostMultiplier)).get().setFontScale(fontScl);
                    t.label(() -> " " + (state.rules.buildSpeedMultiplier)).get().setFontScale(fontScl);
                    t.label(() -> " " + (state.rules.deconstructRefundMultiplier)).get().setFontScale(fontScl);
                    t.label(() -> " " + (state.rules.unitBuildSpeedMultiplier)).get().setFontScale(fontScl);
                }).left();

                body.row();

                /** wave info */
                body.table(t -> {
                    t.label(() -> "Wave " + (state.wave + waveOffset)).get().setFontScale(fontScl);

                    t.button("<<", () -> {
                        waveOffset -= 10;
                        if(state.wave + waveOffset - 1 < 0) waveOffset = -state.wave + 1;
                    }).size(36, 36);

                    t.button("<", () -> {
                        waveOffset -= 1;
                        if(state.wave + waveOffset - 1 < 0) waveOffset = -state.wave + 1;
                    }).size(36, 36);

                    t.button("O", () -> {
                        waveOffset = 0;
                    }).size(36, 36);

                    t.button(">", () -> {
                        waveOffset += 1;
                    }).size(36, 36);

                    t.button(">>", () -> {
                        waveOffset += 10;
                    }).size(36, 36);
                }).left().visible(() -> show);

                body.row();

                body.table(t -> {
                    t.update(() -> {
                        t.clear();

                        int curInfoWave = state.wave - 1 + waveOffset;
                        for(SpawnGroup group : state.rules.spawns){
                            if(group.getSpawned(curInfoWave) > 0){
                                t.label(() -> group.type.emoji()); 
                            }
                        }
                        
                        t.row();
                        for(SpawnGroup group : state.rules.spawns){
                            if(group.getSpawned(curInfoWave) > 0){
                                Label l = t.label(() -> "" + group.getSpawned(curInfoWave) + "\n" + (int)group.getShield(curInfoWave))
                                .padLeft(2f).padRight(2f).get();
                                l.setAlignment(Align.center);
                                l.setFontScale(0.9f * fontScl);
                            }
                        }
                    });

                }).left();

                body.row();
                
                /**script buttons */
                body.table(t -> {
                    t.button(Icon.refreshSmall, () -> {
                        Call.sendChatMessage("/sync");
                    }).maxSize(40f, 40f);

                    t.button("BP", () -> {
                        if(!player.unit().canBuild() || state.rules.mode() == Gamemode.pvp) return;
                        int p = 0;
                        for(BlockPlan block : state.teams.get(player.team()).blocks){
                            if(Mathf.len(block.x - player.tileX(), block.y - player.tileY()) >= 100) continue;
                            p++;
                            if(p > 255) break;
                            player.unit().addBuild(new BuildPlan(block.x, block.y, block.rotation, content.block(block.block), block.config));
                        }
                    }).maxSize(40f, 40f);

                    //set ui fontsize
                    Slider slider = new Slider(0.1f, 1.5f, 0.05f, false);
                    slider.setValue(fontScl);
                    slider.released(() -> {fontScl = slider.getValue(); rebuild();});
                    t.add("Font Size").get().setFontScale(fontScl);
                    t.add(slider).left().fillX();

                }).left();
            }).left();
        }

    }

}