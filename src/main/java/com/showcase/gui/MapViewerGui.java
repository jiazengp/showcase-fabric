package com.showcase.gui;

public class MapViewerGui extends BaseWorldGui {
    public MapViewerGui(MapViewerContext context, int slot) {
        super(context, slot);
        this.rebuildUi();
        this.open();
    }

    @Override
    protected void buildUi() {
        this.addSlot(context.getMap());
    }

    @Override
    protected MapViewerContext.SwitchEntry asSwitchableUi() {
        return new MapViewerContext.SwitchEntry(MapViewerGui::new, this.getSelectedSlot());
    }

    @Override
    public void onClose() {
        super.onClose();
    }
}
