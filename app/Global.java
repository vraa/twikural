import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import akka.actor.Cancellable;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.libs.Akka;
import play.libs.Time.CronExpression;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

public class Global extends GlobalSettings {

	Cancellable morningJob = null;
	Cancellable eveningJob = null;

	public void onStart(Application app) {
		scheduleJobs();
	}

	public void onStop(Application app) {
		cancelJobs();
	}

	private void scheduleJobs() {
		scheduleMorningJob();
		scheduleEveningJob();
	}

	private void cancelJobs() {
		if (morningJob != null) {
			morningJob.cancel();
			Logger.info("Canceled morning job");
		}
		if (eveningJob != null) {
			eveningJob.cancel();
			Logger.info("Canceled evening job");
		}
	}

	private void scheduleMorningJob() {
		Logger.info("Scheduling morning cron job");
		try {
			CronExpression cron = new CronExpression("0 0 10 1/1 * ? *");
			cron.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));

			Date nextValidTime = cron.getNextValidTimeAfter(new Date());
			FiniteDuration fd = Duration.create(nextValidTime.getTime()
					- System.currentTimeMillis(), TimeUnit.MILLISECONDS);

			morningJob = Akka.system().scheduler()
					.scheduleOnce(fd, new Runnable() {
						public void run() {
							controllers.Application.trigger();
							scheduleMorningJob();
						}
					}, Akka.system().dispatcher());
			Logger.info("Successfully scheduled morning cron job");

		} catch (ParseException e) {
		}
	}

	private void scheduleEveningJob() {
		Logger.info("Scheduling evening cron job");
		try {
			CronExpression cron = new CronExpression("0 0 16 1/1 * ? *");
			cron.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));

			Date nextValidTime = cron.getNextValidTimeAfter(new Date());
			FiniteDuration fd = Duration.create(nextValidTime.getTime()
					- System.currentTimeMillis(), TimeUnit.MILLISECONDS);

			eveningJob = Akka.system().scheduler()
					.scheduleOnce(fd, new Runnable() {
						public void run() {
							controllers.Application.trigger();
							scheduleEveningJob();
						}
					}, Akka.system().dispatcher());
			Logger.info("Successfully scheduled evening cron job");
		} catch (ParseException e) {

		}
	}
}
