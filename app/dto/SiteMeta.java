package dto;

import controllers.routes;

public class SiteMeta {

	public String title = "Twikural";
	public String description = "தினமிரு திருக்குறள், நேரடியாக உங்களின் Facebook / Twitter க்கு.";
	public String image = "http://twikural.veerasundar.com/assets/images/valluvar-fb.png";
	public String url = routes.Application.index().url();

}
