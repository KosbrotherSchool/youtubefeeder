package at.bartinger.list.item;


public class EntryItem implements Item{

	public final String title;
	public final String subtitle;
	public final String picUrl;
	public final int picInt;

	public EntryItem(String title, String subtitle, String picUrl) {
		this.title = title;
		this.subtitle = subtitle;
		this.picUrl = picUrl;
		this.picInt = -1;
	}
	
	public EntryItem(String title, int icon_draw) {
		// TODO Auto-generated constructor stub
		this.title = title;
		this.picInt = icon_draw;
		this.subtitle = "";
		this.picUrl = "";
	}

	@Override
	public boolean isSection() {
		return false;
	}

}
