package mindustry.ui.fragments;

import arc.*;
import arc.graphics.g2d.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.input.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.net.*;
import mindustry.net.Packets.*;
import mindustry.ui.*;

import static mindustry.Vars.*;

public class PlayerListFragment extends Fragment{
    public Table content = new Table().marginRight(13f).marginLeft(13f);
    private boolean visible = false;
    private Interval timer = new Interval();
    private TextField sField;
    private Seq<Player> players = new Seq<>();

    @Override
    public void build(Group parent){
        content.name = "players";
        parent.fill(cont -> {
            cont.name = "playerlist";
            cont.visible(() -> visible);
            cont.update(() -> {
                if(!(net.active() && state.isGame())){
                    visible = false;
                    return;
                }

                if(visible && timer.get(20)){
                    rebuild();
                    content.pack();
                    content.act(Core.graphics.getDeltaTime());
                    //hacky
                    Core.scene.act(0f);
                }
            });

            cont.table(Tex.buttonTrans, pane -> {
                pane.label(() -> Core.bundle.format(Groups.player.size() == 1 ? "players.single" : "players", Groups.player.size()));
                pane.row();
                sField = pane.field(null, text -> {
                    rebuild();
                }).grow().pad(8).get();
                sField.name = "search";
                sField.setMaxLength(maxNameLength);
                sField.setMessageText(Core.bundle.format("players.search"));

                pane.row();
                pane.pane(content).grow().get().setScrollingDisabled(true, false);
                pane.row();

                pane.table(menu -> {
                    menu.defaults().growX().height(50f).fillY();
                    menu.name = "menu";

                    menu.button("@server.bans", ui.bans::show).disabled(b -> net.client());
                    menu.button("@server.admins", ui.admins::show).disabled(b -> net.client());
                    menu.button("@close", this::toggle);
                }).margin(0f).pad(10f).growX();

            }).touchable(Touchable.enabled).margin(14f).minWidth(720f);
        });

        rebuild();
    }

    public void rebuild(){
        content.clear();

        float h = 40f;
        float bs = (h) - 2f;
        boolean found = false;

        players.clear();
        Groups.player.copy(players);

        players.sort(Structs.comps(Structs.comparing(Player::team), Structs.comparingBool(p -> !p.admin)));

        for(var user : players){
            found = true;
            NetConnection connection = user.con;

            if(connection == null && net.server() && !user.isLocal()) return;
            if(sField.getText().length() > 0 && !user.name().toLowerCase().contains(sField.getText().toLowerCase()) && !Strings.stripColors(user.name().toLowerCase()).contains(sField.getText().toLowerCase())) return;

            Table button = new Table();
            button.left();
            button.margin(5).marginBottom(10);

            Table table = new Table(){
                @Override
                public void draw(){
                    super.draw();
                    Draw.color(Pal.gray);
                    Draw.alpha(parentAlpha);
                    Lines.stroke(Scl.scl(4f));
                    Lines.rect(x, y, width, height);
                    Draw.reset();
                }
            };
            table.margin(4);
            table.add(new Image(user.icon()).setScaling(Scaling.bounded)).grow();
            table.name = user.name();

            button.add(table).size(h);
            button.labelWrap("[" + user.id + "] ").minWidth(150f);
            button.image(Icon.admin).visible(() -> user.admin && !(!user.isLocal() && net.server())).size(bs).get().updateVisibility();
            button.labelWrap("[#" + user.color().toString().toUpperCase() + "]" + user.name()).width(320f).pad(10);
            button.add().grow();

            

            
            button.add().growY();

            button.table(t -> {
                t.defaults().size(bs);

                t.button(Icon.units, Styles.clearPartiali,()->{
                    if(control.input instanceof DesktopInput){
                        ((DesktopInput) control.input).panning = true;
                        ((DesktopInput) control.input).panPosition.set(user.x - player.x, user.y - player.y);
                    }
                    //Core.camera.position.lerpDelta(user.x, user.y,1f);
                }).visible(()-> !user.isLocal());

                if((net.server() || player.admin) && !user.isLocal() && (!user.admin || net.server())){
                    t.button(Icon.hammer, Styles.clearPartiali,
                    () -> ui.showConfirm("@confirm", Core.bundle.format("confirmban",  user.name()), () -> Call.adminRequest(user, AdminAction.ban)));
                    t.button(Icon.cancel, Styles.clearPartiali,
                    () -> ui.showConfirm("@confirm", Core.bundle.format("confirmkick",  user.name()), () -> Call.adminRequest(user, AdminAction.kick)));
                    //t.row();

                    t.button(Icon.admin, Styles.clearTogglePartiali, () -> {
                        if(net.client()) return;

                        String id = user.uuid();

                        if(netServer.admins.isAdmin(id, connection.address)){
                            ui.showConfirm("@confirm", Core.bundle.format("confirmunadmin",  user.name()), () -> netServer.admins.unAdminPlayer(id));
                        }else{
                            ui.showConfirm("@confirm", Core.bundle.format("confirmadmin",  user.name()), () -> netServer.admins.adminPlayer(id, user.usid()));
                        }
                    }).update(b -> b.setChecked(user.admin))
                        .disabled(b -> net.client())
                        .touchable(() -> net.client() ? Touchable.disabled : Touchable.enabled)
                        .checked(user.admin);

                    t.button(Icon.zoom, Styles.clearPartiali, () -> Call.adminRequest(user, AdminAction.trace));
                }else if(!user.isLocal() && !user.admin && net.client() && Groups.player.size() >= 2 && player.team() == user.team()){ //votekick
                    t.button(Icon.hammerSmall, Styles.clearPartiali,
                            () -> {
                                ui.showConfirm("@confirm", Core.bundle.format("confirmvotekick",  user.name()), () -> {
                                    Call.sendChatMessage("/votekick " + user.name());
                                });
                            });
                }

            }).padRight(12).size(bs*5 + 10f, bs);


            content.add(button).padBottom(-6).width(700f).maxHeight(h + 14);
            content.row();
            content.image().height(4f).color(state.rules.pvp ? user.team().color : Pal.gray).growX();
            content.row();
        }

        if(!found){
            content.add(Core.bundle.format("players.notfound")).padBottom(6).width(350f).maxHeight(h + 14);
        }

        content.marginBottom(5);
    }

    public void toggle(){
        visible = !visible;
        if(visible){
            rebuild();
        }else{
            Core.scene.setKeyboardFocus(null);
            sField.clearText();
        }
    }

}
