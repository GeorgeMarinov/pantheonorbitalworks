package pantheonorbitalworks;

public class RefitRepresentative {
    public String Name;
    public String Make;

    public RefitRepresentative(String name, String make) {
        Name = name;
        Make = make;
    }

    public static final RefitRepresentative[] list = new RefitRepresentative[] {
            new RefitRepresentative("Mr. Torgue", "Torgue"),
            // new RefitRepresentative("Andreyevna", "Vladof"),
            // new RefitRepresentative("Katagawa Jr.", "Maliwan"),
            // new RefitRepresentative("Jacobs", "Jacobs"),
            // new RefitRepresentative("Rhys Strongfork", "Atlas"),
            // new RefitRepresentative("Handsome Jack", "Hyperion")
    };
}
