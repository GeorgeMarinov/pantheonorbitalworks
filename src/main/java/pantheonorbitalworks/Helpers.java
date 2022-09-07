package pantheonorbitalworks;

public class Helpers {
    public static String weaponIdToLabel(String weaponId) {
        if (weaponId == null) {
            return weaponId;
        }
        switch (weaponId) {
            case "torgue_lightag":
                return "Light Assault Gun";
            case "torgue_lightmortar":
                return "Light Mortar";
            case "torgue_chaingun":
                return "Assault Chaingun";
            case "torgue_dualflak":
                return "Dual Flak Cannon";
            case "torgue_flak":
                return "Flak Cannon";
            case "torgue_heavymauler":
                return "Heavy Mauler";
            case "torgue_heavymortar":
                return "Heavy Mortar";
            case "torgue_devastator":
                return "Devastator Cannon";
            case "torgue_hellbore":
                return "Hellbore Cannon";
            case "torgue_hephag":
                return "Hephaestus Assault Gun";

            default:
                return weaponId;
        }
    }
}
