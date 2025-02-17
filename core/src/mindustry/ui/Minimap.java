package mindustry.ui;

import arc.*;
import arc.graphics.g2d.*;
import arc.input.*;
import arc.math.Mathf;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.ui.layout.*;
import arc.util.Log;
import mindustry.gen.*;
import mindustry.input.DesktopInput;

import static mindustry.Vars.*;

public class Minimap extends Table{


    public Minimap(){
        background(Tex.pane);
        float margin = 5f;
        this.touchable = Touchable.enabled;

        add(new Element(){
            {
                setSize(Scl.scl((float)getMinimapSize()));
            }

            @Override
            public void act(float delta){
                setPosition(Scl.scl(margin), Scl.scl(margin));

                super.act(delta);
            }

            @Override
            public void draw(){
                if(renderer.minimap.getRegion() == null) return;
                if(!clipBegin()) return;

                Draw.rect(renderer.minimap.getRegion(), x + width / 2f, y + height / 2f, width, height);

                if(renderer.minimap.getTexture() != null){
                    renderer.minimap.drawEntities(x, y, width, height, 0.75f, false);
                }

                clipEnd();
            }
        }).size((float)getMinimapSize());

        margin(margin);

        addListener(new InputListener(){
            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountx, float amounty){
                renderer.minimap.zoomBy(amounty);
                return true;
            }
        });

        addListener(new ClickListener(){
            {
                tapSquareSize = Scl.scl(11f);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button){
                if(inTapSquare()){
                    super.touchUp(event, x, y, pointer, button);
                }else{
                    pressed = false;
                    pressedPointer = -1;
                    pressedButton = null;
                    cancelled = false;
                }
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer){
                if(!inTapSquare(x, y)){
                    invalidateTapSquare();
                }
                super.touchDragged(event, x, y, pointer);

                if(mobile){
                    float max = Math.min(world.width(), world.height()) / 16f / 2f;
                    renderer.minimap.setZoom(1f + y / height * (max - 1f));
                }
            }

            @Override
            public void clicked(InputEvent event, float x, float y){
                //ui.minimapfrag.toggle();
                if(control.input instanceof DesktopInput){
                    try{
                        DesktopInput inp = (DesktopInput)control.input;
                        inp.panPosition.set(
                            ((x / width - 0.5f) * 2f * 16f * renderer.minimap.getZoom() + Mathf.clamp(Core.camera.position.x / tilesize, 16f * renderer.minimap.getZoom(), world.width() - 16f * renderer.minimap.getZoom())) * tilesize - player.x, 
                            ((y / height - 0.5f) * 2f * 16f * renderer.minimap.getZoom() + Mathf.clamp(Core.camera.position.y / tilesize, 16f * renderer.minimap.getZoom(), world.height() - 16f * renderer.minimap.getZoom())) * tilesize - player.y);
                        inp.panning = true;
                    }catch(Exception e){
                        Log.err("Minimap", e);
                    }
                }
            }
        });

        update(() -> {

            Element e = Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true);
            if(e != null && e.isDescendantOf(this)){
                requestScroll();
            }else if(hasScroll()){
                Core.scene.setScrollFocus(null);
            }
        });
    }
}
