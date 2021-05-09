package mindustry.ui;

import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.Cell;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Time;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.core.*;
import mindustry.game.Team;
import mindustry.game.Teams;
import mindustry.type.Item;
import mindustry.type.UnitType;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.modules.ItemModule;

import static mindustry.Vars.content;
import static mindustry.Vars.state;

//code by shugen002
public class OtherCoreItemDisplay extends Table {
    private Seq<Teams.TeamData> teams = null;
    private Seq<CoreBlock.CoreBuild> teamcores = null;
    public float lastUpd = 0f;

    public OtherCoreItemDisplay() {

        rebuild();
    }

    void rebuild() {
        clear();
        background(Styles.black6);
        update(() -> {
            if (Time.time - lastUpd > 120f){
                lastUpd = Time.time;
                rebuild();
                return;
            }
            if (teams != state.teams.getActive()) {
                rebuild();
                return;
            }
            int a = 0;
            for (Teams.TeamData team : teams) {
                if (team.hasCore()) {
                    if (a >= teamcores.size) {
                        rebuild();
                        return;
                    }
                    teamcores.set(a, team.core());
                    a++;
                }
            }
            if (a != teamcores.size) {
                rebuild();
                return;
            }

        });
        float fontSize = 0.8f;

        label(() -> "").get().setFontScale(fontSize);
        teams = Vars.state.teams.getActive();
        for (Teams.TeamData team : teams) {
            if (team.hasCore()) {
                label(() -> "[#" + team.team.color + "]" + team.team.localized()).get().setFontScale(fontSize);
            }
        }
        row();
        label(() -> Blocks.coreNucleus.emoji()).get().setFontScale(fontSize);
        for (Teams.TeamData team : teams) {
            if (team.hasCore()) {
                label(() -> {
                    return UI.formatAmount(team.cores.size);
                }).padRight(1).get().setFontScale(fontSize);
            }
        }
        row();
        label(() -> UnitTypes.mono.emoji()).get().setFontScale(fontSize);
        for (Teams.TeamData team : teams) {
            if (team.hasCore()) {
                label(() -> {
                    return UI.formatAmount(team.units.size);
                }).padRight(1).get().setFontScale(fontSize);
            }
        }
        row();
        teamcores = new Seq<CoreBlock.CoreBuild>();
        for (Teams.TeamData team : teams) {
            if (team.hasCore()) {
                teamcores.add(team.core());
            }
        }

        int[] dispItems = new int[content.items().size];
        int idi = 0;
        for (Item item : content.items()) {            
            for (int i = 0; i < teamcores.size; i++) {
                int finalI = i;
                int num = 0;
                try {
                    num = teamcores.get(finalI).items.get(item);
                }catch (Exception e){
                }
                finally {
                    if(num > 0){
                        dispItems[idi] = 1;
                    }
                    
                }
            }
            idi++;
        }
        idi = 0;
        for (Item item : content.items()) {
            if (dispItems[idi] == 1){
                label(() -> item.emoji()).padRight(3).left().get().setFontScale(fontSize);
                for (int i = 0; i < teamcores.size; i++) {
                    int finalI = i;
                    label(() -> {
                        int num = 0;
                        try {
                            num = teamcores.get(finalI).items.get(item);
                        }catch (Exception e){
                            Log.err(e);
                        }
                        finally {
                            return num+"";
                        }
                    }).get().setFontScale(fontSize);
                }
                row();
            }
            idi++;
        }


        idi = 0;
        int[] dispUnits = new int[content.units().size];
        for (UnitType unit : content.units()) {            
            for (int i = 0; i < teamcores.size; i++) {
                int finalI = i;
                int num = 0;
                try {
                    num = teamcores.get(finalI).team.data().countType(unit);
                }catch (Exception e){
                }
                finally {
                    if(num > 0){
                        //Log.infoTag("unit", idi + "");
                        dispUnits[idi] = 1;
                    }
                    
                }
            }
            idi++;
        }
        idi = 0;
        for (UnitType unit : content.units()) {  
            if (dispUnits[idi] == 1){
                label(() -> unit.emoji()).padRight(3).left().get().setFontScale(fontSize);
                for (int i = 0; i < teamcores.size; i++) {
                    int finalI = i;
                    label(() -> {
                        int num = 0;
                        try {
                            num = teamcores.get(finalI).team.data().countType(unit);
                        }catch (Exception e){
                            Log.err(e);
                        }
                        finally {
                            return num+"";
                        }
                    }).get().setFontScale(fontSize);
                }
                row();
            }
            idi++;
        }
        ;
    }

    public void updateTeamList() {
        teams = null;
    }
}