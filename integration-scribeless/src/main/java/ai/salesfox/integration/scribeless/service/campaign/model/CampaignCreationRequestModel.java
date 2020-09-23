package ai.salesfox.integration.scribeless.service.campaign.model;

import ai.salesfox.integration.scribeless.model.ScribelessAddressModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignCreationRequestModel {
    private String paper_size;
    private String handwriting_style;
    private String title;
    private String product;
    private String text;
    private String inserts;
    private String due_date; // dd/mm/yyyy
    private String notes;
    private String header_image;
    private String header_type;
    private String header_font;
    private String header_text;
    private String footer_text;
    private String footer_font;
    private ScribelessAddressModel return_address;
    private List<ScribelessAddressModel> recipients;

    /*
    {
  "paper_size": "A5",
  "handwriting_style": "Jane",
  "title": "My campaign",
  "product": "Advanced",
  "text": "Hello {{{first name}}},This is a test!",
  "inserts": "$5 Starbucks gift card",
  "due_date": "31/01/2021",
  "notes": "Campaign notes",
  "header_image": "http://app.thehandwriting.company/static/images/your_logo.png",
  "header_type": "Logo",
  "header_font": "josefin",
  "header_text": "Header text",
  "footer_text": "Footer text",
  "footer_font": "josefin",
  "return_address": {
    "address line 1": "Flat 1",
    "address line 2": "123 Broom Road",
    "address line 3": "Bathwick hill",
    "city": "London",
    "country": "United Kingdom",
    "department": "HR",
    "first name": "Tim",
    "last name": "Johnson",
    "state/region": "London",
    "title": "Mr",
    "zip/postal code": "TW11 9PG"
  },
  "recipients": [
    {
      "address line 1": "Flat 1",
      "address line 2": "123 Broom Road",
      "address line 3": "Bathwick hill",
      "city": "London",
      "country": "United Kingdom",
      "department": "HR",
      "first name": "Tim",
      "last name": "Johnson",
      "state/region": "London",
      "title": "Mr",
      "zip/postal code": "TW11 9PG"
    }
  ]
}
     */
}
