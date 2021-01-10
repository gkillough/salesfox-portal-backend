package ai.salesfox.portal.integration.scribeless.api.on_demand;

import ai.salesfox.integration.common.exception.SalesfoxException;
import ai.salesfox.integration.common.util.SalesfoxEnumUtils;
import ai.salesfox.integration.scribeless.enumeration.ScribelessFontColor;
import ai.salesfox.integration.scribeless.enumeration.ScribelessHandwritingStyles;
import ai.salesfox.integration.scribeless.service.on_demand.OnDemandPreviewService;
import ai.salesfox.integration.scribeless.service.on_demand.model.OnDemandPreviewParams;
import ai.salesfox.portal.rest.api.image.model.ImageResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Slf4j
@Component
public class ScribelessOnDemandEndpointService {
    private final OnDemandPreviewService onDemandPreviewService;

    @Autowired
    public ScribelessOnDemandEndpointService(OnDemandPreviewService onDemandPreviewService) {
        this.onDemandPreviewService = onDemandPreviewService;
    }

    public ImageResponseModel getPreviewImage(String text, Integer widthInMM, Integer heightInMM, Integer sizeInMM, String fontColor, String handwritingStyle) {
        OnDemandPreviewParams previewParams = populateParamsObject(widthInMM, heightInMM, sizeInMM, fontColor, handwritingStyle);
        try {
            BufferedImage previewImage = onDemandPreviewService.getPreviewImage(text, previewParams);
            return new ImageResponseModel(previewImage, MediaType.IMAGE_PNG);
        } catch (SalesfoxException e) {
            log.error("Error with request to generate a Scribeless preview image", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not generate preview image");
        }
    }

    private OnDemandPreviewParams populateParamsObject(Integer widthInMM, Integer heightInMM, Integer sizeInMM, String fontColor, String handwritingStyle) {
        OnDemandPreviewParams previewParams = OnDemandPreviewParams.testing();

        List<String> errors = new ArrayList<>(5);
        setIntegerOrCreateErrorString(previewParams::setWidthInMillimeters, widthInMM, "widthInMM", 40).ifPresent(errors::add);
        setIntegerOrCreateErrorString(previewParams::setHeightInMillimeters, heightInMM, "heightInMM", 25).ifPresent(errors::add);
        setIntegerOrCreateErrorString(previewParams::setSizeInMillimeters, sizeInMM, "sizeInMM", 3).ifPresent(errors::add);

        if (null != fontColor) {
            if (!EnumUtils.isValidEnumIgnoreCase(ScribelessFontColor.class, fontColor)) {
                errors.add(String.format("The field 'fontColor' is invalid. Options: %s", SalesfoxEnumUtils.capitalizeValuesString(ScribelessFontColor.values())));
            } else {
                previewParams.setFontColor(formatRequestStringOrDefault(fontColor, ScribelessFontColor.BLACK.name()));
            }
        }

        if (null != handwritingStyle) {
            if (!EnumUtils.isValidEnumIgnoreCase(ScribelessHandwritingStyles.class, handwritingStyle)) {
                errors.add(String.format("The field 'handwritingStyle' is invalid. Options: %s", SalesfoxEnumUtils.capitalizeValuesString(ScribelessHandwritingStyles.values())));
            } else {
                previewParams.setHandwritingStyle(formatRequestStringOrDefault(handwritingStyle, ScribelessHandwritingStyles.STAFFORD.name()));
            }
        }

        if (!errors.isEmpty()) {
            String errorString = String.join(", ", errors);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("There were errors with the request: %s", errorString));
        }
        return previewParams;
    }

    private Optional<String> setIntegerOrCreateErrorString(Consumer<Integer> previewParamsSetter, Integer param, String paramName, int defaultValue) {
        if (null != param) {
            if (param > 0) {
                previewParamsSetter.accept(param);
            } else {
                return Optional.of(String.format("The field '%s' must be greater than zero", paramName));
            }
        } else {
            previewParamsSetter.accept(defaultValue);
        }
        return Optional.empty();
    }

    private String formatRequestStringOrDefault(String requestString, String defaultString) {
        if (StringUtils.isBlank(requestString)) {
            requestString = defaultString;
        }
        return StringUtils.capitalize(StringUtils.lowerCase(requestString));
    }

}
