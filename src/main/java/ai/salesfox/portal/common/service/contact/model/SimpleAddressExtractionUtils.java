package ai.salesfox.portal.common.service.contact.model;

import ai.salesfox.portal.common.model.PortalAddressModel;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;

public class SimpleAddressExtractionUtils {
    public static Optional<PortalAddressModel> extractSimpleAddress(String addressString) {
        String[] splitAddress = StringUtils.split(addressString, ' ');
        if (null == splitAddress || splitAddress.length < 6) {
            // Expected at least 5 tokens: zip, state, city, street-type, street-name, street-number
            return Optional.empty();
        }

        PortalAddressModel extractedAddressModel = new PortalAddressModel();
        extractedAddressModel.setIsBusiness(false);

        Stack<String> reverseAddressTokens = new Stack<>();
        Arrays.stream(splitAddress)
                .map(StringUtils::trimToNull)
                .filter(Objects::nonNull)
                .forEach(reverseAddressTokens::push);
        if (reverseAddressTokens.peek().length() < 5) {
            reverseAddressTokens.pop();
        } else {
            String zipCodeCandidate = reverseAddressTokens.pop();
            int zipCodeCandidateLength = zipCodeCandidate.length();
            if (zipCodeCandidateLength == 5 || zipCodeCandidateLength == 10) {
                extractedAddressModel.setZipCode(zipCodeCandidate);
            } else {
                return Optional.empty();
            }
        }

        String twoLetterStateCandidate = reverseAddressTokens.pop();
        if (twoLetterStateCandidate.length() == 2) {
            extractedAddressModel.setState(twoLetterStateCandidate);
        } else {
            return Optional.empty();
        }

        boolean successfullyParsedCityAndStreet = appendCityAndStreet(extractedAddressModel, reverseAddressTokens);
        if (successfullyParsedCityAndStreet) {
            return Optional.of(extractedAddressModel);
        }
        return Optional.empty();
    }

    /**
     * @return true if successful
     */
    private static boolean appendCityAndStreet(PortalAddressModel targetAddressModel, Stack<String> remainingReverseTokens) {
        if (remainingReverseTokens.size() < 4) {
            return false;
        }

        String cityCandidate = StringUtils.removeEnd(remainingReverseTokens.pop(), ",");

        int remainingTokens = remainingReverseTokens.size();
        if (remainingTokens == 3) {
            targetAddressModel.setCity(cityCandidate);
            appendThreeTokenAddressLine1(targetAddressModel, remainingReverseTokens);
            return true;
        }

        if (remainingTokens == 4) {
            String unknownToken = remainingReverseTokens.pop();
            if (StringUtils.containsOnly(unknownToken, "#123456789")) {
                targetAddressModel.setCity(cityCandidate);
                targetAddressModel.setAddressLine2(unknownToken);
                appendThreeTokenAddressLine1(targetAddressModel, remainingReverseTokens);
                return true;
            } else {
                // Too many variables to be able to guess
                return false;
            }
        }

        return false;
    }

    private static void appendThreeTokenAddressLine1(PortalAddressModel targetAddressModel, Stack<String> remainingReverseTokens) {
        String streetTypeCandidate = remainingReverseTokens.pop();
        String streetNameCandidate = remainingReverseTokens.pop();
        String streetNumberCandidate = remainingReverseTokens.pop();
        targetAddressModel.setAddressLine1(String.format("%s %s %s", streetNumberCandidate, streetNameCandidate, streetTypeCandidate));
    }

}
