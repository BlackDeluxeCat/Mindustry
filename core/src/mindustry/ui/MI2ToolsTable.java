package mindustry.ui;

import static mindustry.Vars.*;

import java.text.BreakIterator;

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

    public MI2ToolsTable(){
        rebuild();
    }

    void rebuild(){
        clear();
        left();

        table(t -> {
            t.label(() -> "" + Iconc.statusBurning).get().setColor((state.rules.fire?new Color(1f,1f,1f):new Color(1f,0.3f,0.3f,0.5f)));
            t.label(() -> "" + Iconc.itemBlastCompound).get().setColor((state.rules.damageExplosions?new Color(1f,1f,1f):new Color(1f,0.3f,0.3f,0.5f)));
            t.label(() -> "" + Iconc.blockThoriumReactor).get().setColor((state.rules.reactorExplosions?new Color(1f,1f,1f):new Color(1f,0.3f,0.3f,0.5f)));
            t.label(() -> "" + Iconc.itemCopper).get().setColor((state.rules.unitAmmo?new Color(1f,1f,1f):new Color(1f,0.3f,0.3f,0.5f)));
            t.label(() -> "" + Iconc.blockMicroProcessor).get().setColor((state.rules.logicUnitBuild?new Color(1f,1f,1f):new Color(1f,0.3f,0.3f,0.5f)));
            t.label(() -> "" + Iconc.blockCoreShard).get().setColor((state.rules.unitCapVariable?new Color(1f,1f,1f):new Color(1f,0.3f,0.3f,0.5f)));
            t.label(() -> "bUCap:" + state.rules.unitCap).colspan(2).get().setFontScale(0.6f);
            t.row();

            t.label(() -> "" + Iconc.blockIlluminator).get().setColor((state.rules.lighting?new Color(1f,1f,1f):new Color(1f,0.3f,0.3f,0.5f)));
            t.label(() -> "" + Iconc.blockIncinerator).get().setColor((state.rules.coreIncinerates?new Color(1f,1f,1f):new Color(1f,0.3f,0.3f,0.5f)));
            t.label(() -> "" + Iconc.paste).get().setColor((state.rules.schematicsAllowed?new Color(1f,1f,1f):new Color(1f,0.3f,0.3f,0.5f)));
            t.label(() -> world != null ? (world.width() + "x" + world.height()):"ohno").colspan(3).get().setFontScale(0.6f);
            t.add("MI2").get().setFontScale(0.6f);
            t.add("a19").get().setFontScale(0.6f);
        }).left();

        row();

        table(t -> {
            t.label(() -> "BHp ").get().setFontScale(0.6f);
            t.label(() -> "BDmg ").get().setFontScale(0.6f);
            t.label(() -> "UDmg ").get().setFontScale(0.6f);
            t.label(() -> "BCost ").get().setFontScale(0.6f);
            t.label(() -> "BSpd ").get().setFontScale(0.6f);
            t.label(() -> "BRe ").get().setFontScale(0.6f);
            t.label(() -> "USpd ").get().setFontScale(0.6f);
            t.row();
    
            t.label(() -> " " + (state.rules.blockHealthMultiplier)).get().setFontScale(0.6f);
            t.label(() -> " " + (state.rules.blockDamageMultiplier)).get().setFontScale(0.6f);
            t.label(() -> " " + (state.rules.unitDamageMultiplier)).get().setFontScale(0.6f);
            t.label(() -> " " + (state.rules.buildCostMultiplier)).get().setFontScale(0.6f);
            t.label(() -> " " + (state.rules.buildSpeedMultiplier)).get().setFontScale(0.6f);
            t.label(() -> " " + (state.rules.deconstructRefundMultiplier)).get().setFontScale(0.6f);
            t.label(() -> " " + (state.rules.unitBuildSpeedMultiplier)).get().setFontScale(0.6f);
        }).left();

        row();

        table(t -> {
            t.label(() -> "Wave " + (state.wave + waveOffset)).get().setFontScale(1f);

            t.button("<", () -> {
                waveOffset -= 1;
                if(waveOffset < 0) waveOffset = 0;
            }).maxSize(36f, 36f);

            t.button("○", () -> {
                waveOffset = 0;
            }).maxSize(36f, 36f);

            t.button(">", () -> {
                waveOffset += 1;
            }).maxSize(36f, 36f);
        }).left();

        row();

        table(t -> {
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
                        l.setFontScale(0.5f);
                    }
                }
            });

        }).left();

        row();

        table(t -> {
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

        }).left();

        row();

        table(t -> {
            t.label(() -> (control.saves.getCurrent() != null ? ("Time: " + control.saves.getCurrent().getPlayTime() + "\n"):"") + 
            "Built: " + state.stats.buildingsBuilt + 
            "\nDeconstructed: " + state.stats.buildingsDeconstructed + 
            "\nDestoryed: " + state.stats.buildingsDestroyed).get().setFontScale(0.6f);
        }).left().get().setScale(0.5f);

    }

}