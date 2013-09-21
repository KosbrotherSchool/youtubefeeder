package at.bartinger.list.item;


public class EntryItem implements Item{

	public final String title;
	public final String subtitle;
	public final String picUrl;

	public EntryItem(String title, String subtitle, String picUrl) {
		this.title = title;
		this.subtitle = subtitle;
		this.picUrl = picUrl;
	}
	
	@Override
	public boolean isSection() {
		return false;
	}

}
