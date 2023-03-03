package com.nktfh100.AmongUs.info;

public class ItemInfoContainer {

	private ItemInfo item;
	private ItemInfo item2;
	private ItemInfo item3;

	public ItemInfoContainer(ItemInfo item, ItemInfo item2, ItemInfo item3) {
		this.item = item;
		this.item2 = item2;
		this.item3 = item3;
	}

	public Integer getSlot() {
		return this.item.getSlot();
	}

	public ItemInfo getItem3() {
		return item3;
	}

	public ItemInfo getItem2() {
		return item2;
	}

	public ItemInfo getItem() {
		return item;
	}

}
