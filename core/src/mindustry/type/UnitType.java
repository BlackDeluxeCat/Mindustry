package mindustry.type;

import arc.*;
import arc.audio.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.ai.*;
import mindustry.ai.types.*;
import mindustry.annotations.Annotations.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.ctype.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.entities.units.*;
import mindustry.entities.comp.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.blocks.units.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import mindustry.world.meta.values.*;

import static mindustry.Vars.*;

import java.nio.IntBuffer;

public class UnitType extends UnlockableContent{
    public static final float shadowTX = -12, shadowTY = -13, outlineSpace = 0.01f;
    private static final Vec2 legOffset = new Vec2();
    //MI2 unit transparency
    private static float legTrans = 1f, unitTrans = 1f;

    /** If true, the unit is always at elevation 1. */
    public boolean flying;
    public Prov<? extends Unit> constructor;
    public Prov<? extends UnitController> defaultController = () -> !flying ? new GroundAI() : new FlyingAI();
    public float speed = 1.1f, boostMultiplier = 1f, rotateSpeed = 5f, baseRotateSpeed = 5f;
    public float drag = 0.3f, accel = 0.5f, landShake = 0f, rippleScale = 1f, riseSpeed = 0.08f, fallSpeed = 0.018f;
    public float health = 200f, range = -1, armor = 0f, maxRange = -1f;
    public float crashDamageMultiplier = 1f;
    public boolean targetAir = true, targetGround = true;
    public boolean faceTarget = true, rotateShooting = true, isCounted = true, lowAltitude = false, circleTarget = false;
    public boolean canBoost = false;
    public boolean destructibleWreck = true;
    public float groundLayer = Layer.groundUnit;
    public float payloadCapacity = 8;
    public float aimDst = -1f;
    public float buildBeamOffset = 3.8f;
    public int commandLimit = 8;
    public float visualElevation = -1f;
    public boolean allowLegStep = false;
    public boolean hovering = false;
    public boolean omniMovement = true;
    public Effect fallEffect = Fx.fallSmoke;
    public Effect fallThrusterEffect = Fx.fallSmoke;
    public Seq<Ability> abilities = new Seq<>();
    public BlockFlag targetFlag = BlockFlag.generator;

    public int legCount = 4, legGroupSize = 2;
    public float legLength = 10f, legSpeed = 0.1f, legTrns = 1f, legBaseOffset = 0f, legMoveSpace = 1f, legExtension = 0, legPairOffset = 0, legLengthScl = 1f, kinematicScl = 1f, maxStretch = 1.75f;
    public float legSplashDamage = 0f, legSplashRange = 5;
    public boolean flipBackLegs = true;

    public int ammoResupplyAmount = 10;
    public float ammoResupplyRange = 100f;

    public float mechSideSway = 0.54f, mechFrontSway = 0.1f;
    public float mechStride = -1f;
    public float mechStepShake = -1f;
    public boolean mechStepParticles = false;
    public Color mechLegColor = Pal.darkMetal;

    public int itemCapacity = -1;
    public int ammoCapacity = -1;
    public AmmoType ammoType = AmmoTypes.copper;
    public int mineTier = -1;
    public float buildSpeed = -1f, mineSpeed = 1f;
    public Sound mineSound = Sounds.minebeam;
    public float mineSoundVolume = 0.6f;

    /** This is a VERY ROUGH estimate of unit DPS. */
    public float dpsEstimate = -1;
    public float clipSize = -1;
    public boolean canDrown = true;
    public float engineOffset = 5f, engineSize = 2.5f;
    public float strafePenalty = 0.5f;
    public float hitSize = 6f;
    public float itemOffsetY = 3f;
    public float lightRadius = 60f, lightOpacity = 0.6f;
    public Color lightColor = Pal.powerLight;
    public boolean drawCell = true, drawItems = true, drawShields = true;
    public int trailLength = 3;
    public float trailX = 4f, trailY = -3f, trailScl = 1f;
    /** Whether the unit can heal blocks. Initialized in init() */
    public boolean canHeal = false;
    public boolean singleTarget = false;

    public ObjectSet<StatusEffect> immunities = new ObjectSet<>();
    public Sound deathSound = Sounds.bang;

    public Seq<Weapon> weapons = new Seq<>();
    public TextureRegion baseRegion, legRegion, region, shadowRegion, cellRegion,
        softShadowRegion, jointRegion, footRegion, legBaseRegion, baseJointRegion, outlineRegion;
    public TextureRegion[] wreckRegions;

    protected @Nullable ItemStack[] cachedRequirements;

    public UnitType(String name){
        super(name);

        constructor = EntityMapping.map(name);
    }

    public UnitController createController(){
        return defaultController.get();
    }

    public Unit create(Team team){
        Unit unit = constructor.get();
        unit.team = team;
        unit.setType(this);
        unit.ammo = ammoCapacity; //fill up on ammo upon creation
        unit.elevation = flying ? 1f : 0;
        unit.heal();
        return unit;
    }

    public Unit spawn(Team team, float x, float y){
        Unit out = create(team);
        out.set(x, y);
        out.add();
        return out;
    }

    public Unit spawn(float x, float y){
        return spawn(state.rules.defaultTeam, x, y);
    }

    public boolean hasWeapons(){
        return weapons.size > 0;
    }

    public void update(Unit unit){

    }

    public void landed(Unit unit){

    }
    //info % status
    public void displayStatus(Unit unit, Table bars){
        bars.add(new Bar(() -> "Hp:" + unit.healthMultiplier() + " Dmg:" + unit.damageMultiplier() + " Sp:" + unit.speedMultiplier(),() -> Pal.accent, () -> 1f));
        bars.row();
        bars.add(new Bar(() -> "S:" + unit.shield() + " A:" + unit.armor() + (unit.isBoss() ? " (Boss)" : ""), () -> Pal.accent, () -> 1f));
        bars.row();
        for(StatusEffect eff : Vars.content.statusEffects()){
            if(unit.hasEffect(eff)){
                bars.add(new Bar(() -> eff.emoji() + eff.name + ": " + ((unit.getEffectTime(eff) < 0f)? "Inf":(int)unit.getEffectTime(eff)/60f), () -> Pal.accent, () -> 1f));
                bars.row();
            }
        }
    }

    public void display(Unit unit, Table table){
        table.table(t -> {
            t.left();
            t.add(new Image(icon(Cicon.medium))).size(8 * 4).scaling(Scaling.fit);
            t.labelWrap("[#" + unit.team.color + "]" + localizedName).left().width(190f).padLeft(5);
        }).growX().left();
        table.row();

        table.table(bars -> {
            bars.defaults().growX().height(20f).pad(4);
            bars.add(new Bar(() -> {
                return unit.health + "/" + unit.maxHealth + " (" + (int)(100 * unit.health / unit.maxHealth) + "%)";
            }, () -> Pal.health, unit::healthf).blink(Color.white));
            bars.row();

            if(state.rules.unitAmmo){
                bars.add(new Bar(() -> ammoType.icon + " " + Core.bundle.format("stat.ammoDetail", unit.ammo, ammoCapacity), () -> ammoType.barColor, () -> unit.ammo / ammoCapacity));
                bars.row();
            }

            for(Ability ability : unit.abilities){
                ability.displayBars(unit, bars);
            }

            if(unit instanceof Payloadc payload){
                bars.add(new Bar("stat.payloadcapacity", Pal.items, () -> payload.payloadUsed() / unit.type().payloadCapacity));
                bars.row();

                var count = new float[]{-1};
                bars.table().update(t -> {
                    if(count[0] != payload.payloadUsed()){
                        payload.contentInfo(t, 8 * 2, 270);
                        count[0] = payload.payloadUsed();
                    }
                }).growX().left().height(0f).pad(0f);
                bars.row();
            }
            displayStatus(unit, bars);

        }).growX();

        if(unit.controller() instanceof LogicAI){
            table.row();
            table.add(Blocks.microProcessor.emoji() + " " + Core.bundle.get("units.processorcontrol")).growX().wrap().left();
            table.row();
            table.label(() -> Iconc.settings + " " + (long)unit.flag + "").color(Color.lightGray).growX().wrap().left();
        }
        
        table.row();
    }

    @Override
    public void getDependencies(Cons<UnlockableContent> cons){
        //units require reconstructors being researched
        for(Block block : content.blocks()){
            if(block instanceof Reconstructor r){
                for(UnitType[] recipe : r.upgrades){
                    //result of reconstruction is this, so it must be a dependency
                    if(recipe[1] == this){
                        cons.get(block);
                    }
                }
            }
        }

        for(ItemStack stack : researchRequirements()){
            cons.get(stack.item);
        }
    }

    @Override
    public void setStats(){
        Unit inst = constructor.get();

        stats.add(Stat.health, health);
        stats.add(Stat.armor, armor);
        stats.add(Stat.speed, speed);
        stats.add(Stat.itemCapacity, itemCapacity);
        stats.add(Stat.range, (int)(maxRange / tilesize), StatUnit.blocks);
        stats.add(Stat.commandLimit, commandLimit);

        if(abilities.any()){
            var unique = new ObjectSet<String>();

            for(Ability a : abilities){
                if(unique.add(a.localized())){
                    stats.add(Stat.abilities, a.localized());
                }
            }
        }

        stats.add(Stat.flying, flying);

        if(!flying){
            stats.add(Stat.canBoost, canBoost);
        }

        if(mineTier >= 1){
            stats.addPercent(Stat.mineSpeed, mineSpeed);
            stats.add(Stat.mineTier, new BlockFilterValue(b -> b instanceof Floor f && f.itemDrop != null && f.itemDrop.hardness <= mineTier && !f.playerUnmineable));
        }
        if(buildSpeed > 0){
            stats.addPercent(Stat.buildSpeed, buildSpeed);
        }
        if(inst instanceof Payloadc){
            stats.add(Stat.payloadCapacity, (payloadCapacity / (tilesize * tilesize)), StatUnit.blocksSquared);
        }

        if(weapons.any()){
            stats.add(Stat.weapons, new WeaponListValue(this, weapons));
        }
    }

    @CallSuper
    @Override
    public void init(){
        if(constructor == null) throw new IllegalArgumentException("no constructor set up for unit '" + name + "'");

        Unit example = constructor.get();

        //water preset
        if(example instanceof WaterMovec){
            canDrown = false;
            omniMovement = false;
            immunities.add(StatusEffects.wet);
        }

        singleTarget = weapons.size <= 1;

        if(itemCapacity < 0){
            itemCapacity = Math.max(Mathf.round((int)(hitSize * 4.3), 10), 10);
        }

        //set up default range
        if(range < 0){
            range = Float.MAX_VALUE;
            for(Weapon weapon : weapons){
                range = Math.min(range, weapon.bullet.range() + hitSize / 2f);
                maxRange = Math.max(maxRange, weapon.bullet.range() + hitSize / 2f);
            }
        }

        if(maxRange < 0){
            maxRange = 0f;
            maxRange = Math.max(maxRange, range);

            for(Weapon weapon : weapons){
                maxRange = Math.max(maxRange, weapon.bullet.range() + hitSize / 2f);
            }
        }

        if(weapons.isEmpty()){
            range = maxRange = miningRange;
        }

        if(mechStride < 0){
            mechStride = 4f + (hitSize -8f)/2.1f;
        }

        if(aimDst < 0){
            aimDst = weapons.contains(w -> !w.rotate) ? hitSize * 2f : hitSize / 2f;
        }

        if(mechStepShake < 0){
            mechStepShake = Mathf.round((hitSize - 11f) / 9f);
            mechStepParticles = hitSize > 15f;
        }

        canHeal = weapons.contains(w -> w.bullet.healPercent > 0f);

        //add mirrored weapon variants
        Seq<Weapon> mapped = new Seq<>();
        for(Weapon w : weapons){
            mapped.add(w);

            //mirrors are copies with X values negated
            if(w.mirror){
                Weapon copy = w.copy();
                copy.x *= -1;
                copy.shootX *= -1;
                copy.flipSprite = !copy.flipSprite;
                mapped.add(copy);

                //since there are now two weapons, the reload time must be doubled
                w.reload *= 2f;
                copy.reload *= 2f;

                w.otherSide = mapped.size - 1;
                copy.otherSide = mapped.size - 2;
            }
        }
        this.weapons = mapped;

        //dynamically create ammo capacity based on firing rate
        if(ammoCapacity < 0){
            float shotsPerSecond = weapons.sumf(w -> 60f / w.reload);
            //duration of continuous fire without reload
            float targetSeconds = 30;

            ammoCapacity = Math.max(1, (int)(shotsPerSecond * targetSeconds));
        }

        //calculate estimated DPS for one target based on weapons
        if(dpsEstimate < 0){
            dpsEstimate = weapons.sumf(w -> (w.bullet.estimateDPS() / w.reload) * w.shots * 60f);

            //suicide enemy
            if(weapons.contains(w -> w.bullet.killShooter)){
                //scale down DPS to be insignificant
                dpsEstimate /= 25f;
            }
        }
    }

    @CallSuper
    @Override
    public void load(){
        weapons.each(Weapon::load);
        region = Core.atlas.find(name);
        legRegion = Core.atlas.find(name + "-leg");
        jointRegion = Core.atlas.find(name + "-joint");
        baseJointRegion = Core.atlas.find(name + "-joint-base");
        footRegion = Core.atlas.find(name + "-foot");
        legBaseRegion = Core.atlas.find(name + "-leg-base", name + "-leg");
        baseRegion = Core.atlas.find(name + "-base");
        cellRegion = Core.atlas.find(name + "-cell", Core.atlas.find("power-cell"));
        softShadowRegion = Core.atlas.find("circle-shadow");
        outlineRegion = Core.atlas.find(name + "-outline");
        shadowRegion = icon(Cicon.full);

        wreckRegions = new TextureRegion[3];
        for(int i = 0; i < wreckRegions.length; i++){
            wreckRegions[i] = Core.atlas.find(name + "-wreck" + i);
        }
    }

    @Override
    public ItemStack[] researchRequirements(){
        if(cachedRequirements != null){
            return cachedRequirements;
        }

        ItemStack[] stacks = null;

        //calculate costs based on reconstructors or factories found
        Block rec = content.blocks().find(b -> b instanceof Reconstructor re && re.upgrades.contains(u -> u[1] == this));

        if(rec != null && rec.consumes.has(ConsumeType.item) && rec.consumes.get(ConsumeType.item) instanceof ConsumeItems ci){
            stacks = ci.items;
        }else{
            UnitFactory factory = (UnitFactory)content.blocks().find(u -> u instanceof UnitFactory uf && uf.plans.contains(p -> p.unit == this));
            if(factory != null){
                stacks = factory.plans.find(p -> p.unit == this).requirements;
            }
        }

        if(stacks != null){
            ItemStack[] out = new ItemStack[stacks.length];
            for(int i = 0; i < out.length; i++){
                out[i] = new ItemStack(stacks[i].item, UI.roundAmount((int)(Math.pow(stacks[i].amount, 1.1) * 50)));
            }

            cachedRequirements = out;

            return out;
        }

        return super.researchRequirements();
    }

    @Override
    public ContentType getContentType(){
        return ContentType.unit;
    }

    //region drawing

    public void draw(Unit unit){
        initUnitTransp();
        initLegTransp();

        Mechc mech = unit instanceof Mechc ? (Mechc)unit : null;
        float z = unit.elevation > 0.5f ? (lowAltitude ? Layer.flyingUnitLow : Layer.flyingUnit) : groundLayer + Mathf.clamp(hitSize / 4000f, 0, 0.01f);

        if(unit.controller().isBeingControlled(player.unit())){
            drawControl(unit);
        }

        if(unitTrans > 0f && (unit.isFlying() || visualElevation > 0)){
            Draw.z(Math.min(Layer.darkness, z - 1f));
            drawShadow(unit);
        }

        Draw.z(z - 0.02f);

        if(mech != null){
            if(unitTrans > 0) drawMech(mech);

            //side
            legOffset.trns(mech.baseRotation(), 0f, Mathf.lerp(Mathf.sin(mech.walkExtend(true), 2f/Mathf.PI, 1) * mechSideSway, 0f, unit.elevation));

            //front
            legOffset.add(Tmp.v1.trns(mech.baseRotation() + 90, 0f, Mathf.lerp(Mathf.sin(mech.walkExtend(true), 1f/Mathf.PI, 1) * mechFrontSway, 0f, unit.elevation)));

            unit.trns(legOffset.x, legOffset.y);
        }

        if(unit instanceof Legsc && legTrans > 0f){
            drawLegs((Unit & Legsc)unit);
        }

        Draw.z(Math.min(z - 0.01f, Layer.bullet - 1f));

        if(unit instanceof Payloadc && unitTrans > 0f){
            drawPayload((Unit & Payloadc)unit);
        }

        if(unitTrans > 0f){
            drawSoftShadow(unit);

            Draw.z(z - outlineSpace);

            drawOutline(unit);

            Draw.z(z);
        }

        if(engineSize > 0 && unitTrans > 0f) drawEngine(unit);
        if(unitTrans > 0f) drawBody(unit);
        if(drawCell && unitTrans > 0f) drawCell(unit);
        if(unitTrans > 0f) drawWeapons(unit);
        if(drawItems && unitTrans > 0f) drawItems(unit);
        drawLight(unit);

        if(unit.shieldAlpha > 0 && drawShields){
            drawShield(unit);
        }

        if(mech != null){
            unit.trns(-legOffset.x, -legOffset.y);
        }

        if(unit.abilities.size > 0){
            for(Ability a : unit.abilities){
                Draw.reset();
                a.draw(unit);
            }

            Draw.reset();
        }

        //display healthbar by MI2
        Draw.z(Layer.shields + 6f);
        if(Core.settings.getBool("unitHealthBar")){
            Draw.reset();
            Lines.stroke(4f);
            Draw.color(unit.team.color, 0.5f);
            Lines.line(unit.x - unit.hitSize() * 0.6f, unit.y + (unit.hitSize() / 2f), unit.x + unit.hitSize() * 0.6f, unit.y + (unit.hitSize() / 2f));
            Lines.stroke(2f);
            /*Draw.color(Pal.health, 0.3f);
            Lines.line(
                unit.x - unit.hitSize() * 0.6f, unit.y + (unit.hitSize() / 2f), 
                unit.x + unit.hitSize() * 0.6f, unit.y + (unit.hitSize() / 2f));
                */
            Draw.color(Pal.health, 0.8f);
            Lines.line(
                unit.x - unit.hitSize() * 0.6f, unit.y + (unit.hitSize() / 2f), 
                unit.x + unit.hitSize() * (Mathf.maxZero(unit.health) * 1.2f / unit.maxHealth - 0.6f), unit.y + (unit.hitSize() / 2f));
            Lines.stroke(2f);
            if(unit.shield > 0){
                for(int didgt = 1; didgt <= Mathf.digits((int)(unit.shield / unit.maxHealth)) + 1; didgt++){
                    Draw.color(Pal.shield, 0.8f);
                    float shieldAmountScale = unit.shield / (unit.maxHealth * Mathf.pow(10f, (float)didgt - 1f));
                    if(didgt > 1){
                        Lines.line(unit.x - unit.hitSize() * 0.6f, 
                        unit.y + (unit.hitSize() / 2f) + (float)didgt * 2f, 
                        unit.x + unit.hitSize() * ((Mathf.ceil((shieldAmountScale - Mathf.floor(shieldAmountScale)) * 10f) - 1f + 0.0001f) * 1.2f * (1f / 9f) - 0.6f), 
                        unit.y + (unit.hitSize() / 2f) + (float)didgt * 2f);
                        //(s-1)*(1/9)because line(0) will draw length of 1
                    } else {
                        Lines.line(unit.x - unit.hitSize() * 0.6f, 
                        unit.y + (unit.hitSize() / 2f) + (float)didgt * 2f, 
                        unit.x + unit.hitSize() * ((shieldAmountScale - Mathf.floor(shieldAmountScale) - 0.001f) * 1.2f - 0.6f), 
                        unit.y + (unit.hitSize() / 2f) + (float)didgt * 2f);
                    }
                }
            }
            Draw.reset();
            
            float index = 0f;
            for(StatusEffect eff : Vars.content.statusEffects()){
                if(unit.hasEffect(eff)){
                    float iconSize = Mathf.ceil(unit.hitSize() / 4f);
                    Draw.rect(eff.icon(Cicon.small), 
                    unit.x - unit.hitSize() * 0.6f + 0.5f * iconSize * Mathf.mod(index, 4f), 
                    unit.y + (unit.hitSize() / 2f) + 3f + iconSize * Mathf.floor(index / 4f), 
                    4f, 4f);
                    index++;
                }
            }
        }

        //display logicAI info by MI2
        if(unit.controller() instanceof LogicAI logicai){
            Draw.reset();
            if(Core.settings.getBool("unitLogicMoveLine") && Mathf.len(logicai.moveX - unit.x, logicai.moveY - unit.y) <= 1200f){
                Lines.stroke(1f);
                Draw.color(0.2f, 0.2f, 1f, 0.9f);
                Lines.dashLine(unit.x, unit.y, logicai.moveX, logicai.moveY, (int)(Mathf.len(logicai.moveX - unit.x, logicai.moveY - unit.y) / 8));
                Lines.dashCircle(logicai.moveX, logicai.moveY, logicai.moveRad);
                Draw.reset();
            }
            
            //logicai timers
            if(Core.settings.getBool("unitLogicTimerBars")){

                Lines.stroke(2f);
                Draw.color(Pal.heal);
                Lines.line(unit.x - (unit.hitSize() / 2f), unit.y - (unit.hitSize() / 2f), unit.x - (unit.hitSize() / 2f), unit.y + unit.hitSize() * (logicai.controlTimer / logicai.logicControlTimeout - 0.5f));

                Lines.stroke(2f);
                Draw.color(Pal.items);
                Lines.line(unit.x - (unit.hitSize() / 2f) - 1f, unit.y - (unit.hitSize() / 2f), unit.x - (unit.hitSize() / 2f) - 1f, unit.y + unit.hitSize() * (logicai.itemTimer / logicai.transferDelay - 0.5f));

                Lines.stroke(2f);
                Draw.color(Pal.items);
                Lines.line(unit.x - (unit.hitSize() / 2f) - 1.5f, unit.y - (unit.hitSize() / 2f), unit.x - (unit.hitSize() / 2f) - 1.5f, unit.y + unit.hitSize() * (logicai.payTimer / logicai.transferDelay - 0.5f));

                Draw.reset();
            }
        }

        //Pathfind Renderer
        if(Core.settings.getBool("unitPathLine") && Core.settings.getInt("unitPathLineLength") > 0){
            Draw.z(Layer.power - 4f);
            Tile tile = unit.tileOn();
            Draw.reset();
            for(int tileIndex = 1; tileIndex <= Core.settings.getInt("unitPathLineLength"); tileIndex++){
                Tile nextTile = pathfinder.getTargetTile(tile, pathfinder.getField(unit.team, unit.pathType(), (unit.team.data().command == UnitCommand.attack)? Pathfinder.fieldCore : Pathfinder.fieldRally));
                if(nextTile == null) break;
                Lines.stroke(Core.settings.getInt("unitPathLineStroke"));
                if(nextTile == tile){
                    Draw.color(unit.team.color, Color.black, Mathf.absin(Time.time, 4f, 1f));
                    Lines.poly(unit.x, unit.y, 6, unit.hitSize());
                    break;
                }
                Draw.color(unit.team.color, Color.lightGray, Mathf.absin(Time.time, 8f, 1f));
                Lines.dashLine(tile.worldx(), tile.worldy(), nextTile.worldx(), nextTile.worldy(), (int)(Mathf.len(nextTile.worldx() - tile.worldx(), nextTile.worldy() - tile.worldy()) / 4f));
                //Fill.poly(nextTile.worldx(), nextTile.worldy(), 4, tilesize - 2, 90);
                tile = nextTile;
            }
            Draw.reset();
        }

    }

    public <T extends Unit & Payloadc> void drawPayload(T unit){
        if(unit.hasPayload()){
            Payload pay = unit.payloads().first();
            pay.set(unit.x, unit.y, unit.rotation);
            pay.draw();
        }
    }

    public void drawShield(Unit unit){
        float alpha = unit.shieldAlpha();
        float radius = unit.hitSize() * 1.3f;
        Fill.light(unit.x, unit.y, Lines.circleVertices(radius), radius, Tmp.c1.set(Pal.shieldIn), Tmp.c2.set(Pal.shield).lerp(Color.white, Mathf.clamp(unit.hitTime() / 2f)).a(Pal.shield.a * alpha));
    }

    public void drawControl(Unit unit){
        Draw.z(Layer.groundUnit - 2);

        Draw.color(Pal.accent, Color.white, Mathf.absin(4f, 0.3f));
        Lines.poly(unit.x, unit.y, 4, unit.hitSize + 1.5f);

        Draw.reset();
    }

    public void drawShadow(Unit unit){
        Draw.color(Pal.shadow, 0.4f * unitTrans);
        float e = Math.max(unit.elevation, visualElevation);
        Draw.rect(shadowRegion, unit.x + shadowTX * e, unit.y + shadowTY * e, unit.rotation - 90);
        Draw.color();
    }

    public void drawSoftShadow(Unit unit){
        Draw.color(0, 0, 0, 0.4f * unitTrans); //transp!
        float rad = 1.6f;
        float size = Math.max(region.width, region.height) * Draw.scl;
        Draw.rect(softShadowRegion, unit, size * rad, size * rad);
        Draw.color();
    }

    public void drawItems(Unit unit){
        applyColor(unit);

        //draw back items
        if(unit.item() != null && unit.itemTime > 0.01f){
            float size = (itemSize + Mathf.absin(Time.time, 5f, 1f)) * unit.itemTime;

            Draw.mixcol(Pal.accent, Mathf.absin(Time.time, 5f, 0.5f));
            Draw.alpha(unitTrans);
            Draw.rect(unit.item().icon(Cicon.medium),
            unit.x + Angles.trnsx(unit.rotation + 180f, itemOffsetY),
            unit.y + Angles.trnsy(unit.rotation + 180f, itemOffsetY),
            size, size, unit.rotation);

            Draw.mixcol();
            Draw.alpha(unitTrans);
            
            Lines.stroke(1f, Pal.accent);
            Lines.circle(
            unit.x + Angles.trnsx(unit.rotation + 180f, itemOffsetY),
            unit.y + Angles.trnsy(unit.rotation + 180f, itemOffsetY),
            (3f + Mathf.absin(Time.time, 5f, 1f)) * unit.itemTime);

            if((Core.settings.getBool("unitItemAmountAlwaysOn") ? true : unit.isLocal()) && !renderer.pixelator.enabled()){
                Fonts.outline.draw(unit.stack.amount + "",
                unit.x + Angles.trnsx(unit.rotation + 180f, itemOffsetY),
                unit.y + Angles.trnsy(unit.rotation + 180f, itemOffsetY) - 3,
                Pal.accent, 0.25f * unit.itemTime / Scl.scl(1f), false, Align.center
                );
            }

            Draw.reset();
        }
    }

    public void drawEngine(Unit unit){
        if(!unit.isFlying()) return;

        float scale = unit.elevation;
        float offset = engineOffset/2f + engineOffset/2f*scale;

        if(unit instanceof Trailc){
            Trail trail = ((Trailc)unit).trail();
            trail.draw(unit.team.color, (engineSize + Mathf.absin(Time.time, 2f, engineSize / 4f) * scale) * trailScl);
        }

        Draw.color(unit.team.color, unitTrans);
        Fill.circle(
            unit.x + Angles.trnsx(unit.rotation + 180, offset),
            unit.y + Angles.trnsy(unit.rotation + 180, offset),
            (engineSize + Mathf.absin(Time.time, 2f, engineSize / 4f)) * scale
        );
        Draw.color(Color.white, unitTrans);
        Fill.circle(
            unit.x + Angles.trnsx(unit.rotation + 180, offset - 1f),
            unit.y + Angles.trnsy(unit.rotation + 180, offset - 1f),
            (engineSize + Mathf.absin(Time.time, 2f, engineSize / 4f)) / 2f  * scale
        );
        Draw.color();
    }

    public void drawWeapons(Unit unit){
        applyColor(unit);
        Draw.alpha(unitTrans); //

        for(WeaponMount mount : unit.mounts){
            Weapon weapon = mount.weapon;

            float rotation = unit.rotation - 90;
            float weaponRotation  = rotation + (weapon.rotate ? mount.rotation : 0);
            float recoil = -((mount.reload) / weapon.reload * weapon.recoil);
            float wx = unit.x + Angles.trnsx(rotation, weapon.x, weapon.y) + Angles.trnsx(weaponRotation, 0, recoil),
                wy = unit.y + Angles.trnsy(rotation, weapon.x, weapon.y) + Angles.trnsy(weaponRotation, 0, recoil);

            if(weapon.shadow > 0){
                Drawf.shadow(wx, wy, weapon.shadow, unitTrans);
            }

            if(weapon.outlineRegion.found()){
                float z = Draw.z();
                if(!weapon.top) Draw.z(z - outlineSpace);

                Draw.alpha(unitTrans); //
                Draw.rect(weapon.outlineRegion,
                wx, wy,
                weapon.outlineRegion.width * Draw.scl * -Mathf.sign(weapon.flipSprite),
                weapon.region.height * Draw.scl,
                weaponRotation);

                Draw.z(z);
            }

            //display target line for every weaponmount by MI2
            if(mount.aimX !=0 && mount.aimY != 0 && Core.settings.getBool("unitWeaponTargetLine") && Mathf.len(mount.aimX - wx, mount.aimY - wy) <= 1200f){
                Lines.stroke(1f);
                if(mount.shoot){
                    Draw.color(1f, 0.2f, 0.2f, 0.8f);
                    Lines.line(wx, wy, mount.aimX, mount.aimY);
                } else {
                    Draw.color(1f, 1f, 1f, 0.3f);
                    Lines.line(wx, wy, mount.aimX, mount.aimY);
                }
                Lines.dashCircle(mount.aimX, mount.aimY, 8);
                Draw.reset();

            }

            Draw.rect(weapon.region,
            wx, wy,
            weapon.region.width * Draw.scl * -Mathf.sign(weapon.flipSprite),
            weapon.region.height * Draw.scl,
            weaponRotation);

            if(weapon.heatRegion.found() && mount.heat > 0){
                Draw.color(weapon.heatColor, mount.heat);
                Draw.blend(Blending.additive);
                Draw.rect(weapon.heatRegion,
                wx, wy,
                weapon.heatRegion.width * Draw.scl * -Mathf.sign(weapon.flipSprite),
                weapon.heatRegion.height * Draw.scl,
                weaponRotation);
                Draw.blend();
                Draw.color();
            }
        }

        Draw.reset();
    }

    public void drawOutline(Unit unit){
        Draw.reset();
        Draw.alpha(unitTrans); //

        if(Core.atlas.isFound(outlineRegion)){
            Draw.rect(outlineRegion, unit.x, unit.y, unit.rotation - 90);
        }
    }

    public void drawBody(Unit unit){
        applyColor(unit);

        Draw.alpha(unitTrans); //

        Draw.rect(region, unit.x, unit.y, unit.rotation - 90);

        Draw.reset();
    }

    public void drawCell(Unit unit){
        applyColor(unit);

        Draw.color(cellColor(unit));
        Draw.alpha(unitTrans); //

        Draw.rect(cellRegion, unit.x, unit.y, unit.rotation - 90);
        Draw.reset();
    }

    public Color cellColor(Unit unit){
        return Tmp.c1.set(Color.black).lerp(unit.team.color, unit.healthf() + Mathf.absin(Time.time, Math.max(unit.healthf() * 5f, 1f), 1f - unit.healthf()));
    }

    public void drawLight(Unit unit){
        if(lightRadius > 0){
            Drawf.light(unit.team, unit.x, unit.y, lightRadius, lightColor, lightOpacity);
        }
    }

    public <T extends Unit & Legsc> void drawLegs(T unit){
        applyColor(unit);

        Leg[] legs = unit.legs();

        float ssize = footRegion.width * Draw.scl * 1.5f;
        float rotation = unit.baseRotation();

        for(Leg leg : legs){
            Drawf.shadow(leg.base.x, leg.base.y, ssize, legTrans);
        }

        //legs are drawn front first
        for(int j = legs.length - 1; j >= 0; j--){
            int i = (j % 2 == 0 ? j/2 : legs.length - 1 - j/2);
            Leg leg = legs[i];
            float angle = unit.legAngle(rotation, i);
            boolean flip = i >= legs.length/2f;
            int flips = Mathf.sign(flip);

            Vec2 position = legOffset.trns(angle, legBaseOffset).add(unit);

            Tmp.v1.set(leg.base).sub(leg.joint).inv().setLength(legExtension);

            if(leg.moving && visualElevation > 0){
                float scl = visualElevation;
                float elev = Mathf.slope(1f - leg.stage) * scl;
                Draw.color(Pal.shadow);
                Draw.alpha(legTrans); //
                Draw.rect(footRegion, leg.base.x + shadowTX * elev, leg.base.y + shadowTY * elev, position.angleTo(leg.base));
                Draw.color();
            }
            Draw.alpha(legTrans); //

            Draw.rect(footRegion, leg.base.x, leg.base.y, position.angleTo(leg.base));

            Lines.stroke(legRegion.height * Draw.scl * flips);
            Lines.line(legRegion, position.x, position.y, leg.joint.x, leg.joint.y, false);

            Lines.stroke(legBaseRegion.height * Draw.scl * flips);
            Lines.line(legBaseRegion, leg.joint.x + Tmp.v1.x, leg.joint.y + Tmp.v1.y, leg.base.x, leg.base.y, false);

            if(jointRegion.found()){
                Draw.rect(jointRegion, leg.joint.x, leg.joint.y);
            }

            if(baseJointRegion.found()){
                Draw.rect(baseJointRegion, position.x, position.y, rotation);
            }
        }

        if(baseRegion.found()){
            Draw.rect(baseRegion, unit.x, unit.y, rotation - 90);
        }

        Draw.reset();
    }

    public void drawMech(Mechc mech){
        Unit unit = (Unit)mech;

        Draw.reset();

        float e = unit.elevation;

        float sin = Mathf.lerp(Mathf.sin(mech.walkExtend(true), 2f / Mathf.PI, 1f), 0f, e);
        float extension = Mathf.lerp(mech.walkExtend(false), 0, e);
        float boostTrns = e * 2f;

        Floor floor = unit.isFlying() ? Blocks.air.asFloor() : unit.floorOn();

        if(floor.isLiquid){
            Draw.color(Color.white, floor.mapColor, 0.5f);
        }

        for(int i : Mathf.signs){
            Draw.mixcol(Tmp.c1.set(mechLegColor).lerp(Color.white, Mathf.clamp(unit.hitTime)), Math.max(Math.max(0, i * extension / mechStride), unit.hitTime));
            Draw.alpha(unitTrans); //

            Draw.rect(legRegion,
            unit.x + Angles.trnsx(mech.baseRotation(), extension * i - boostTrns, -boostTrns*i),
            unit.y + Angles.trnsy(mech.baseRotation(), extension * i - boostTrns, -boostTrns*i),
            legRegion.width * i * Draw.scl,
            legRegion.height * Draw.scl - Math.max(-sin * i, 0) * legRegion.height * 0.5f * Draw.scl,
            mech.baseRotation() - 90 + 35f*i*e);
        }

        Draw.mixcol(Color.white, unit.hitTime);

        if(floor.isLiquid){
            Draw.color(Color.white, floor.mapColor, unit.drownTime() * 0.4f);
        }else{
            Draw.color(Color.white);
        }
        Draw.alpha(unitTrans); //
        
        Draw.rect(baseRegion, unit, mech.baseRotation() - 90);

        Draw.mixcol();
    }

    public void applyColor(Unit unit){
        Draw.color();
        Draw.mixcol(Color.white, unit.hitTime);
        if(unit.drownTime > 0 && unit.floorOn().isDeep()){
            Draw.mixcol(unit.floorOn().mapColor, unit.drownTime * 0.8f);
        }
    }

    //endregion

    public static void initUnitTransp(){
        unitTrans = (float)Core.settings.getInt("unitTransparency") / 100f;
    }

    public static void initLegTransp(){
        legTrans = (float)Core.settings.getInt("unitLegTransparency") / 100f;
    }
}
