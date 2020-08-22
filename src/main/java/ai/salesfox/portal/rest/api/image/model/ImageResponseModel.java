package ai.salesfox.portal.rest.api.image.model;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.awt.image.BufferedImage;

public class ImageResponseModel extends ResponseEntity<BufferedImage> {
    public ImageResponseModel(BufferedImage image, MediaType imageMediaType) {
        super(image, createHeaders(imageMediaType), HttpStatus.OK);
    }

    private static HttpHeaders createHeaders(MediaType mediaType) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(mediaType);
        return httpHeaders;
    }

}
