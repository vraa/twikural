package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
import dto.SiteMeta;

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
			Logger.info("Invalid Kural ID [" + kid + "]");
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
	
	public static Result manualRoll(){
		Logger.info("Manually rolling.");
		roll();
		return ok("Manually rolled");
	}

	public static boolean roll() {
		boolean status = false;
		Data data = Data.find.byId(1L);
		Long nextKural = data.activeKuralId == Thirukural.LAST ? 1L
				: data.activeKuralId + 1;
		Logger.info("Rolling over to next thirukural [" + nextKural + "]");
		Thirukural thirukural = getKural(nextKural);
		if (publish(thirukural)) {
			data.activeKuralId = nextKural.intValue();
			data.save();
			Logger.info("Successfully rolled over to next thirukural ["
					+ nextKural + "]");
			status = true;
		} else {
			String message = "Failed to roll over to next thirukural ["
					+ nextKural + "]";
			Logger.info(message);
			informAdmin(message);
			status = false;
		}
		return status;
	}

	private static String mapToPublicContent(Thirukural thirukural) {
		return thirukural.asText() + " / விளக்கம் காண:  " + thirukural.url();
	}

	private static boolean publish(Thirukural thirukural) {
		Logger.info("Going to publish kural [" + thirukural.id + "]");
		boolean status = false;
		Twitter twitter = TwitterFactory.getSingleton();
		try {
			twitter.updateStatus(mapToPublicContent(thirukural));
			status = true;
			Logger.info("Successfully published to Twitter [" + thirukural.id
					+ "]");
		} catch (TwitterException e) {
			status = false;
			Logger.info("Failed to publish to Twitter[" + thirukural.id + "]");
			Logger.info(e.getErrorMessage());
		}
		if(status){
			publishToTwitterFollowers(thirukural);
		}
		return status;
	}

	private static boolean publishToTwitterFollowers(Thirukural thirukural) {
		Logger.info("Going to send DMs for [" + thirukural.id + "]");
		boolean status = false;
		Twitter twitter = TwitterFactory.getSingleton();
		IDs followers = null;
		try {
			followers = twitter.getFollowersIDs(-1);
		} catch (TwitterException twe) {
			Logger.info("Failed to get followers list [" + thirukural.id + "]");
			status = false;
		}
		if (followers != null) {
			Logger.info("Sending kural [" + thirukural.id + "] to "
					+ followers.getIDs().length + " followers.");
			for (long follower : followers.getIDs()) {
				try {
					twitter.sendDirectMessage(follower,
							mapToPublicContent(thirukural));
				} catch (TwitterException e) {
					Logger.info("Failed sending kural [" + thirukural.id
							+ "] to [" + follower + "]");
				}
			}
			status = true;
		}

		return status;
	}

	private static void informAdmin(String message) {
		Twitter twitter = TwitterFactory.getSingleton();
		try {
			twitter.sendDirectMessage(8643632, message);
		} catch (TwitterException te) {
			Logger.info("Unable to inform admin" + te.getMessage());
		}
	}

	public static String theme() {
		//String[] themes = { "navy", "orange", "teal", "maroon" };
		return "teal";
	}

}
