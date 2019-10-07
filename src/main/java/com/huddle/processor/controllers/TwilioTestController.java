package com.huddle.processor.controllers;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Log4j2
public class TwilioTestController {
  public static final String ACCOUNT_SID = "AC6820e790989f115ea69a22ce18861ce7";
  public static final String AUTH_TOKEN = "d92fe4670e1f7cb095a42b7e3b74a6dc";

  @GetMapping("/message/test")
  public String testMessage(final String phoneNumber) {
    Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

    Message message = Message.creator(new PhoneNumber("whatsapp:+"+phoneNumber),
        new PhoneNumber("whatsapp:+14155238886"),
        "Your appointment is coming up on 2019/05/29 at 2:00 PM").create();
    log.info(message);
    return message.getSid();
  }
}
