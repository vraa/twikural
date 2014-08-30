package models;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;

@Entity
public class Data extends Model {
	
	private static final long serialVersionUID = 4394059321784203349L;
	
	@Id
	public Long id;
	
	public int activeKuralId;
	
	public String twitterFollowers;
	
	public static Finder<Long, Data> find = new Finder<>(Long.class,
			Data.class);

}
