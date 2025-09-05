package com.showcase.gui;

public class MapViewerGui extends BaseWorldGui {
    public MapViewerGui(MapViewerContext context, int slot) {
        super(context, slot);
        this.rebuildUi();
        this.open();
    }

    @Override
    protected void buildUi() {
        this.addSlot(((MapViewerContext) context).getMap());
    }

    @Override
    protected BaseViewerContext.SwitchEntry asSwitchableUi() {
        return new BaseViewerContext.SwitchEntry((context, slot) -> new MapViewerGui((MapViewerContext) context, slot), this.getSelectedSlot());
    }

    @Override
    public void onClose() {
        super.onClose();
    }
}
