package mindustry.ui.dialogs;

import arc.Core;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Align;

public class AboutMI2Dialog extends BaseDialog{
    Seq<String> changelogs = new Seq<>();


    public AboutMI2Dialog() {
        super("@aboutmi2.button");

        shown(() -> {
            changelogs = Seq.with(Core.files.internal("changelog").readString("UTF-8").split("\n"));
            Core.app.post(this::setup);
        });

        shown(this::setup);
        onResize(this::setup);
    }

    void setup(){
        cont.clear();
        buttons.clear();

        Table about = new Table();
        ScrollPane pane = new ScrollPane(about);

        cont.add("MI2 Client by MyIndustry2 (我的工业2)");

        cont.row();

        for(String log : changelogs){
            about.add(log).left();
            about.row();
        }

        cont.add(pane).growX();

        cont.row();

        cont.add("Thanks to Anukes");

        addCloseButton();
    }
}
