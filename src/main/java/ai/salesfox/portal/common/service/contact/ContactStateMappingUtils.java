package ai.salesfox.portal.common.service.contact;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class ContactStateMappingUtils {
    private static final Map<String, String> STATEMAP = new HashMap<>();

    static {
        STATEMAP.put("alabama", "AL");
        STATEMAP.put("alaska", "AK");
        STATEMAP.put("arizona", "AZ");
        STATEMAP.put("arkansas", "AR");
        STATEMAP.put("california", "CA");
        STATEMAP.put("colorado", "CO");
        STATEMAP.put("connecticut", "CT");
        STATEMAP.put("delaware", "DE");
        STATEMAP.put("florida", "FL");
        STATEMAP.put("georgia", "GA");
        STATEMAP.put("hawaii", "HI");
        STATEMAP.put("idaho", "ID");
        STATEMAP.put("illinois", "IL");
        STATEMAP.put("indiana", "IN");
        STATEMAP.put("iowa", "IA");
        STATEMAP.put("kansas", "KS");
        STATEMAP.put("kentucky", "KY");
        STATEMAP.put("louisiana", "LA");
        STATEMAP.put("maine", "ME");
        STATEMAP.put("maryland", "MD");
        STATEMAP.put("massachusetts", "MA");
        STATEMAP.put("michigan", "MI");
        STATEMAP.put("minnesota", "MN");
        STATEMAP.put("mississippi", "MS");
        STATEMAP.put("missouri", "MO");
        STATEMAP.put("montana", "MT");
        STATEMAP.put("nebraska", "NE");
        STATEMAP.put("nevada", "NV");
        STATEMAP.put("new hampshire", "NH");
        STATEMAP.put("newhampshire", "NH");
        STATEMAP.put("new jersey", "NJ");
        STATEMAP.put("newjersey", "NJ");
        STATEMAP.put("new mexico", "NM");
        STATEMAP.put("newmexico", "NM");
        STATEMAP.put("new york", "NY");
        STATEMAP.put("newyork", "NY");
        STATEMAP.put("north carolina", "NC");
        STATEMAP.put("northcarolina", "NC");
        STATEMAP.put("north dakota", "ND");
        STATEMAP.put("northdakota", "ND");
        STATEMAP.put("ohio", "OH");
        STATEMAP.put("oklahoma", "OK");
        STATEMAP.put("oregon", "OR");
        STATEMAP.put("pennsylvania", "PA");
        STATEMAP.put("rhode island", "RI");
        STATEMAP.put("rhodeisland", "RI");
        STATEMAP.put("south carolina", "SC");
        STATEMAP.put("southcarolina", "SC");
        STATEMAP.put("south dakota", "SD");
        STATEMAP.put("southdakota", "SD");
        STATEMAP.put("tennessee", "TN");
        STATEMAP.put("texas", "TX");
        STATEMAP.put("utah", "UT");
        STATEMAP.put("vermont", "VT");
        STATEMAP.put("virginia", "VA");
        STATEMAP.put("washington", "WA");
        STATEMAP.put("west virginia", "WV");
        STATEMAP.put("westvirginia", "WV");
        STATEMAP.put("wisconsin", "WI");
        STATEMAP.put("wyoming", "WY");
    }

    public static String verifyState(String state) {
        Set<String> errors = new LinkedHashSet<>();
        if (StringUtils.isBlank(state)) {
            return "State must not be null";
        }
        if (state.length() == 2) {
            return StringUtils.upperCase(state);
        } else {
            String lowerState = StringUtils.lowerCase(state);
            String foundStateCode = STATEMAP.get("lowerState");
            if (StringUtils.isBlank(foundStateCode)) {
                return state;
            } else {
                return foundStateCode;
            }
        }
    }

}
