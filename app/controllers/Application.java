package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dto.SiteMeta;
import models.Data;
import models.Thirukural;
import play.Logger;
import play.cache.Cache;
import play.mvc.Controller;
import play.mvc.Result;
import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import views.html.home;
import views.html.index;

public class Application extends Controller {

	private static Thirukural getKural(long id) {
		Thirukural thirukural = (Thirukural) Cache.get("kural-" + id);
		if (thirukural == null) {
			thirukural = Thirukural.find.byId(id);
			Cache.set("kural-" + id, thirukural);
		}
		return thirukural;
	}

	public static Result index() {
		List<Thirukural> thirukurals = new ArrayList<>();

		Data data = Data.find.byId(1L);
		thirukurals.add(getKural(data.activeKuralId));
		int j = data.activeKuralId - 1;
		for (int i = 1; i <= 3; i++) {
			j = j > 0 ? j : j + 1330;
			Thirukural thirukural = getKural(Long.valueOf(j));
			if (thirukural != null) {
				thirukurals.add(thirukural);
			}
			j--;
		}

		return ok(home.render(new SiteMeta(), thirukurals));
	}

	public static Result kural(String kid) {
		Integer id = 0;
		try {
			id = Integer.valueOf(kid);
		} catch (NumberFormatException nfe) {
			Logger.error("Invalid Kural ID [" + kid + "]");
			id = 0;
		}
		if (id == 0) {
			return badRequest("Invalid Kural ID");
		}
		Thirukural thirukural = getKural(id);
		if (thirukural != null) {
			return ok(index.render(thirukural));
		} else {
			return badRequest("Invalid Kural ID");
		}
	}

	public static Result oldApi(String id) {
		if (id == null) {
			Data data = Data.find.byId(1L);
			return kural(String.valueOf(data.activeKuralId));
		} else {
			Long kuralId = Long.valueOf(id, 16) - 10297;
			return kural(kuralId.toString());
		}
	}

	public static void trigger() {
		Logger.info("Triggered by cron job");
		roll();
		Logger.info("Successfully called triggered task");
	}

	public static boolean roll() {
		Logger.info("Rolling over to next thirukural");
		boolean status = false;
		Data data = Data.find.byId(1L);
		Long nextKural = data.activeKuralId == Thirukural.LAST ? 1L
				: data.activeKuralId + 1;
		Thirukural thirukural = Thirukural.find.byId(Long.valueOf(nextKural));
		if (publish(thirukural)) {
			data.activeKuralId = nextKural.intValue();
			data.save();
			Logger.info("Successfully rolled over to next thirukural");
			status = true;
		} else {
			Logger.error("Failed to roll over to next thirukural");
			informAdmin("Failed to roll over to next thirukural");
			status = false;
		}
		return status;
	}

	private static String mapToPublicContent(Thirukural thirukural) {
		return thirukural.asText() + " / விளக்கம் காண:  " + thirukural.url();
	}

	private static boolean publish(Thirukural thirukural) {
		boolean status = false;
		Twitter twitter = TwitterFactory.getSingleton();
		try {
			twitter.updateStatus(mapToPublicContent(thirukural));
			status = true;
			Logger.info("Successfully published to Twitter");
		} catch (TwitterException e) {
			status = false;
			Logger.error("Failed to publish to Twitter[" + e.getMessage() + "]");
		}
		if (status) {
			publishToTwitterFollowers(thirukural);
		}
		return status;
	}

	private static boolean publishToTwitterFollowers(Thirukural thirukural) {
		boolean status = false;
		try {
			Twitter twitter = TwitterFactory.getSingleton();
			IDs followers = twitter.getFollowersIDs(-1);
			for (long follower : followers.getIDs()) {
				twitter.sendDirectMessage(follower,
						mapToPublicContent(thirukural));
			}
			status = true;
		} catch (TwitterException twe) {
			Logger.error("Failed to send DMs" + twe.getMessage());
			status = false;
		}
		return status;
	}

	private static void informAdmin(String message) {
		Twitter twitter = TwitterFactory.getSingleton();
		try {
			twitter.sendDirectMessage(8643632, message);
		} catch (TwitterException te) {
			Logger.error("Unable to inform admin" + te.getMessage());
		}
	}

	public static String theme() {
		String[] themes = { "navy", "orange", "teal", "maroon" };
		return themes[new Random().nextInt(themes.length)];
	}

}
