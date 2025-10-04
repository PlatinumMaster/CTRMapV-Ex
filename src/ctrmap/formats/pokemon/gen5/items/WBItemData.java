package ctrmap.formats.pokemon.gen5.items;

public class WBItemData {
    private int price;
    private int heldEffect;
    private int heldArgument;
    private int naturalGiftEffect;
    private int flingEffect;
    private int flingPower;
    private int naturalGiftPower;
    private int packed;
    private int effectField;
    private int effectBattle;
    private int hasBattleStats;
    private int itemClass;
    private int consumable;
    private int sortIndex;
    private WBItemBattleStats battleStats;

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getHeldEffect() {
        return heldEffect;
    }

    public void setHeldEffect(int heldEffect) {
        this.heldEffect = heldEffect;
    }

    public int getHeldArgument() {
        return heldArgument;
    }

    public void setHeldArgument(int heldArgument) {
        this.heldArgument = heldArgument;
    }

    public int getNaturalGiftEffect() {
        return naturalGiftEffect;
    }

    public void setNaturalGiftEffect(int naturalGiftEffect) {
        this.naturalGiftEffect = naturalGiftEffect;
    }

    public int getFlingEffect() {
        return flingEffect;
    }

    public void setFlingEffect(int flingEffect) {
        this.flingEffect = flingEffect;
    }

    public int getFlingPower() {
        return flingPower;
    }

    public void setFlingPower(int flingPower) {
        this.flingPower = flingPower;
    }

    public int getNaturalGiftPower() {
        return naturalGiftPower;
    }

    public void setNaturalGiftPower(int naturalGiftPower) {
        this.naturalGiftPower = naturalGiftPower;
    }

    public int getPacked() {
        return packed;
    }

    public void setPacked(int packed) {
        this.packed = packed;
    }

    public int getEffectField() {
        return effectField;
    }

    public void setEffectField(int effectField) {
        this.effectField = effectField;
    }

    public int getEffectBattle() {
        return effectBattle;
    }

    public void setEffectBattle(int effectBattle) {
        this.effectBattle = effectBattle;
    }

    public int getHasBattleStats() {
        return hasBattleStats;
    }

    public void setHasBattleStats(int hasBattleStats) {
        this.hasBattleStats = hasBattleStats;
    }

    public int getItemClass() {
        return itemClass;
    }

    public void setItemClass(int itemClass) {
        this.itemClass = itemClass;
    }

    public int getConsumable() {
        return consumable;
    }

    public void setConsumable(int consumable) {
        this.consumable = consumable;
    }

    public int getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(int sortIndex) {
        this.sortIndex = sortIndex;
    }

    public WBItemBattleStats getBattleStats() {
        return battleStats;
    }

    public void setBattleStats(WBItemBattleStats battleStats) {
        this.battleStats = battleStats;
    }
}