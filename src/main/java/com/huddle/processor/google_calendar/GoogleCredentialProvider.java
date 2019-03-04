package com.huddle.processor.google_calendar;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.common.base.Preconditions;
import com.huddle.processor.controllers.EventController;
import com.huddle.processor.google.api.client.extensions.jdo.JdoDataStoreFactory;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Log4j2
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class GoogleCredentialProvider {

  @Autowired
  JdoDataStoreFactory dataStoreFactory;

  private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);

  public Credential get() throws GeneralSecurityException, IOException {
    Preconditions.checkNotNull(dataStoreFactory, "Data Store Factory cannot be null");
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    // Load client secrets.
    InputStream in = EventController.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
    GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

    // Build flow and trigger user authorization request.
    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
        .setDataStoreFactory(dataStoreFactory)
        .setAccessType("offline")
        .setApprovalPrompt("force")
        .build();
    LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
    return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
  }
}
