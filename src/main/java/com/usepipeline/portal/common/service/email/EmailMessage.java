package com.usepipeline.portal.common.service.email;

import java.util.List;

public interface EmailMessage {
    List<String> getRecipients();

    String createHtmlBody();

}
