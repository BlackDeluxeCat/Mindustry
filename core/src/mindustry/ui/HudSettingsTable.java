package mindustry.ui;

import arc.struct.Seq;
import arc.Core;
import arc.func.Boolc;
import arc.scene.Element;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;

import static mindustry.Vars.*;
import static arc.Core.bundle;
import static arc.Core.settings;

public class HudSettingsTable extends Table{
    protected Seq<Setting> list = new Seq<>();
    private boolean expandList = false;

    public HudSettingsTable(){
        rebuild();
        right();
    }

    void rebuild(){
        clearChildren();


        button("Options", () -> {
            expandList = !expandList;
            rebuild();
        }).minSize(140f, 30f);
        
        if(expandList){
            list.clear();
            row();

            Table sets = new Table();
            checkPref("effects", true);
            sliderPref("effectScl",100,0,100,1, i -> i + "%");
            checkPref("bloom", true, val -> renderer.toggleBloom(val));
            sliderPref("minimapUnitTeamColorTransparency",100,0,100,1, i -> i + "%");
            checkPref("blockBars", true);
            checkPref("blockWeaponRange", true);
            checkPref("blockWeaponTargetLine", true);
            checkPref("unitHealthBar", true);
            checkPref("unitPathLine", true);
            checkPref("unitLogicMoveLine", true);
            checkPref("unitWeaponTargetLine", true);
            sliderPref("unitTransparency",100,0,100,1, i -> i + "%");
            sliderPref("unitLegTransparency",100,0,100,1, i -> i + "%");
            checkPref("keepPanViewInMove", true);
            
            for(Setting setting : list){
                setting.add(sets);
            }

            ScrollPane pane = pane(sp -> {
                sp.background(Styles.black3);
                sp.add(sets);
                return;
            }).maxSize(800f,300f).get();

            pane.update(() -> {
                Element e = Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true);
                if(e != null && e.isDescendantOf(pane)){
                    pane.requestScroll();
                }else if(pane.hasScroll()){
                    Core.scene.setScrollFocus(null);
                }
            });


        }

    }

    public interface StringProcessor{
        String get(int i);
    }

    public Seq<Setting> getSettings(){
        return list;
    }

    public void pref(Setting setting){
        list.add(setting);
        rebuild();
    }

    public void screenshakePref(){
        sliderPref("screenshake", bundle.get("setting.screenshake.name", "Screen Shake"), 4, 0, 8, i -> (i / 4f) + "x");
    }

    public SliderSettingF sliderPref(String name, String title, int def, int min, int max, StringProcessor s){
        return sliderPref(name, title, def, min, max, 1, s);
    }

    public SliderSettingF sliderPref(String name, String title, int def, int min, int max, int step, StringProcessor s){
        SliderSettingF res;
        list.add(res = new SliderSettingF(name, title, def, min, max, step, s));
        settings.defaults(name, def);
        return res;
    }

    public SliderSettingF sliderPref(String name, int def, int min, int max, StringProcessor s){
        return sliderPref(name, def, min, max, 1, s);
    }

    public SliderSettingF sliderPref(String name, int def, int min, int max, int step, StringProcessor s){
        SliderSettingF res;
        list.add(res = new SliderSettingF(name, bundle.get("setting." + name + ".name"), def, min, max, step, s));
        settings.defaults(name, def);
        return res;
    }

    public void checkPref(String name, String title, boolean def){
        list.add(new CheckSettingF(name, title, def, null));
        settings.defaults(name, def);
    }

    public void checkPref(String name, String title, boolean def, Boolc changed){
        list.add(new CheckSettingF(name, title, def, changed));
        settings.defaults(name, def);
    }

    /** Localized title. */
    public void checkPref(String name, boolean def){
        list.add(new CheckSettingF(name, bundle.get("setting." + name + ".name"), def, null));
        settings.defaults(name, def);
    }

    /** Localized title. */
    public void checkPref(String name, boolean def, Boolc changed){
        list.add(new CheckSettingF(name, bundle.get("setting." + name + ".name"), def, changed));
        settings.defaults(name, def);
    }

    public abstract static class Setting{
        public String name;
        public String title;

        public abstract void add(Table table);
    }

    public static class CheckSettingF extends Setting{
        boolean def;
        Boolc changed;

        CheckSettingF(String name, String title, boolean def, Boolc changed){
            this.name = name;
            this.title = title;
            this.def = def;
            this.changed = changed;
        }

        @Override
        public void add(Table table){
            CheckBox box = new CheckBox(title);
            box.update(() -> box.setChecked(settings.getBool(name)));

            box.changed(() -> {
                settings.put(name, box.isChecked());
                if(changed != null){
                    changed.get(box.isChecked());
                }
            });

            box.left();
            table.add(box).left().padTop(0.5f);
            table.row();
        }
    }

    public static class SliderSettingF extends Setting{
        int def;
        int min;
        int max;
        int step;
        StringProcessor sp;
        float[] values = null;

        SliderSettingF(String name, String title, int def, int min, int max, int step, StringProcessor s){
            this.name = name;
            this.title = title;
            this.def = def;
            this.min = min;
            this.max = max;
            this.step = step;
            this.sp = s;
        }

        @Override
        public void add(Table table){
            Slider slider = new Slider(min, max, step, false);

            slider.setValue(settings.getInt(name));
            if(values != null){
                slider.setSnapToValues(values, 1f);
            }

            Label label = new Label(title);

            slider.changed(() -> {
                settings.put(name, (int)slider.getValue());
                label.setText(title + ": " + sp.get((int)slider.getValue()));
            });

            slider.change();

            table.table(t -> {
                t.left().defaults().left();
                t.add(label).minWidth(label.getPrefWidth() / Scl.scl(1f) + 30);
                t.add(slider).width(100);
            }).left().padTop(1);

            table.row();
        }
    }

}