package controllers;

import models.Data;
import models.Thirukural;
import play.Logger;
import play.cache.Cache;
import play.mvc.Controller;
import play.mvc.Result;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import views.html.index;

public class Application extends Controller {

	public static Result index() {
		Data data = Data.find.byId(1L);
		return redirect("/kural/" + data.activeKuralId);
	}

	public static Result kural(Integer id) {
		Thirukural thirukural = (Thirukural) Cache.get("kural-" + id);
		if (thirukural == null) {
			thirukural = Thirukural.find.byId(Long.valueOf(id));
			Cache.set("kural-" + id, thirukural);
		}
		return ok(index.render(thirukural));
	}
	
	public static Result test(){
		roll();
		return ok();
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

	private static boolean publish(Thirukural thirukural) {
		boolean status = false;
		Twitter twitter = TwitterFactory.getSingleton();
		try {
			twitter.updateStatus(thirukural.asText()
					+ " / விளக்கம் காண:  " + thirukural.url());
			status = true;
			Logger.info("Successfully published to Twitter");
		} catch (TwitterException e) {
			status = false;
			Logger.error("Failed to publish to Twitter[" + e.getMessage() + "]");
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

}
