package mindustry.world.consumers;

import arc.scene.ui.layout.*;
import arc.struct.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.meta.*;
import mindustry.world.meta.values.*;

public class ConsumeItems extends Consume{
    public final ItemStack[] items;

    public ConsumeItems(ItemStack[] items){
        this.items = items;
    }

    /** Mods.*/
    protected ConsumeItems(){
        this(ItemStack.empty);
    }

    @Override
    public void applyItemFilter(Bits filter){
        for(ItemStack stack : items){
            filter.set(stack.item.id);
        }
    }

    @Override
    public ConsumeType type(){
        return ConsumeType.item;
    }

    @Override
    public void build(Building tile, Table table){
        table.table(c -> {
            c.update(() -> {  
                c.clear();
                int i = 0;
                for(ItemStack stack : items){
                    c.add(new ReqImage(new ItemImage(stack.item.icon(Cicon.medium), stack.amount, tile.items.get(stack.item)),
                    () -> tile.items != null && tile.items.has(stack.item, stack.amount))).padRight(8);
                    if(++i % 4 == 0) table.row();
                }
            });

        }).left();
    }

    @Override
    public String getIcon(){
        return "icon-item";
    }

    @Override
    public void update(Building entity){

    }

    @Override
    public void trigger(Building entity){
        for(ItemStack stack : items){
            entity.items.remove(stack);
        }
    }

    @Override
    public boolean valid(Building entity){
        return entity.items != null && entity.items.has(items);
    }

    @Override
    public void display(Stats stats){
        stats.add(booster ? Stat.booster : Stat.input, new ItemListValue(items));
    }
}
