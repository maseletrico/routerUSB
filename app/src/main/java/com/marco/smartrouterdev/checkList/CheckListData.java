package com.marco.smartrouterdev.checkList;

public class CheckListData {
    private String checkListItem;
    private boolean itemChecked;
    private boolean itemUnsuitable;
    private boolean itemBad;


    String getCheckListItem() {
        return checkListItem;
    }

    public void setCheckListItem(String checkListItem) {
        this.checkListItem = checkListItem;
    }

    public boolean isItemChecked() {
        return itemChecked;
    }

    public void setItemChecked(boolean itemChecked) {
        this.itemChecked = itemChecked;
    }

    public boolean isItemUnsuitable() {
        return itemUnsuitable;
    }

    public void setItemUnsuitable(boolean itemUnsuitable) {
        this.itemUnsuitable = itemUnsuitable;
    }

    public boolean isItemBad() {
        return itemBad;
    }

    public void setItemBad(boolean itemBad) {
        this.itemBad = itemBad;
    }
}
