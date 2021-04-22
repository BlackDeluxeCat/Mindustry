package mindustry.maps.filters;

import arc.func.*;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.graphics.*;
import mindustry.maps.filters.FilterOption.*;
import mindustry.world.*;

public class PointSymmetryFilter extends GenerateFilter{
    private final Vec2 v1 = new Vec2(), v2 = new Vec2(), v3 = new Vec2(), v4 = new Vec2(), v1o = new Vec2();

    int angle = 45;
    int fourSides = 0;

    @Override
    public FilterOption[] options(){
        return Structs.arr(
        new SliderOption("angle", () -> angle, f -> angle = (int)f, 0, 360, 45),
        new SliderOption("fourSides", () -> fourSides, f -> fourSides = (int)f, 0, 1, 1)
        );
    }

    @Override
    protected void apply(){
        v1.trnsExact(angle - 90, 1f);
        v2.set(v1).scl(-1f);
        v4.trnsExact(angle - 180, 1f);

        v1o.set(v1);
        v1.add(in.width/2f - 0.5f, in.height/2f - 0.5f);
        v2.add(in.width/2f - 0.5f, in.height/2f - 0.5f);

        v3.set(in.x, in.y);
        if(fourSides == 0){
            if(!left(v1, v2, v3)){
                symmetry(v3, 0);
                Tile tile = in.tile(v3.x, v3.y);
                in.floor = tile.floor();
                if(!tile.block().synthetic()){
                    in.block = tile.block();
                }
                in.overlay = tile.overlay();
            }
        } else {
            Vec2 v3sub = new Vec2();
            v3sub.set(v3);
            v3sub.sub(in.width/2f - 0.5f,in.height/2f - 0.5f);
            if(!rightTop(v1o, v4, v3sub)){
                int corner = 0;
                if(leftTop(v1o, v4, v3sub)) corner = 1;
                if(leftButtom(v1o, v4, v3sub)) corner = 2;
                if(rightButtom(v1o, v4, v3sub)) corner = 3;
                symmetry(v3, corner);

                Tile tile = in.tile(v3.x, v3.y);
                in.floor = tile.floor();
                if(!tile.block().synthetic()){
                    in.block = tile.block();
                }
                in.overlay = tile.overlay();
            }
        }
    }

    @Override
    public void draw(Image image){
        super.draw(image);

        Vec2 vsize = Scaling.fit.apply(image.getDrawable().getMinWidth(), image.getDrawable().getMinHeight(), image.getWidth(), image.getHeight());
        float imageWidth = Math.max(vsize.x, vsize.y);
        float imageHeight = Math.max(vsize.y, vsize.x);

        float size = Math.max(image.getWidth() *2, image.getHeight()*2);
        Cons<Vec2> clamper = v -> v.clamp(
            image.x + image.getWidth()/2f - imageWidth/2f,
        image.y + image.getHeight()/2f - imageHeight/2f, image.y + image.getHeight()/2f + imageHeight/2f, image.x + image.getWidth()/2f + imageWidth/2f
        );

        clamper.get(Tmp.v1.trns(angle - 90, size).add(image.getWidth()/2f + image.x, image.getHeight()/2f + image.y));
        if(fourSides == 0){
            clamper.get(Tmp.v2.set(Tmp.v1).sub(image.getWidth()/2f + image.x, image.getHeight()/2f + image.y).rotate(180f).add(image.getWidth()/2f + image.x, image.getHeight()/2f + image.y));
        } else {
            clamper.get(Tmp.v2.set(Tmp.v1).sub(image.getWidth()/2f + image.x, image.getHeight()/2f + image.y).rotate(-90f).add(image.getWidth()/2f + image.x, image.getHeight()/2f + image.y));
        }

        Lines.stroke(Scl.scl(3f), Pal.accent);
        Lines.line(Tmp.v1.x, Tmp.v1.y, image.getWidth()/2f + image.x, image.getHeight()/2f + image.y);
        Lines.line(Tmp.v2.x, Tmp.v2.y, image.getWidth()/2f + image.x, image.getHeight()/2f + image.y);
        Draw.reset();
    }

    void symmetry(Vec2 t2o, int c){
        if(fourSides == 0 || in.width != in.height){
            t2o.x = in.width - t2o.x - 1;
            t2o.y = in.height - t2o.y - 1;
        } else {
            float orix, oriy;
            if(c == 1){
                //rotate -90
                orix = t2o.x;
                oriy = t2o.y;
                t2o.x = oriy;
                t2o.y = in.width - orix - 1;
                //t2o.x = t2o.y;
                //t2o.y = in.width - t2o.x - 1;
            }
            if(c == 2){
                t2o.x = in.width - t2o.x - 1;
                t2o.y = in.height - t2o.y - 1;
            }
            if(c == 3){
                //rotate -180
                t2o.x = in.width - t2o.x - 1;
                t2o.y = in.height - t2o.y - 1;
                //rotate -90
                orix = t2o.x;
                oriy = t2o.y;
                t2o.x = oriy;
                t2o.y = in.width - orix - 1;
            }
        }

    }
    /*corners
            /\
            ||vyc
         1  ||  0
            ||
            ||
    =================>vxc
         2  ||  3
            ||
            ||
    */
    boolean rightTop(Vec2 vyc, Vec2 vxc, Vec2 v){
        return ((vyc.x * v.x + vyc.y * v.y) >= 0 && (vxc.x * v.x + vxc.y * v.y) >= 0 );
    }
    boolean rightButtom(Vec2 vyc, Vec2 vxc, Vec2 v){
        return ((vyc.x * v.x + vyc.y * v.y) < 0 && (vxc.x * v.x + vxc.y * v.y) >= 0 );
    }
    boolean leftTop(Vec2 vyc, Vec2 vxc, Vec2 v){
        return ((vyc.x * v.x + vyc.y * v.y) >= 0 && (vxc.x * v.x + vxc.y * v.y) < 0 );
    }
    boolean leftButtom(Vec2 vyc, Vec2 vxc, Vec2 v){
        return ((vyc.x * v.x + vyc.y * v.y) < 0 && (vxc.x * v.x + vxc.y * v.y) < 0 );
    }
    boolean left(Vec2 a, Vec2 b, Vec2 c){
        return ((b.x - a.x)*(c.y - a.y) - (b.y - a.y)*(c.x - a.x)) > 0;
    }
}
