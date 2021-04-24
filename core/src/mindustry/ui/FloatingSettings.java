package arc.scene.ui;

import arc.struct.Seq;
import arc.func.Boolc;
import arc.func.Cons;
import arc.scene.ui.*;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.Scl;

import static arc.Core.bundle;
import static arc.Core.settings;

public class FloatingSettings extends Table{
    protected Seq<Setting> list = new Seq<>();
    protected Cons<FloatingSettings> rebuilt;

    public FloatingSettings(){
        right();
    }

    public FloatingSettings(Cons<FloatingSettings> rebuilt){
        this.rebuilt = rebuilt;
        right();
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
        rebuild();
        return res;
    }

    public SliderSettingF sliderPref(String name, int def, int min, int max, StringProcessor s){
        return sliderPref(name, def, min, max, 1, s);
    }

    public SliderSettingF sliderPref(String name, int def, int min, int max, int step, StringProcessor s){
        SliderSettingF res;
        list.add(res = new SliderSettingF(name, bundle.get("setting." + name + ".name"), def, min, max, step, s));
        settings.defaults(name, def);
        rebuild();
        return res;
    }

    public void checkPref(String name, String title, boolean def){
        list.add(new CheckSettingF(name, title, def, null));
        settings.defaults(name, def);
        rebuild();
    }

    public void checkPref(String name, String title, boolean def, Boolc changed){
        list.add(new CheckSettingF(name, title, def, changed));
        settings.defaults(name, def);
        rebuild();
    }

    /** Localized title. */
    public void checkPref(String name, boolean def){
        list.add(new CheckSettingF(name, bundle.get("setting." + name + ".name"), def, null));
        settings.defaults(name, def);
        rebuild();
    }

    /** Localized title. */
    public void checkPref(String name, boolean def, Boolc changed){
        list.add(new CheckSettingF(name, bundle.get("setting." + name + ".name"), def, changed));
        settings.defaults(name, def);
        rebuild();
    }

    void rebuild(){
        clearChildren();

        for(Setting setting : list){
            setting.add(this, 1f);
        }

        if(rebuilt != null) rebuilt.get(this);
    }

    public abstract static class Setting{
        public String name;
        public String title;

        public abstract void add(FloatingSettings table, float scale);
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
        public void add(FloatingSettings table, float scale){
            CheckBox box = new CheckBox(title);
            box.setScale(scale);
            box.update(() -> box.setChecked(settings.getBool(name)));

            box.changed(() -> {
                settings.put(name, box.isChecked);
                if(changed != null){
                    changed.get(box.isChecked);
                }
            });

            box.left();
            table.add(box).left().padTop(1f);
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
        public void add(FloatingSettings table, float scale){
            Slider slider = new Slider(min, max, step, false);

            slider.setValue(settings.getInt(name));
            slider.setScale(scale);
            if(values != null){
                slider.setSnapToValues(values, 1f);
            }

            Label label = new Label(title);
            label.setScale(scale);

            slider.changed(() -> {
                settings.put(name, (int)slider.getValue());
                label.setText(title + ": " + sp.get((int)slider.getValue()));
            });

            slider.change();

            table.table(t -> {
                t.left().defaults().left();
                t.add(label).minWidth(label.getPrefWidth() / Scl.scl(2f) + 50);
                t.add(slider).width(100);
            }).left().padTop(1);

            table.row();
        }
    }

}