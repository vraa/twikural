package models;

import java.util.Random;

import javax.persistence.Entity;
import javax.persistence.Id;

import controllers.routes;
import dto.SiteMeta;
import play.db.ebean.Model;
import play.mvc.Http.Context;
import play.mvc.Http.Request;

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
		return String.valueOf(this.id == Thirukural.FIRST ? Thirukural.LAST
				: this.id - 1);
	}

	public String next() {
		return String.valueOf(this.id == Thirukural.LAST ? Thirukural.FIRST
				: this.id + 1);
	}

	public String asText() {
		return tamil.replace("<br>", " ");
	}

	public String url() {
		return baseURL()
				+ routes.Application.kural(String.valueOf(this.id)).url();
	}

	public SiteMeta meta() {
		String[] images = { "valluvar-1", "valluvar-2", "valluvar-3" };
		SiteMeta meta = new SiteMeta();
		meta.title = this.asText();
		meta.description = this.muva;
		meta.url = this.url();
		meta.image = baseURL() + routes.Assets.at("images/cover").url() + "/"
				+ images[new Random().nextInt(images.length)] + ".jpg";
		return meta;
	}

	private String baseURL() {
		String base = "http://twikural.veerasundar.com";
		try {
			Request req = Context.current().request();
			if(req!=null){
				base = "http://" + req.host();
			}
		} catch (Exception e) {

		}
		return base;
	}
}
