package mindustry.world.blocks.distribution;

import arc.graphics.g2d.Draw;
import arc.math.*;
import arc.util.*;
import arc.util.io.*;
import arc.math.geom.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.type.*;
import mindustry.world.*;

import static mindustry.Vars.*;

public class BufferedItemBridge extends ExtendingItemBridge{
    public final int timerAccept = timers++;

    public float speed = 40f;
    public int bufferCapacity = 50;

    public BufferedItemBridge(String name){
        super(name);
        hasPower = false;
        hasItems = true;
        canOverdrive = true;
    }

    public class BufferedItemBridgeBuild extends ExtendingItemBridgeBuild{
        ItemBuffer buffer = new ItemBuffer(bufferCapacity);

        @Override
        public void updateTransport(Building other){
            if(buffer.accepts() && items.total() > 0){
                buffer.accept(items.take());
            }

            Item item = buffer.poll(speed / timeScale);
            if(timer(timerAccept, 4 / timeScale) && item != null && other.acceptItem(this, item)){
                cycleSpeed = Mathf.lerpDelta(cycleSpeed, 4f, 0.05f);
                other.handleItem(this, item);
                buffer.remove();
            }else{
                cycleSpeed = Mathf.lerpDelta(cycleSpeed, 0f, 0.008f);
            }
        }

        @Override
        public void draw(){
            super.draw();

            Item[] bufferItems = buffer.getItems();
            float[] bufferTimes = buffer.getTimes();
            Tile other = world.tile(link);

            float begx, begy, endx, endy;
            if(!linkValid(tile, other)){
                begx = x - tilesize / 2f;
                begy = y - tilesize / 2f;
                endx = x + tilesize / 2f;
                endy = y - tilesize / 2f;
            }else{
                int i = tile.absoluteRelativeTo(other.x, other.y);
                float ex = other.worldx() - x - Geometry.d4(i).x * tilesize / 2f,
                ey = other.worldy() - y - Geometry.d4(i).y * tilesize / 2f;
                float uptime = state.isEditor() ? 1f : this.uptime;
                ex *= uptime;
                ey *= uptime;

                begx = x + Geometry.d4(i).x * tilesize / 2f;
                begy = y + Geometry.d4(i).y * tilesize / 2f;
                endx = x + ex;
                endy = y + ey;
            }

            float loti = 0f;
            for(int idi = 0; idi < bufferItems.length; idi++){
                if(bufferItems[idi] != null){
                    Draw.rect(bufferItems[idi].icon(Cicon.medium), 
                    begx + ((endx - begx) / (float)bufferItems.length * Math.min(((Time.time - bufferTimes[idi]) / speed) * bufferCapacity, bufferCapacity - loti)), 
                    begy + ((endy - begy) / (float)bufferItems.length * Math.min(((Time.time - bufferTimes[idi]) / speed) * bufferCapacity, bufferCapacity - loti)), 4f, 4f);
                }
                loti++;
            }
        }

        @Override
        public void write(Writes write){
            super.write(write);
            buffer.write(write);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            buffer.read(read);
        }
    }
}
