package ca.adritakhan.hotel.data;

import ca.adritakhan.hotel.domain.BedType;
import ca.adritakhan.hotel.domain.Hotel;
import ca.adritakhan.hotel.domain.RoomType;
import ca.adritakhan.hotel.repository.HotelRepository;
import ca.adritakhan.hotel.repository.RoomTypeRepository;
import ca.adritakhan.hotel.service.InventoryService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Populates the six destinations with hotels, room types and ~7 months of
 * nightly inventory the first time the app starts against an empty database.
 * Idempotent (skips entirely once any hotel exists) and disabled in tests via
 * {@code app.seed-demo-data=false} (see src/test/resources/application.yml) so
 * the concurrency/reservation tests aren't slowed down by seeding data they
 * don't use.
 */
@Component
@ConditionalOnProperty(name = "app.seed-demo-data", havingValue = "true", matchIfMissing = true)
public class DemoDataSeeder implements ApplicationRunner {

    private static final int NIGHTS_TO_OPEN = 210;

    private final HotelRepository hotels;
    private final RoomTypeRepository roomTypes;
    private final InventoryService inventory;

    public DemoDataSeeder(HotelRepository hotels, RoomTypeRepository roomTypes, InventoryService inventory) {
        this.hotels = hotels;
        this.roomTypes = roomTypes;
        this.inventory = inventory;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (hotels.count() > 0) {
            return;
        }

        seedToronto();
        seedNiagaraFalls();
        seedMontreal();
        seedBanff();
        seedVancouver();
        seedBlueMountain();

        LocalDate from = LocalDate.now();
        LocalDate to = from.plusDays(NIGHTS_TO_OPEN);
        for (RoomType type : roomTypes.findAll()) {
            if (type.getHotel() != null) {
                inventory.openInventory(type.getId(), from, to, roomsForRate(type.getNightlyRate()));
            }
        }
    }

    /** Cheaper rooms are sold in higher volume, like a real hotel's room mix. */
    private int roomsForRate(BigDecimal nightlyRate) {
        if (nightlyRate.compareTo(new BigDecimal("300")) >= 0) {
            return 3;
        }
        if (nightlyRate.compareTo(new BigDecimal("200")) >= 0) {
            return 5;
        }
        return 8;
    }

    private void seedToronto() {
        Hotel royalYork = hotel("Fairmont Royal York", "Fairmont", "Toronto", "Ontario",
                "A landmark since 1929, directly across from Union Station in the heart of downtown Toronto. "
                        + "Grand chateau-style architecture, a rooftop apiary, and the CN Tower a short walk away.",
                "100 Front St W, Toronto, ON",
                img("1517935706615-2717063c2225"),
                List.of(img("1517090504586-fde19ea6066f"), img("1611048267451-e6ed903d4a38"), img("1595576508898-0ad5c879a061")),
                List.of("Free WiFi", "Indoor Pool", "Fitness Center", "Spa", "Restaurant", "Valet Parking"));

        roomType(royalYork, "Fairmont King Room", "A bright king room with skyline views over the Financial District.",
                BedType.KING, 1, "City View", 2, "319.00", img("1591088398332-8a7791972843"));
        roomType(royalYork, "Classic Queen Room", "Two queen beds, ideal for families exploring downtown Toronto.",
                BedType.QUEEN, 1, "City View", 4, "279.00", img("1616594039964-ae9021a400a0"));
        roomType(royalYork, "Royal Two-Bedroom Suite", "A two-bedroom suite with a separate living area and skyline views.",
                BedType.KING, 2, "Skyline View", 6, "549.00", img("1631049035182-249067d7618e"));

        Hotel sheratonCentre = hotel("Sheraton Centre Toronto Hotel", "Sheraton", "Toronto", "Ontario",
                "Steps from the Eaton Centre and City Hall, with an award-winning indoor/outdoor pool garden "
                        + "right in the middle of the entertainment district.",
                "123 Queen St W, Toronto, ON",
                img("1584132967334-10e028bd69f7"),
                List.of(img("1517090504586-fde19ea6066f"), img("1566665797739-1674de7a421a"), img("1590490360182-c33d57733427")),
                List.of("Free WiFi", "Outdoor Pool", "Fitness Center", "Restaurant", "Business Center", "Pet Friendly"));

        roomType(sheratonCentre, "Sheraton King Room", "A modern king room overlooking Nathan Phillips Square.",
                BedType.KING, 1, "City View", 2, "259.00", img("1566073771259-6a8506099945"));
        roomType(sheratonCentre, "Double Room, Two Doubles", "Two double beds, great for groups and families.",
                BedType.DOUBLE, 1, "Standard View", 4, "229.00", img("1560448204-e02f11c3d0e2"));
        roomType(sheratonCentre, "Executive Queen Suite", "A one-bedroom suite with a queen bed and separate sitting area.",
                BedType.QUEEN, 1, "City View", 3, "349.00", img("1578683010236-d716f9a3f461"));
    }

    private void seedNiagaraFalls() {
        Hotel fallsview = hotel("Sheraton Fallsview Hotel", "Sheraton", "Niagara Falls", "Ontario",
                "Perched directly above the brink of the Horseshoe Falls, with many rooms looking straight down onto "
                        + "the mist. Steps from Clifton Hill and the Fallsview Casino.",
                "5875 Falls Ave, Niagara Falls, ON",
                img("1526129318478-62ed807ebdf9"),
                List.of(img("1502085671122-2d218cd434e6"), img("1566665797739-1674de7a421a"), img("1611048267451-e6ed903d4a38")),
                List.of("Free WiFi", "Indoor Pool", "Fitness Center", "Spa", "Restaurant", "Falls View Terrace"));

        roomType(fallsview, "King Room, Falls View", "Floor-to-ceiling windows facing the Horseshoe Falls.",
                BedType.KING, 1, "Falls View", 2, "329.00", img("1591088398332-8a7791972843"));
        roomType(fallsview, "Queen Room, Falls View", "Two queen beds with a direct view of the falls.",
                BedType.QUEEN, 1, "Falls View", 4, "309.00", img("1616594039964-ae9021a400a0"));
        roomType(fallsview, "Two-Bedroom Falls View Suite", "A two-bedroom suite with wraparound windows over the falls.",
                BedType.KING, 2, "Falls View", 6, "599.00", img("1631049035182-249067d7618e"));

        Hotel marriott = hotel("Marriott on the Falls", "Marriott", "Niagara Falls", "Ontario",
                "A short walk from the falls with a rooftop revolving restaurant and an indoor water park "
                        + "next door, a favourite with families.",
                "6740 Fallsview Blvd, Niagara Falls, ON",
                img("1445019980597-93fa8acb246c"),
                List.of(img("1502085671122-2d218cd434e6"), img("1551882547-ff40c63fe5fa"), img("1590490360182-c33d57733427")),
                List.of("Free WiFi", "Indoor Water Park Access", "Fitness Center", "Restaurant", "Parking"));

        roomType(marriott, "King Room, City View", "A comfortable king room overlooking the city skyline.",
                BedType.KING, 1, "City View", 2, "219.00", img("1566073771259-6a8506099945"));
        roomType(marriott, "Double Room, Partial Falls View", "Two double beds with a partial view of the falls mist.",
                BedType.DOUBLE, 1, "Falls View", 4, "259.00", img("1560448204-e02f11c3d0e2"));
        roomType(marriott, "Family Two-Bedroom Suite", "A two-bedroom suite, perfect for a family trip to the falls.",
                BedType.QUEEN, 2, "Falls View", 6, "479.00", img("1595576508898-0ad5c879a061"));
    }

    private void seedMontreal() {
        Hotel queenElizabeth = hotel("Fairmont Queen Elizabeth", "Fairmont", "Montreal", "Quebec",
                "An Art Deco icon in the heart of downtown Montreal, connected to the underground city and "
                        + "a five-minute walk from Old Montreal.",
                "900 Rene-Levesque Blvd W, Montreal, QC",
                img("1519178614-68673b201f36"),
                List.of(img("1519832979-6fa011b87667"), img("1611048267451-e6ed903d4a38"), img("1522798514-97ceb8c4f1c8")),
                List.of("Free WiFi", "Indoor Pool", "Fitness Center", "Spa", "Restaurant", "Valet Parking"));

        roomType(queenElizabeth, "Fairmont King Room", "An elegant king room with views of downtown Montreal.",
                BedType.KING, 1, "City View", 2, "289.00", img("1566073771259-6a8506099945"));
        roomType(queenElizabeth, "Classic Queen Room", "Two queen beds in a refined, quiet room.",
                BedType.QUEEN, 1, "Standard View", 4, "249.00", img("1601918774946-25832a4be0d6"));
        roomType(queenElizabeth, "Fairmont Two-Bedroom Suite", "A two-bedroom suite with a separate living room.",
                BedType.KING, 2, "City View", 6, "519.00", img("1631049035182-249067d7618e"));

        Hotel fourSeasonsMtl = hotel("Four Seasons Hotel Montreal", "Four Seasons", "Montreal", "Quebec",
                "A modern, understated luxury property in the Golden Square Mile, next to boutique shopping "
                        + "on Sherbrooke Street.",
                "1440 Rue de la Montagne, Montreal, QC",
                img("1611892440504-42a792e24d32"),
                List.of(img("1519178614-68673b201f36"), img("1618773928121-c32242e63f39"), img("1590490360182-c33d57733427")),
                List.of("Free WiFi", "Rooftop Pool", "Fitness Center", "Spa", "Restaurant", "Concierge"));

        roomType(fourSeasonsMtl, "Deluxe King Room", "A spacious king room with a marble bathroom.",
                BedType.KING, 1, "City View", 2, "429.00", img("1591088398332-8a7791972843"));
        roomType(fourSeasonsMtl, "Deluxe Queen Room", "A quiet queen room overlooking the Golden Square Mile.",
                BedType.QUEEN, 1, "City View", 3, "399.00", img("1616594039964-ae9021a400a0"));
        roomType(fourSeasonsMtl, "Signature Two-Bedroom Suite", "A two-bedroom suite with a dining area.",
                BedType.KING, 2, "City View", 6, "899.00", img("1578683010236-d716f9a3f461"));
    }

    private void seedBanff() {
        Hotel banffSprings = hotel("Fairmont Banff Springs", "Fairmont", "Banff", "Alberta",
                "A Scottish-baronial \"castle in the Rockies\" set among Sulphur Mountain and the Bow Valley, "
                        + "with golf, a full spa, and hiking trails from the front door.",
                "405 Spray Ave, Banff, AB",
                img("1508264165352-258db2ebd59b"),
                List.of(img("1486870591958-9b9d0d1dda99"), img("1517816428104-797678c7cf0c"), img("1441974231531-c6227db76b6e")),
                List.of("Free WiFi", "Indoor Pool", "Full Spa", "Golf Course", "Ski Storage", "Restaurant"));

        roomType(banffSprings, "King Room, Mountain View", "A king room facing the Rocky Mountains.",
                BedType.KING, 1, "Mountain View", 2, "379.00", img("1591088398332-8a7791972843"));
        roomType(banffSprings, "Queen Room, Mountain View", "Two queen beds with a view of the Bow Valley.",
                BedType.QUEEN, 1, "Mountain View", 4, "349.00", img("1601918774946-25832a4be0d6"));
        roomType(banffSprings, "Castle Two-Bedroom Suite", "A two-bedroom suite in the historic castle wing.",
                BedType.KING, 2, "Mountain View", 6, "749.00", img("1631049035182-249067d7618e"));

        Hotel ptarmigan = hotel("Banff Ptarmigan Inn", "", "Banff", "Alberta",
                "A cozy independent lodge on Banff Avenue, a short stroll from the shops, restaurants and "
                        + "the gondola up Sulphur Mountain.",
                "337 Banff Ave, Banff, AB",
                img("1516483638261-f4dbaf036963"),
                List.of(img("1470770841072-f978cf4d019e"), img("1551882547-ff40c63fe5fa"), img("1522798514-97ceb8c4f1c8")),
                List.of("Free WiFi", "Indoor Pool", "Hot Tub", "Ski Storage", "Parking"));

        roomType(ptarmigan, "King Room, Mountain View", "A cozy king room with views of the surrounding peaks.",
                BedType.KING, 1, "Mountain View", 2, "199.00", img("1566073771259-6a8506099945"));
        roomType(ptarmigan, "Double Room, Two Doubles", "Two double beds, well suited to ski trips with friends.",
                BedType.DOUBLE, 1, "Standard View", 4, "179.00", img("1560448204-e02f11c3d0e2"));
        roomType(ptarmigan, "Family Two-Bedroom Suite", "A two-bedroom suite with a kitchenette.",
                BedType.QUEEN, 2, "Mountain View", 6, "329.00", img("1595576508898-0ad5c879a061"));
    }

    private void seedVancouver() {
        Hotel waterfront = hotel("Fairmont Waterfront", "Fairmont", "Vancouver", "British Columbia",
                "Set on the harbour across from Canada Place, with floatplanes taking off outside and the "
                        + "North Shore mountains as a backdrop.",
                "900 Canada Pl, Vancouver, BC",
                img("1519681393784-d120267933ba"),
                List.of(img("1560814304-4f05b62af116"), img("1611048267451-e6ed903d4a38"), img("1522798514-97ceb8c4f1c8")),
                List.of("Free WiFi", "Outdoor Pool", "Fitness Center", "Spa", "Restaurant", "Valet Parking"));

        roomType(waterfront, "King Room, Harbour View", "A king room overlooking the harbour and mountains.",
                BedType.KING, 1, "Ocean View", 2, "339.00", img("1591088398332-8a7791972843"));
        roomType(waterfront, "Queen Room, City View", "Two queen beds with a view over downtown Vancouver.",
                BedType.QUEEN, 1, "City View", 4, "299.00", img("1616594039964-ae9021a400a0"));
        roomType(waterfront, "Harbour Two-Bedroom Suite", "A two-bedroom suite with panoramic harbour views.",
                BedType.KING, 2, "Ocean View", 6, "649.00", img("1631049035182-249067d7618e"));

        Hotel fourSeasonsYvr = hotel("Four Seasons Hotel Vancouver", "Four Seasons", "Vancouver", "British Columbia",
                "Downtown on Georgia Street, connected to the Pacific Centre shops with a quiet, residential "
                        + "feel a block from Robson Street.",
                "791 W Georgia St, Vancouver, BC",
                img("1582719478250-c89cae4dc85b"),
                List.of(img("1519681393784-d120267933ba"), img("1618773928121-c32242e63f39"), img("1590490360182-c33d57733427")),
                List.of("Free WiFi", "Indoor Pool", "Fitness Center", "Spa", "Restaurant", "Concierge"));

        roomType(fourSeasonsYvr, "Deluxe King Room", "A spacious king room with a soaking tub.",
                BedType.KING, 1, "City View", 2, "449.00", img("1601918774946-25832a4be0d6"));
        roomType(fourSeasonsYvr, "Deluxe Queen Room", "A calm queen room a block from Robson Street.",
                BedType.QUEEN, 1, "City View", 3, "419.00", img("1571896349842-33c89424de2d"));
        roomType(fourSeasonsYvr, "Signature Two-Bedroom Suite", "A two-bedroom suite with a dining area and city views.",
                BedType.KING, 2, "City View", 6, "929.00", img("1578683010236-d716f9a3f461"));
    }

    private void seedBlueMountain() {
        Hotel westin = hotel("Westin Trillium House Blue Mountain", "Westin", "Blue Mountain", "Ontario",
                "Ski-in/ski-out at the base of the Village, with a rooftop hot tub looking straight up the "
                        + "slopes, and equally popular in summer for the trails and beach.",
                "220 Gord Canning Dr, The Blue Mountains, ON",
                img("1551524559-8af4e6624178"),
                List.of(img("1516483638261-f4dbaf036963"), img("1441974231531-c6227db76b6e"), img("1551882547-ff40c63fe5fa")),
                List.of("Free WiFi", "Ski-in/Ski-out", "Rooftop Hot Tub", "Fitness Center", "Restaurant", "Ski Storage"));

        roomType(westin, "King Room, Slope View", "A king room looking straight up the ski slopes.",
                BedType.KING, 1, "Mountain View", 2, "289.00", img("1591088398332-8a7791972843"));
        roomType(westin, "Queen Room, Village View", "Two queen beds overlooking the pedestrian village.",
                BedType.QUEEN, 1, "Mountain View", 4, "259.00", img("1616594039964-ae9021a400a0"));
        roomType(westin, "Two-Bedroom Slopeside Suite", "A two-bedroom suite with a kitchenette, steps from the lifts.",
                BedType.KING, 2, "Mountain View", 6, "579.00", img("1631049035182-249067d7618e"));

        Hotel innAndSuites = hotel("Blue Mountain Inn & Suites", "", "Blue Mountain", "Ontario",
                "A relaxed, independent inn a short shuttle ride from the village, with a quiet indoor pool "
                        + "and easy access to the Georgian Trail.",
                "150 Crossing Way, The Blue Mountains, ON",
                img("1508264165352-258db2ebd59b"),
                List.of(img("1470770841072-f978cf4d019e"), img("1551882547-ff40c63fe5fa"), img("1522798514-97ceb8c4f1c8")),
                List.of("Free WiFi", "Indoor Pool", "Hot Tub", "Ski Storage", "Free Parking", "Pet Friendly"));

        roomType(innAndSuites, "King Room, Standard View", "A comfortable king room close to the trail entrance.",
                BedType.KING, 1, "Standard View", 2, "169.00", img("1566073771259-6a8506099945"));
        roomType(innAndSuites, "Double Room, Two Doubles", "Two double beds, popular with ski groups.",
                BedType.DOUBLE, 1, "Standard View", 4, "149.00", img("1560448204-e02f11c3d0e2"));
        roomType(innAndSuites, "Family Two-Bedroom Suite", "A two-bedroom suite with a kitchenette and pull-out sofa.",
                BedType.QUEEN, 2, "Mountain View", 6, "279.00", img("1595576508898-0ad5c879a061"));
    }

    private Hotel hotel(String name, String brand, String city, String region, String description, String address,
                        String heroImageUrl, List<String> imageUrls, List<String> amenities) {
        return hotels.save(new Hotel(name, brand.isBlank() ? null : brand, city, region, "Canada",
                description, address, heroImageUrl, imageUrls, amenities));
    }

    private RoomType roomType(Hotel hotel, String name, String description, BedType bedType, int bedrooms,
                              String viewType, int maxOccupancy, String nightlyRate, String imageUrl) {
        return roomTypes.save(new RoomType(hotel, name, description, bedType, bedrooms, viewType, maxOccupancy,
                new BigDecimal(nightlyRate), imageUrl));
    }

    private String img(String unsplashPhotoId) {
        return Destinations.img(unsplashPhotoId);
    }
}
