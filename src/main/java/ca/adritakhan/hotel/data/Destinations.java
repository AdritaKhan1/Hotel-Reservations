package ca.adritakhan.hotel.data;

import java.util.List;

/**
 * The six Canadian destinations this demo sells hotels in. Shared by
 * {@code DemoDataSeeder} (which creates the hotels) and {@code HotelController}
 * (which lists them for the search form), so the two never drift apart.
 */
public final class Destinations {

    public static final List<Destination> ALL = List.of(
            new Destination("Toronto", "Ontario",
                    img("1517090504586-fde19ea6066f")),
            new Destination("Niagara Falls", "Ontario",
                    img("1526129318478-62ed807ebdf9")),
            new Destination("Montreal", "Quebec",
                    img("1519178614-68673b201f36")),
            new Destination("Banff", "Alberta",
                    img("1486870591958-9b9d0d1dda99")),
            new Destination("Vancouver", "British Columbia",
                    img("1519681393784-d120267933ba")),
            new Destination("Blue Mountain", "Ontario",
                    img("1551524559-8af4e6624178"))
    );

    private Destinations() {
    }

    public static String img(String unsplashPhotoId) {
        return "https://images.unsplash.com/photo-" + unsplashPhotoId + "?w=1200&q=80&auto=format&fit=crop";
    }
}
