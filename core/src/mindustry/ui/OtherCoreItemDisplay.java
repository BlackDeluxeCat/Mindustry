package mindustry.ui;

import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.UnitTypes;
import mindustry.core.*;
import mindustry.game.Teams;
import mindustry.type.Item;
import mindustry.type.UnitType;

import static mindustry.Vars.content;
import static mindustry.Vars.state;

//code by MI2 now
public class OtherCoreItemDisplay extends Table {
    private Seq<Teams.TeamData> teams = null, teams0 = null;

    private float lastUpd = 0f, fontScl = 0.8f;
    private int showTeams = 6, showStart = 0;
    private boolean show = true, showStat = true, showItem = true, showUnit = true;
    //TODO Stats

    private Table teamsTable;

    public OtherCoreItemDisplay() {
        teamsTable = new Table();
        rebuild();
    }

    void rebuild(){
        clear();
        clearChildren();

        button("<\n<\n<", () -> {
            show = !show;
        }).left().width(40f).fillY().get().left();

        table(t -> {
            t.table(buttons -> {
                buttons.button("+", () -> {
                    if(showTeams >= teams0.size) return;
                    showTeams += 1;
                    if(showTeams > 15) showTeams = 15;
                }).width(40f);

                buttons.row();

                buttons.button("-", () -> {
                    showTeams -= 1;
                    if(showTeams <= 0) showTeams = 1;
                }).width(40f);

                buttons.row();

                buttons.button(">", () -> {
                    showStart += 1;
                    if(showStart + showTeams > teams0.size) showStart = teams0.size - showTeams;
                }).width(40f);

                buttons.row();

                buttons.button("<", () -> {
                    showStart -= 1;
                    if(showStart < 0) showStart = 0;
                }).width(40f);

                buttons.row();

                buttons.button(content.items().get(0).emoji(), () -> {
                    showItem = !showItem;
                }).width(40f);

                buttons.row();

                buttons.button(UnitTypes.mono.emoji(), () -> {
                    showUnit = !showUnit;
                }).width(40f);
            }).left();
    
            teamsRebuild();

            t.add(teamsTable).left();

        }).visible(() -> show).left();

    }

    private void teamsRebuild(){
        teamsTable.clear();
        teamsTable.background(Styles.black6);
        teamsTable.update(() -> {
            if (Time.time - lastUpd > 120f){
                lastUpd = Time.time;
                teamsRebuild();
                return;
            }

            if (teams != state.teams.getActive()) {
                teamsRebuild();
                return;
            }
        });

        teamsTable.label(() -> "").get().setFontScale(fontScl);
        teams0 = Vars.state.teams.getActive();
        teams0.sort(team -> {
            float items = 0f;
            if(team.hasCore()){
                for(Item item : content.items()){
                    items -= team.core().items.get(item);
                }
            }
            return items;
        });

        teams = new Seq<>();
        for(int ii = 0; ii < showTeams; ii++){
            if(ii + showStart >= teams0.size) break;
            teams.add(teams0.get(ii + showStart));
        }

        /**name + cores + units */
        for (Teams.TeamData team : teams) {
            teamsTable.label(() -> "[#" + team.team.color + "]" + team.team.localized()).get().setFontScale(fontScl);
        }
        teamsTable.row();
        teamsTable.label(() -> Blocks.coreNucleus.emoji()).get().setFontScale(fontScl);
        for (Teams.TeamData team : teams) {
            teamsTable.label(() -> {
                return UI.formatAmount(team.cores.size);
            }).padRight(1).get().setFontScale(fontScl);
        }
        teamsTable.row();
        teamsTable.label(() -> UnitTypes.mono.emoji()).get().setFontScale(fontScl);
        for (Teams.TeamData team : teams) {
            teamsTable.label(() -> {
                return UI.formatAmount(team.units.size);
            }).padRight(1).get().setFontScale(fontScl);
        }
        teamsTable.row();

        if(showItem){
            boolean[] dispItems = new boolean[content.items().size];
            for(Item item : content.items()){            
                for(Teams.TeamData team : teams){
                    if(team.hasCore() && team.core().items.get(item) > 0) dispItems[content.items().indexOf(item)] = true;
                }
            }

            for(Item item : content.items()){            
                if(dispItems[content.items().indexOf(item)]){

                    teamsTable.label(() -> item.emoji()).padRight(5f).left().get().setFontScale(fontScl);
                    for(Teams.TeamData team : teams){
                        teamsTable.label(() -> "" + ((team.hasCore() && team.core().items.get(item) > 0) ? team.core().items.get(item) : "-")).get().setFontScale(fontScl);
                    }
                    teamsTable.row();

                }
            }
        }

        if(showUnit){
            boolean[] dispUnits = new boolean[content.units().size];
            for(UnitType unit : content.units()){            
                for(Teams.TeamData team : teams){
                    if(team.countType(unit) > 0) dispUnits[content.units().indexOf(unit)] = true;
                }
            }

            for(UnitType unit : content.units()){            
                if(dispUnits[content.units().indexOf(unit)]){

                    teamsTable.label(() -> unit.emoji()).padRight(5f).left().get().setFontScale(fontScl);
                    for(Teams.TeamData team : teams){
                        teamsTable.label(() -> "" + (team.countType(unit) > 0 ? team.countType(unit) : "-")).get().setFontScale(fontScl);
                    }
                    teamsTable.row();

                }
            }
        }
    }

    public void updateTeamList() {
        teams = null;
    }
}