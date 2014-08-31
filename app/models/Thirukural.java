package models;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;

@Entity
public class Thirukural extends Model {

	private static final long serialVersionUID = -6568487359920458909L;

	public static final long LAST = 1330L;
	public static final long FIRST = 1L;

	@Id
	public long id;

	public String tamil;

	public String english;

	public String explanation;

	public String adhikaram;

	public String kalaignar;

	public String muva;

	public String pappaiya;

	public static Finder<Long, Thirukural> find = new Finder<>(Long.class,
			Thirukural.class);

	public String previous() {
		return String.valueOf(this.id == Thirukural.FIRST ? Thirukural.LAST : this.id - 1);
	}

	public String next() {
		return String.valueOf(this.id == Thirukural.LAST ? Thirukural.FIRST : this.id + 1);
	}
	
	public String asText(){
		return tamil.replace("<br>", " ");
	}
	
	public String url(){
		return "http://twikural.veerasundar.com/kural/" + this.id;
	}

}
